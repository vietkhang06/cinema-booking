package com.example.cinemabooking.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.AuthActivity;
import com.example.cinemabooking.core.navigation.AppNavigator;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.StaffStatsDTO;
import com.example.cinemabooking.data.remote.api.BookingApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.example.cinemabooking.data.remote.api.ShowtimeApiService;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Showtime;
import com.example.cinemabooking.domain.model.User;
import com.google.android.material.card.MaterialCardView;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffDashboardActivity extends AuthActivity {

    private TextView tvWelcome;
    private MaterialCardView btnStaffProfile;
    private ImageView ivStaffAvatar;
    private TextView tvStatTotal, tvStatPaid, tvStatPending, tvStatCancelled, tvStatUpcoming, tvStatHeldSeats;
    private MaterialCardView checkInvoiceButton, searchBookingButton, seatSupportButton, paymentSupportButton, showtimesButton, auditLogButton;
    private com.google.firebase.firestore.ListenerRegistration heldSeatsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfileAndCheckRole();
        loadStats();
        loadUpcomingShowtimesCount();
        startHeldSeatsListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopHeldSeatsListener();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        btnStaffProfile = findViewById(R.id.btn_staff_profile);
        ivStaffAvatar = findViewById(R.id.iv_staff_avatar);

        tvStatTotal = findViewById(R.id.tv_stat_total);
        tvStatPaid = findViewById(R.id.tv_stat_paid);
        tvStatPending = findViewById(R.id.tv_stat_pending);
        tvStatCancelled = findViewById(R.id.tv_stat_cancelled);
        tvStatUpcoming = findViewById(R.id.tv_stat_upcoming);
        tvStatHeldSeats = findViewById(R.id.tv_stat_held_seats);

        checkInvoiceButton = findViewById(R.id.staff_db_check_invoice);
        searchBookingButton = findViewById(R.id.staff_db_search_booking);
        seatSupportButton = findViewById(R.id.staff_db_seat_support);
        paymentSupportButton = findViewById(R.id.staff_db_payment_support);
        showtimesButton = findViewById(R.id.staff_db_showtimes);
        auditLogButton = findViewById(R.id.staff_db_audit_log);
    }

    private void bindActions() {
        btnStaffProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, StaffProfileActivity.class));
        });
        checkInvoiceButton.setOnClickListener(v -> {
            startActivity(new Intent(this, StaffScanQRActivity.class));
        });
        searchBookingButton.setOnClickListener(v -> {
            startActivity(new Intent(this, StaffSearchBookingActivity.class));
        });
        seatSupportButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffShowtimesActivity.class);
            intent.putExtra("mode", "seat_support");
            startActivity(intent);
        });
        paymentSupportButton.setOnClickListener(v -> {
            startActivity(new Intent(this, StaffPaymentSupportActivity.class));
        });
        showtimesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffShowtimesActivity.class);
            intent.putExtra("mode", "showtimes");
            startActivity(intent);
        });
        auditLogButton.setOnClickListener(v -> {
            startActivity(new Intent(this, StaffAuditLogActivity.class));
        });
    }

    private void loadUserProfileAndCheckRole() {
        ServiceProvider.getInstance().getAuthenticationService().getCurrentAuthUser(new ResultCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    AppNavigator.goToLogin(StaffDashboardActivity.this);
                    return;
                }

                // Check role: must be staff or admin
                String role = user.role == null ? "" : user.role.trim().toLowerCase();
                if (!"staff".equals(role) && !"admin".equals(role)) {
                    showToast("Tài khoản không có quyền truy cập module Staff");
                    AppNavigator.goToHomeByRole(StaffDashboardActivity.this, user.role);
                    return;
                }

                // Set welcome greeting
                String displayName = (user.name != null && !user.name.isEmpty()) ? user.name : user.email;
                tvWelcome.setText("Xin chào, " + displayName);

                // Load avatar if present
                if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                    com.bumptech.glide.Glide.with(StaffDashboardActivity.this)
                            .load(user.avatarUrl)
                            .placeholder(R.drawable.user_solid_full)
                            .into(ivStaffAvatar);
                }
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải thông tin tài khoản: " + message);
            }
        });
    }

    private void loadStats() {
        BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
        bookingApi.getStaffStats().enqueue(new Callback<ApiResponse<StaffStatsDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaffStatsDTO>> call, Response<ApiResponse<StaffStatsDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    StaffStatsDTO stats = response.body().getData();
                    tvStatTotal.setText(String.valueOf(stats.totalBookingsToday));
                    tvStatPaid.setText(String.valueOf(stats.paidBookingsToday));
                    tvStatPending.setText(String.valueOf(stats.pendingBookingsToday));
                    tvStatCancelled.setText(String.valueOf(stats.cancelledBookingsToday + stats.failedBookingsToday));
                } else {
                    setStatsFallback();
                    if (response.code() == 403) {
                        showToast("Tài khoản không có quyền truy cập thống kê (403 Forbidden)");
                    } else {
                        showToast("Không thể tải thống kê (" + response.code() + ")");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StaffStatsDTO>> call, Throwable t) {
                setStatsFallback();
                showToast("Lỗi kết nối thống kê: " + t.getMessage());
            }
        });
    }

    private void setStatsFallback() {
        tvStatTotal.setText("--");
        tvStatPaid.setText("--");
        tvStatPending.setText("--");
        tvStatCancelled.setText("--");
    }

    private void loadUpcomingShowtimesCount() {
        ShowtimeApiService showtimeApi = RetrofitClient.getInstance().create(ShowtimeApiService.class);
        showtimeApi.getAllShowtimes().enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call, Response<ApiResponse<List<Showtime>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Showtime> list = response.body().getData();
                    long now = System.currentTimeMillis();
                    int count = 0;
                    for (Showtime s : list) {
                        if (s.startAt >= now && !s.deleted) {
                            count++;
                        }
                    }
                    tvStatUpcoming.setText(String.valueOf(count));
                } else {
                    tvStatUpcoming.setText("--");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                tvStatUpcoming.setText("--");
            }
        });
    }

    private void startHeldSeatsListener() {
        if (heldSeatsListener != null) {
            heldSeatsListener.remove();
            heldSeatsListener = null;
        }
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        heldSeatsListener = db.collection("seats")
                .whereEqualTo("status", "held")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        tvStatHeldSeats.setText("--");
                        return;
                    }
                    if (snapshots != null) {
                        long now = System.currentTimeMillis();
                        int count = 0;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            Long heldUntil = doc.getLong("heldUntil");
                            if (heldUntil != null && heldUntil > now) {
                                count++;
                            }
                        }
                        tvStatHeldSeats.setText(String.valueOf(count));
                    } else {
                        tvStatHeldSeats.setText("0");
                    }
                });
    }

    private void stopHeldSeatsListener() {
        if (heldSeatsListener != null) {
            heldSeatsListener.remove();
            heldSeatsListener = null;
        }
    }

    void openGoogleScanner() {
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, options);

        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String rawValue = barcode.getRawValue();
                    if (rawValue != null && !rawValue.trim().isEmpty()) {
                        Intent intent = new Intent(this, StaffInvoiceActivity.class);
                        intent.putExtra("invoiceId", rawValue);
                        startActivity(intent);
                    } else {
                        showToast("Mã QR rỗng");
                    }
                })
                .addOnCanceledListener(() -> {
                })
                .addOnFailureListener(e -> {
                    showToast("Quét thất bại: " + e.getMessage());
                });
    }
}