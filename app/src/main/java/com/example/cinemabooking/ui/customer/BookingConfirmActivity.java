package com.example.cinemabooking.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.BookingDTO;
import com.example.cinemabooking.data.dto.SeatBookingRequestDTO;
import com.example.cinemabooking.data.remote.api.BookingApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingConfirmActivity extends AppCompatActivity {

    public static final String EXTRA_SHOWTIME_ID = "showtimeId";
    public static final String EXTRA_MOVIE_TITLE = "movieTitle";
    public static final String EXTRA_MOVIE_ID = "movieId";
    public static final String EXTRA_POSTER_URL = "posterUrl";
    public static final String EXTRA_CINEMA_NAME = "cinemaName";
    public static final String EXTRA_SHOWTIME_START = "showtimeStart";
    public static final String EXTRA_TOTAL = "total";
    public static final String EXTRA_SEAT_CODES = "seatCodes";
    public static final String EXTRA_SEAT_IDS = "seatIds";

    private String showtimeId, movieTitle, movieId, cinemaName, imageUrl;
    private long showtimeStart;
    private double total;
    private ArrayList<String> seatCodes, seatIds;

    private TextView tvTimer;
    private BookingTimerManager.TimerListener timerListener;

    // Phase 3 Promotion Fields
    private com.example.cinemabooking.domain.model.User currentUser;
    private double discountVoucher = 0;
    private double discountRank = 0;
    private double discountStars = 0;
    private String appliedPromoCode = "";
    private boolean isStarsApplied = false;

    private TextView tvOriginalPrice, tvTotal, tvAppliedPromo, tvStarsLabel;
    private com.google.android.material.switchmaterial.SwitchMaterial switchStars;
    private TextView btnPromo;

    // Phase 4 Payment Fields
    private android.widget.RadioGroup rgPayment;
    private String selectedPaymentMethod = "cash";
    private com.google.android.material.bottomsheet.BottomSheetDialog momoDialog;

    // Phase 5 Age Rating Fields
    private TextView tvAgeRatingBadge;
    private String movieAgeRating = "P";

    private boolean isBookingConfirmed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirm);

        // Nhận data
        showtimeId = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        cinemaName = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        imageUrl = getIntent().getStringExtra(EXTRA_POSTER_URL);
        showtimeStart = getIntent().getLongExtra(EXTRA_SHOWTIME_START, 0);
        total = getIntent().getDoubleExtra(EXTRA_TOTAL, 0);
        seatCodes = getIntent().getStringArrayListExtra(EXTRA_SEAT_CODES);
        seatIds = getIntent().getStringArrayListExtra(EXTRA_SEAT_IDS);

        tvTimer = findViewById(R.id.tvTimer);

        // Start timer if not already active
        if (!BookingTimerManager.getInstance().isTimerActive(this)) {
            BookingTimerManager.getInstance().startTimer(this, 7 * 60 * 1000);
        } else {
            BookingTimerManager.getInstance().restoreTimer(this);
        }

        timerListener = new BookingTimerManager.TimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                if (tvTimer != null) {
                    tvTimer.setText(String.format(Locale.getDefault(), "Thời gian giữ ghế: %02d:%02d", minutes, seconds));
                }
            }

            @Override
            public void onFinish() {
                if (tvTimer != null) {
                    tvTimer.setText("Thời gian giữ ghế: 00:00");
                }
                if (momoDialog != null && momoDialog.isShowing()) {
                    momoDialog.dismiss();
                }
                if (!isBookingConfirmed) {
                    releaseLockedSeats();
                }
                Toast.makeText(BookingConfirmActivity.this, "Thời gian giữ ghế đã hết! Vui lòng chọn lại.", Toast.LENGTH_LONG).show();
                finish();
            }
        };

        initViews();
        loadUserProfile();
        loadMovieData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Bind data lên UI
        TextView tvMovie = findViewById(R.id.tvMovieName);
        TextView tvCinema = findViewById(R.id.tvCinemaName);
        TextView tvTime = findViewById(R.id.tvShowtime);
        TextView tvSeats = findViewById(R.id.tvSeats);

        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvTotal = findViewById(R.id.tvTotal);
        btnPromo = findViewById(R.id.btnPromo);
        tvAppliedPromo = findViewById(R.id.tvAppliedPromo);
        tvStarsLabel = findViewById(R.id.tvStarsLabel);
        switchStars = findViewById(R.id.switchStars);
        tvAgeRatingBadge = findViewById(R.id.tvAgeRatingBadge);

        if (tvMovie != null && movieTitle != null) tvMovie.setText(movieTitle);
        if (tvCinema != null && cinemaName != null) tvCinema.setText(cinemaName);

        if (tvTime != null && showtimeStart > 0) {
            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
            tvTime.setText(fmt.format(new Date(showtimeStart)));
        }

        if (tvSeats != null && seatCodes != null) {
            tvSeats.setText(String.join(", ", seatCodes));
        }

        if (tvTotal != null) {
            tvTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
        }

        rgPayment = findViewById(R.id.rgPayment);
        if (rgPayment != null) {
            rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbPayCash) {
                    selectedPaymentMethod = "cash";
                } else if (checkedId == R.id.rbPayBank) {
                    selectedPaymentMethod = "bank";
                } else if (checkedId == R.id.rbPayMomo) {
                    selectedPaymentMethod = "momo";
                }
            });
        }

        if (btnPromo != null) {
            btnPromo.setOnClickListener(v -> showPromoDialog());
        }

        MaterialButton btnConfirm = findViewById(R.id.btnConfirm);
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> checkAgeRatingAndProceed());
        }
    }

    private void loadMovieData() {
        if (movieId == null || movieId.isEmpty()) return;
        FirebaseFirestore.getInstance()
                .collection("movies")
                .document(movieId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        movieAgeRating = documentSnapshot.getString("ageRating");
                        if (movieAgeRating == null) movieAgeRating = "P";
                        updateAgeRatingBadge();
                    }
                });
    }

    private void updateAgeRatingBadge() {
        if (tvAgeRatingBadge == null || movieAgeRating == null) return;
        tvAgeRatingBadge.setText(movieAgeRating);
        tvAgeRatingBadge.setVisibility(android.view.View.VISIBLE);

        int color = 0xFF888888;
        String rating = movieAgeRating.toUpperCase().trim();
        if (rating.contains("18")) {
            color = 0xFFD32F2F;
        } else if (rating.contains("16")) {
            color = 0xFFF57C00;
        } else if (rating.contains("13")) {
            color = 0xFFFBC02D;
        } else if (rating.contains("P")) {
            color = 0xFF388E3C;
        } else if (rating.contains("K")) {
            color = 0xFF1976D2;
        }

        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(10.0f);
        tvAgeRatingBadge.setBackground(gd);
        tvAgeRatingBadge.setTextColor(0xFFFFFFFF);
        tvAgeRatingBadge.setPadding(16, 4, 16, 4);
    }

    private void checkAgeRatingAndProceed() {
        String rating = movieAgeRating != null ? movieAgeRating.toUpperCase().trim() : "P";
        if (rating.contains("18") || rating.contains("16") || rating.contains("13")) {
            int minAge = 18;
            if (rating.contains("16")) minAge = 16;
            else if (rating.contains("13")) minAge = 13;

            showAgeWarningDialog(minAge, this::confirmBooking);
        } else {
            confirmBooking();
        }
    }

    private void showAgeWarningDialog(int minAge, Runnable onConfirm) {
        com.google.android.material.bottomsheet.BottomSheetDialog warnDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_age_warning, null);
        warnDialog.setContentView(view);

        TextView tvWarningTitle = view.findViewById(R.id.tvWarningTitle);
        TextView tvWarningMsg = view.findViewById(R.id.tvWarningMsg);
        android.widget.Button btnCancel = view.findViewById(R.id.btnCancelWarn);
        android.widget.Button btnAgree = view.findViewById(R.id.btnAgreeWarn);

        if (tvWarningTitle != null) {
            tvWarningTitle.setText("Xác nhận độ tuổi tối thiểu C" + minAge);
        }

        if (tvWarningMsg != null) {
            tvWarningMsg.setText("Phim này có phân loại độ tuổi là C" + minAge + " - CHỈ DÀNH CHO KHÁN GIẢ TỪ " + minAge + " TUỔI TRỞ LÊN. Vui lòng xác nhận bạn đủ tuổi trước khi tiếp tục thanh toán. Vé đã mua không được hoàn trả hoặc đổi trả nếu không đủ tuổi.");
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> warnDialog.dismiss());
        }

        if (btnAgree != null) {
            btnAgree.setOnClickListener(v -> {
                warnDialog.dismiss();
                onConfirm.run();
            });
        }

        warnDialog.show();
    }

    private void loadUserProfile() {
        new com.example.cinemabooking.service.AuthenticationService(this).getCurrentAuthUser(
                new com.example.cinemabooking.domain.common.ResultCallback<com.example.cinemabooking.domain.model.User>() {
                    @Override
                    public void onSuccess(com.example.cinemabooking.domain.model.User user) {
                        if (user != null) {
                            currentUser = user;
                            applyTierDiscount();
                            updateStarsUI();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(BookingConfirmActivity.this, "Không thể tải thông tin thành viên. Ưu đãi thẻ có thể không được áp dụng.", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void applyTierDiscount() {
        if (currentUser == null || currentUser.memberLevel == null) return;
        String level = currentUser.memberLevel.toLowerCase();
        double factor = 0;
        String levelName = "Thành viên";
        if (level.contains("vip")) {
            factor = 0.10;
            levelName = "VIP";
        } else if (level.contains("platinum")) {
            factor = 0.15;
            levelName = "Platinum";
        } else if (level.contains("gold")) {
            factor = 0.08;
            levelName = "Gold";
        }
        if (factor > 0) {
            discountRank = total * factor;
            if (tvAppliedPromo != null) {
                tvAppliedPromo.setText("Đã áp dụng ưu đãi hạng " + levelName + " (-" + (int) (factor * 100) + "%)");
            }
        }
        updateTotalPrice();
    }

    private void updateStarsUI() {
        if (currentUser == null) return;
        int points = (currentUser.points != null) ? currentUser.points : 0;
        if (tvStarsLabel != null) {
            tvStarsLabel.setText(String.format(Locale.getDefault(), "Áp dụng điểm Stars (%d Stars có sẵn)", points));
        }
        if (switchStars != null) {
            switchStars.setEnabled(points > 0);
            switchStars.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isStarsApplied = isChecked;
                if (isChecked) {
                    discountStars = points * 1000.0;
                } else {
                    discountStars = 0;
                }
                updateTotalPrice();
            });
        }
    }

    private void updateTotalPrice() {
        double finalTotal = total - discountVoucher - discountRank - discountStars;
        if (finalTotal < 0) finalTotal = 0;

        if (discountVoucher > 0 || discountRank > 0 || discountStars > 0) {
            if (tvOriginalPrice != null) {
                tvOriginalPrice.setVisibility(android.view.View.VISIBLE);
                tvOriginalPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            }
        } else {
            if (tvOriginalPrice != null) {
                tvOriginalPrice.setVisibility(android.view.View.GONE);
            }
        }

        if (tvTotal != null) {
            tvTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", finalTotal));
        }
    }

    private void showPromoDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_promo_input, null);
        dialog.setContentView(view);

        android.widget.EditText edtPromoCode = view.findViewById(R.id.edtPromoCode);
        android.widget.Button btnApplyPromo = view.findViewById(R.id.btnApplyPromo);
        TextView tvPromoStatus = view.findViewById(R.id.tvPromoStatus);

        if (!appliedPromoCode.isEmpty() && edtPromoCode != null) {
            edtPromoCode.setText(appliedPromoCode);
        }

        if (btnApplyPromo != null) {
            btnApplyPromo.setOnClickListener(v -> {
                if (edtPromoCode == null) return;
                String code = edtPromoCode.getText().toString().trim().toUpperCase();
                if (code.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mã khuyến mãi!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double voucherValue = 0;
                String promoMsg = "";
                boolean isValid = false;

                if ("GALAXY50".equals(code)) {
                    voucherValue = 50000;
                    promoMsg = "Đã áp dụng mã GALAXY50 (-50k)";
                    isValid = true;
                } else if ("WELCOME10".equals(code)) {
                    voucherValue = total * 0.10;
                    promoMsg = "Đã áp dụng mã WELCOME10 (-10%)";
                    isValid = true;
                } else if ("FREESHOP".equals(code)) {
                    voucherValue = 20000;
                    promoMsg = "Đã áp dụng mã FREESHOP (-20k)";
                    isValid = true;
                }

                if (isValid) {
                    discountVoucher = voucherValue;
                    appliedPromoCode = code;
                    if (tvAppliedPromo != null) {
                        tvAppliedPromo.setText(promoMsg);
                        tvAppliedPromo.setTextColor(0xFF4CAF50);
                    }
                    updateTotalPrice();
                    dialog.dismiss();
                    Toast.makeText(this, "Áp dụng mã khuyến mãi thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    if (tvPromoStatus != null) {
                        tvPromoStatus.setText("Mã khuyến mãi không hợp lệ hoặc đã hết hạn!");
                        tvPromoStatus.setVisibility(android.view.View.VISIBLE);
                    }
                }
            });
        }

        dialog.show();
    }

    private void confirmBooking() {
        if ("momo".equals(selectedPaymentMethod)) {
            showMomoCheckoutDialog(selectedPaymentMethod);
        } else {
            createBookingOnBackend(selectedPaymentMethod);
        }
    }

    private void createBookingOnBackend(String paymentMethod) {
        MaterialButton btnConfirm = findViewById(R.id.btnConfirm);
        if (btnConfirm != null) btnConfirm.setEnabled(false);

        List<SeatBookingRequestDTO.SnackOrder> snackOrders = new ArrayList<>();

        SeatBookingRequestDTO request = new SeatBookingRequestDTO(
                showtimeId,
                seatIds != null ? seatIds : new ArrayList<>(),
                snackOrders,
                paymentMethod
        );

        BookingApiService bookingApi = RetrofitClient.getInstance()
                .create(BookingApiService.class);

        bookingApi.createBooking(request).enqueue(new retrofit2.Callback<ApiResponse<BookingDTO>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<BookingDTO>> call, retrofit2.Response<ApiResponse<BookingDTO>> response) {
                if (btnConfirm != null) btnConfirm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    BookingDTO booking = response.body().getData();
                    isBookingConfirmed = true;
                    BookingTimerManager.getInstance().stopTimer(BookingConfirmActivity.this);

                    if ("momo".equals(paymentMethod) || "bank".equals(paymentMethod)) {
                        Intent intent = new Intent(BookingConfirmActivity.this, PaymentInstructionActivity.class);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_BOOKING_ID, booking.bookingId);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_ID, (String) null);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_CODE, booking.paymentCode);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_AMOUNT, booking.total);
                        intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_METHOD, paymentMethod);
                        intent.putExtra("createdAt", booking.createdAt);
                        startActivity(intent);
                        finish();
                    } else {
                        createNotification("Đặt vé thành công", "Bạn đã đặt vé thành công. Vui lòng thanh toán tại quầy trước khi suất chiếu bắt đầu 15 phút.", "BOOKING_SUCCESS");
                        Toast.makeText(BookingConfirmActivity.this, "Đặt vé thành công (Chờ thanh toán tại quầy)!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    String msg = "Lỗi tạo vé. Vui lòng thử lại.";
                    if (response.code() == 409) {
                        msg = "Xung đột: Ghế đã được đặt hoặc đang có người khác giữ!";
                    } else if (response.code() == 403) {
                        msg = "Lỗi: Bạn không giữ ghế này hoặc đã hết hạn giữ ghế!";
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    createNotification("Đặt vé thất bại", msg, "BOOKING_FAILED");
                    Toast.makeText(BookingConfirmActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<BookingDTO>> call, Throwable t) {
                if (btnConfirm != null) btnConfirm.setEnabled(true);
                Toast.makeText(BookingConfirmActivity.this, "Kết nối mạng không ổn định. Vui lòng kiểm tra lại Wifi/4G.", Toast.LENGTH_LONG).show();
            }
        });
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

    private void showMomoCheckoutDialog(String paymentMethod) {
        momoDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_momo_checkout, null);
        momoDialog.setContentView(view);

        TextView tvMomoAmount = view.findViewById(R.id.tvMomoAmount);
        android.widget.Button btnCancelMomo = view.findViewById(R.id.btnCancelMomo);
        android.widget.Button btnConfirmMomo = view.findViewById(R.id.btnConfirmMomo);

        double finalTotal = total - discountVoucher - discountRank - discountStars;
        if (finalTotal < 0) finalTotal = 0;

        if (tvMomoAmount != null) {
            tvMomoAmount.setText(String.format(Locale.getDefault(), "Số tiền: %,.0f đ", finalTotal));
        }

        if (btnCancelMomo != null) {
            btnCancelMomo.setOnClickListener(v -> {
                momoDialog.dismiss();
                Toast.makeText(this, "Hủy thanh toán MoMo", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnConfirmMomo != null) {
            btnConfirmMomo.setOnClickListener(v -> {
                momoDialog.dismiss();
                Toast.makeText(this, "Đang xử lý giao dịch MoMo...", Toast.LENGTH_SHORT).show();
                createBookingOnBackend(paymentMethod);
            });
        }

        momoDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BookingTimerManager.getInstance().registerListener(timerListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BookingTimerManager.getInstance().unregisterListener(timerListener);
    }

    private void releaseLockedSeats() {
        if (seatIds == null || seatIds.isEmpty() || showtimeId == null) return;

        com.example.cinemabooking.data.dto.SeatLockRequestDTO releaseReq =
                new com.example.cinemabooking.data.dto.SeatLockRequestDTO(showtimeId, seatIds);

        com.example.cinemabooking.data.remote.api.SeatApiService seatApi =
                com.example.cinemabooking.data.remote.api.RetrofitClient.getInstance()
                        .create(com.example.cinemabooking.data.remote.api.SeatApiService.class);

        seatApi.releaseSeats(releaseReq).enqueue(new retrofit2.Callback<com.example.cinemabooking.data.dto.ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.cinemabooking.data.dto.ApiResponse<Void>> call, retrofit2.Response<com.example.cinemabooking.data.dto.ApiResponse<Void>> response) {
                android.util.Log.d("BOOKING_FLOW", "Seats released successfully in background");
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.cinemabooking.data.dto.ApiResponse<Void>> call, Throwable t) {
                android.util.Log.e("BOOKING_FLOW", "Failed to release seats: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            BookingTimerManager.getInstance().stopTimer(this);
            if (!isBookingConfirmed) {
                releaseLockedSeats();
            }
        }
    }
}