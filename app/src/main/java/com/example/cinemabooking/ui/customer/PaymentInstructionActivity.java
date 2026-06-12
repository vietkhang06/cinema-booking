package com.example.cinemabooking.ui.customer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.remote.api.BookingApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.example.cinemabooking.ui.customer.transaction.TicketDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

public class PaymentInstructionActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "bookingId";
    public static final String EXTRA_PAYMENT_ID = "paymentId";
    public static final String EXTRA_PAYMENT_CODE = "paymentCode";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_PAYMENT_METHOD = "paymentMethod";

    private String bookingId;
    private String paymentId;
    private String paymentCode;
    private double amount;
    private String paymentMethod;

    private TextView tvStatusText;
    private TextView tvAmount;
    private TextView tvPaymentCode;
    private TextView tvAccountNumber;
    private ImageView imgQrCode;
    private ProgressBar pbQrLoading;
    private MaterialButton btnOpenApp;
    private View layoutTestButtons;
    private MaterialButton btnTestSuccess;
    private MaterialButton btnTestFailed;

    private FirebaseFirestore db;
    private ListenerRegistration paymentListener;
    private ListenerRegistration bookingListener;

    // Guard: đảm bảo payment chỉ được xử lý MỘT LẦN dù listener fire nhiều lần
    private volatile boolean paymentHandled = false;
    private BookingTimerManager.TimerListener timerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_instruction);

        // Retrieve extras
        bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        paymentId = getIntent().getStringExtra(EXTRA_PAYMENT_ID);
        paymentCode = getIntent().getStringExtra(EXTRA_PAYMENT_CODE);
        amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0.0);
        paymentMethod = getIntent().getStringExtra(EXTRA_PAYMENT_METHOD);

        if (bookingId == null || paymentCode == null) {
            Toast.makeText(this, "Không có thông tin đặt vé hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
        loadQrCode();
        startPaymentListener();
        startBookingListener(); // Listener thứ 2: lắng nghe bookings doc để bắt paymentStatus

        long createdAt = getIntent().getLongExtra("createdAt", 0);
        if (createdAt > 0) {
            startCountdownTimer(createdAt + 450000);
        }
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvStatusText = findViewById(R.id.tvStatusText);
        tvAmount = findViewById(R.id.tvAmount);
        tvPaymentCode = findViewById(R.id.tvPaymentCode);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        imgQrCode = findViewById(R.id.imgQrCode);
        pbQrLoading = findViewById(R.id.pbQrLoading);
        btnOpenApp = findViewById(R.id.btnOpenApp);
        layoutTestButtons = findViewById(R.id.layoutTestButtons);
        btnTestSuccess = findViewById(R.id.btnTestSuccess);
        btnTestFailed = findViewById(R.id.btnTestFailed);

        tvAmount.setText(String.format(Locale.getDefault(), "%,.0f đ", amount));
        tvPaymentCode.setText(paymentCode);

        if ("momo".equals(paymentMethod)) {
            btnOpenApp.setText("Mở ứng dụng MoMo");
            if (layoutTestButtons != null) {
                layoutTestButtons.setVisibility(View.VISIBLE);
            }
        } else {
            btnOpenApp.setText("Mở ứng dụng Ngân hàng");
            if (layoutTestButtons != null) {
                layoutTestButtons.setVisibility(View.GONE);
            }
        }
    }

    private void setupListeners() {
        ImageButton btnCopyAccount = findViewById(R.id.btnCopyAccount);
        ImageButton btnCopyAmount = findViewById(R.id.btnCopyAmount);
        ImageButton btnCopyCode = findViewById(R.id.btnCopyCode);

        if (btnCopyAccount != null) {
            btnCopyAccount.setOnClickListener(v -> copyToClipboard("Số tài khoản", tvAccountNumber.getText().toString()));
        }

        if (btnCopyAmount != null) {
            btnCopyAmount.setOnClickListener(v -> {
                String rawAmount = String.format(Locale.getDefault(), "%.0f", amount);
                copyToClipboard("Số tiền", rawAmount);
            });
        }

        if (btnCopyCode != null) {
            btnCopyCode.setOnClickListener(v -> copyToClipboard("Nội dung chuyển khoản", paymentCode));
        }

        if (btnOpenApp != null) {
            btnOpenApp.setOnClickListener(v -> openPaymentApp());
        }

        if (btnTestSuccess != null) {
            btnTestSuccess.setOnClickListener(v -> handleTestPaymentSuccess());
        }

        if (btnTestFailed != null) {
            btnTestFailed.setOnClickListener(v -> handleTestPaymentFailed());
        }
    }

    private void loadQrCode() {
        String encodedAccountName = Uri.encode("CONG TY CP CINEMA VIETNAM");
        String qrUrl =
                "https://img.vietqr.io/image/VCB-1036894913-compact.png"
                        + "?amount=" + amount
                        + "&addInfo=" + paymentCode
                        + "&accountName=DOAN%20VIET%20KHANG";

        pbQrLoading.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(qrUrl)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model,
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        pbQrLoading.setVisibility(View.GONE);
                        Toast.makeText(PaymentInstructionActivity.this, "Không thể tải mã QR", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                   com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource,
                                                   boolean isFirstResource) {
                        pbQrLoading.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imgQrCode);
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép " + label + " vào bộ nhớ tạm", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPaymentApp() {
        if ("momo".equals(paymentMethod)) {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.mservice.momotransfer");
            if (intent != null) {
                startActivity(intent);
            } else {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mservice.momotransfer")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.mservice.momotransfer")));
                }
            }
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dl.vietqr.co/pay"));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Vui lòng mở ứng dụng ngân hàng của bạn để chuyển khoản.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Listener 1: Lắng nghe collection "payments" — bắt khi payment doc được tạo/cập nhật.
     */
    private void startPaymentListener() {
        if (paymentId != null && !paymentId.isEmpty()) {
            paymentListener = db.collection("payments").document(paymentId)
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            android.util.Log.e("PAYMENT_FLOW", "payments listener failed", e);
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            checkPaymentStatus(snapshot.getString("status"));
                        }
                    });
        } else {
            // Fallback: query by bookingId
            paymentListener = db.collection("payments")
                    .whereEqualTo("bookingId", bookingId)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            android.util.Log.e("PAYMENT_FLOW", "payments query listener failed", e);
                            return;
                        }
                        if (snapshots != null && !snapshots.isEmpty()) {
                            DocumentSnapshot doc = snapshots.getDocuments().get(0);
                            checkPaymentStatus(doc.getString("status"));
                        }
                    });
        }
    }

    /**
     * Listener 2: Lắng nghe document "bookings/{bookingId}" — bắt khi backend
     * update paymentStatus trực tiếp trên booking document (không qua payments collection).
     * Đây là fallback quan trọng khi payments doc chưa tồn tại.
     */
    private void startBookingListener() {
        if (bookingId == null) return;
        bookingListener = db.collection("bookings").document(bookingId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        android.util.Log.e("PAYMENT_FLOW", "bookings listener failed", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        Long createdAtVal = snapshot.getLong("createdAt");
                        if (createdAtVal != null && createdAtVal > 0) {
                            startCountdownTimer(createdAtVal + 450000);
                        }

                        // Kiểm tra cả paymentStatus lẫn bookingStatus
                        String paymentStatus = snapshot.getString("paymentStatus");
                        String bookingStatus = snapshot.getString("bookingStatus");
                        android.util.Log.d("PAYMENT_FLOW", "Booking doc update — paymentStatus=" + paymentStatus + " bookingStatus=" + bookingStatus);
                        // Ưu tiên paymentStatus, fallback sang bookingStatus
                        String effectiveStatus = paymentStatus != null ? paymentStatus : bookingStatus;
                        checkPaymentStatus(effectiveStatus);
                    }
                });
    }

    /**
     * Kiểm tra và xử lý trạng thái payment. Guard paymentHandled đảm bảo
     * chỉ xử lý 1 lần dù 2 listeners cùng fire.
     */
    private void checkPaymentStatus(String status) {
        if (status == null) return;
        // Guard: đã xử lý rồi thì bỏ qua
        if (paymentHandled) return;

        android.util.Log.d("PAYMENT_FLOW", "checkPaymentStatus: " + status);

        if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)
                || "CONFIRMED".equalsIgnoreCase(status)) {
            // Đánh dấu đã xử lý TRƯỚC — tránh race condition từ 2 listeners
            paymentHandled = true;
            stopAllListeners();
            BookingTimerManager.getInstance().stopTimer(this);

            createNotification("Thanh toán thành công", "Giao dịch thanh toán vé xem phim của bạn đã thành công. Chúc bạn xem phim vui vẻ!", "BOOKING_SUCCESS");
            Toast.makeText(this, "Thanh toán thành công! Đang xuất vé...", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra(TicketDetailActivity.EXTRA_BOOKING_ID, bookingId);
            intent.putExtra("EXTRA_FROM_BOOKING_SUCCESS", true);
            startActivity(intent);
            finish();

        } else if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status)) {
            if (!paymentHandled) {
                paymentHandled = true;
                stopAllListeners();
                BookingTimerManager.getInstance().stopTimer(this);
                createNotification("Thanh toán thất bại", "Giao dịch thanh toán của bạn đã thất bại hoặc bị hủy.", "BOOKING_FAILED");
                tvStatusText.setText("Trạng thái: Giao dịch thất bại hoặc đã bị huỷ");
                View banner = findViewById(R.id.layoutStatusBanner);
                if (banner != null) {
                    banner.setBackgroundColor(0xFFD32F2F);
                }
            }
        } else if ("WAITING_CONFIRMATION".equalsIgnoreCase(status)) {
            paymentHandled = true;
            stopAllListeners();
            BookingTimerManager.getInstance().stopTimer(this);
            tvStatusText.setText("Trạng thái: Chờ Admin xác nhận...");
            View banner = findViewById(R.id.layoutStatusBanner);
            if (banner != null) {
                banner.setBackgroundColor(0xFF1976D2);
            }
        } else if ("PENDING".equalsIgnoreCase(status)) {
            View banner = findViewById(R.id.layoutStatusBanner);
            if (banner != null) {
                banner.setBackgroundColor(0xFFF57C00); // Keep orange for active countdown
            }
        }
    }

    private void handleTestPaymentSuccess() {
        // Disable button immediately to prevent double-click spam
        if (btnTestSuccess != null) btnTestSuccess.setEnabled(false);

        android.util.Log.d("BOOKING_FLOW", "TEST_BUTTON_CLICKED: SUCCESS. Calling PUT /api/v1/bookings/payment/" + bookingId + "/confirmed");

        BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
        bookingApi.confirmPayment(bookingId).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Void>> call, retrofit2.Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    android.util.Log.d("BOOKING_FLOW", "confirmPayment API succeeded — waiting for Firestore listener to fire");
                    // KHÔNG Toast ở đây — listener sẽ tự xử lý khi Firestore update
                } else {
                    if (btnTestSuccess != null) btnTestSuccess.setEnabled(true);
                    Toast.makeText(PaymentInstructionActivity.this, "Lỗi cập nhật test success: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Void>> call, Throwable t) {
                if (btnTestSuccess != null) btnTestSuccess.setEnabled(true);
                Toast.makeText(PaymentInstructionActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleTestPaymentFailed() {
        performBookingCancellation(false);
    }

    private void performBookingCancellation(boolean isAuto) {
        if (btnTestFailed != null) btnTestFailed.setEnabled(false);

        android.util.Log.d("BOOKING_FLOW", "Cancellation requested. Auto=" + isAuto + ". Calling PUT /api/v1/bookings/payment/" + bookingId + "/failed");

        BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
        bookingApi.cancelBooking(bookingId).enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Void>> call, retrofit2.Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    android.util.Log.d("BOOKING_FLOW", "cancelBooking API succeeded");
                    if (isAuto) {
                        Toast.makeText(PaymentInstructionActivity.this, "Giao dịch đã bị huỷ do hết hạn giữ ghế!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PaymentInstructionActivity.this, "Giả lập: Thanh toán thất bại!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (btnTestFailed != null) btnTestFailed.setEnabled(true);
                    Toast.makeText(PaymentInstructionActivity.this, "Lỗi huỷ giao dịch: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Void>> call, Throwable t) {
                if (btnTestFailed != null) btnTestFailed.setEnabled(true);
                Toast.makeText(PaymentInstructionActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopAllListeners() {
        if (paymentListener != null) {
            paymentListener.remove();
            paymentListener = null;
        }
        if (bookingListener != null) {
            bookingListener.remove();
            bookingListener = null;
        }
    }

    private void createNotification(String title, String message, String type) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? 
                        com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;
        com.example.cinemabooking.domain.model.Notification notification = new com.example.cinemabooking.domain.model.Notification();
        notification.userId = userId;
        notification.title = title;
        notification.message = message;
        notification.type = type;
        notification.isRead = false;
        notification.createdAt = System.currentTimeMillis();
        notification.updatedAt = System.currentTimeMillis();

        new com.example.cinemabooking.data.repository.NotificationRepositoryImpl()
            .createNotification(notification, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timerListener != null) {
            BookingTimerManager.getInstance().registerListener(timerListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timerListener != null) {
            BookingTimerManager.getInstance().unregisterListener(timerListener);
        }
    }

    private void startCountdownTimer(long targetEndTime) {
        if (paymentHandled) return;

        long currentRemaining = BookingTimerManager.getInstance().getRemainingTimeMillis();
        long currentEndTime = System.currentTimeMillis() + currentRemaining;
        long diff = Math.abs(currentEndTime - targetEndTime);

        if (BookingTimerManager.getInstance().isTimerActive(this) && diff < 2000) {
            if (timerListener == null) {
                setupTimerListener();
                BookingTimerManager.getInstance().registerListener(timerListener);
            }
            return;
        }

        if (timerListener == null) {
            setupTimerListener();
        }

        BookingTimerManager.getInstance().startTimerWithEndTime(this, targetEndTime);
        BookingTimerManager.getInstance().registerListener(timerListener);
    }

    private void setupTimerListener() {
        timerListener = new BookingTimerManager.TimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                if (paymentHandled) return;
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                if (tvStatusText != null) {
                    tvStatusText.setText(String.format(Locale.getDefault(), "Trạng thái: Chờ thanh toán (%02d:%02d)", minutes, seconds));
                }
            }

            @Override
            public void onFinish() {
                if (paymentHandled) return;
                paymentHandled = true;
                stopAllListeners();
                BookingTimerManager.getInstance().stopTimer(PaymentInstructionActivity.this);
                if (tvStatusText != null) {
                    tvStatusText.setText("Trạng thái: Hết hạn giữ ghế!");
                }
                View banner = findViewById(R.id.layoutStatusBanner);
                if (banner != null) {
                    banner.setBackgroundColor(0xFFD32F2F);
                }
                Toast.makeText(PaymentInstructionActivity.this, "Thời gian giữ ghế đã hết!", Toast.LENGTH_LONG).show();
                performBookingCancellation(true);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllListeners();
    }
}
