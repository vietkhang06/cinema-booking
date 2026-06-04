package com.example.cinemabooking.ui.staff;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.AuthActivity;
import com.example.cinemabooking.core.navigation.AppNavigator;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.AuditLogDTO;
import com.example.cinemabooking.data.dto.StaffStatsDTO;
import com.example.cinemabooking.data.remote.api.BookingApiService;
import com.example.cinemabooking.data.remote.api.MovieApiService;
import com.example.cinemabooking.data.remote.api.ProfileApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.example.cinemabooking.data.remote.api.ShowtimeApiService;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.model.Showtime;
import com.example.cinemabooking.domain.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffDashboardActivity extends AuthActivity {

    // Views for Main Container switching
    private View scrollContent;
    private View notificationContent;
    private View profileContent;

    // Header Views
    private TextView tvWelcome;
    private ImageView ivStaffAvatar;
    private MaterialCardView btnStaffProfileHeader;

    // Statistics Views
    private TextView tvStatShowtimes;
    private TextView tvStatUpcoming;
    private TextView tvStatHeldSeats;
    private TextView tvStatIncidents;

    // Quick Actions
    private MaterialCardView btnOpSearch, btnOpSupport, btnOpSeats, btnOpCalendar;

    // Upcoming Showtimes RecyclerView
    private RecyclerView rvUpcomingShowtimes;
    private UpcomingShowtimeAdapter upcomingAdapter;
    private final List<Showtime> upcomingShowtimes = new ArrayList<>();
    private final Map<String, String> movieTitleMap = new HashMap<>();

    // Realtime logs RecyclerViews
    private RecyclerView rvTicketLogs, rvSupportLogs;
    private LogItemAdapter ticketLogsAdapter, supportLogsAdapter;
    private final List<AuditLogDTO> ticketLogsList = new ArrayList<>();
    private final List<AuditLogDTO> supportLogsList = new ArrayList<>();
    private TextView tvEmptyTicketLogs, tvEmptySupportLogs;

    // Profile Tab Views
    private ImageView ivProfileAvatar;
    private TextView tvProfileName, tvProfileRole, tvProfileUid, tvProfileEmail, tvProfilePhone, tvProfileGender, tvProfileBirthdate, tvProfileStatus;
    private MaterialButton btnChangePassword, btnLogout;

    // Bottom Navigation Bar Views
    private LinearLayout bottomNavContainer;
    private MaterialCardView navHomeCard, navScanCard, navNotifCard, navProfileCard;
    private TextView navHomeLabel, navScanLabel, navNotifLabel, navProfileLabel;
    private ImageView navHomeIcon, navScanIcon, navNotifIcon, navProfileIcon;

    // Listeners & State
    private ListenerRegistration heldSeatsListener;
    private ListenerRegistration auditLogsListener;
    private int currentTab = 0; // 0 = Home, 2 = Notif, 3 = Profile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        initViews();
        setupBottomNavigation();
        setupRecyclerViews();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfileAndCheckRole();
        loadStatsAndShowtimes();
        startHeldSeatsListener();
        startAuditLogsListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopHeldSeatsListener();
        stopAuditLogsListener();
    }

    private void initViews() {
        // Layout views
        scrollContent = findViewById(R.id.scrollContent);
        notificationContent = findViewById(R.id.notificationContent);
        profileContent = findViewById(R.id.profileContent);

        // Header
        tvWelcome = findViewById(R.id.tvWelcome);
        ivStaffAvatar = findViewById(R.id.iv_staff_avatar);
        btnStaffProfileHeader = findViewById(R.id.btn_staff_profile_header);

        // Stats
        tvStatShowtimes = findViewById(R.id.tv_stat_showtimes);
        tvStatUpcoming = findViewById(R.id.tv_stat_upcoming);
        tvStatHeldSeats = findViewById(R.id.tv_stat_held_seats);
        tvStatIncidents = findViewById(R.id.tv_stat_incidents);

        // Operations
        btnOpSearch = findViewById(R.id.btn_op_search);
        btnOpSupport = findViewById(R.id.btn_op_support);
        btnOpSeats = findViewById(R.id.btn_op_seats);
        btnOpCalendar = findViewById(R.id.btn_op_calendar);

        // Log empty views
        tvEmptyTicketLogs = findViewById(R.id.tv_empty_ticket_logs);
        tvEmptySupportLogs = findViewById(R.id.tv_empty_support_logs);

        // Profile tab elements
        ivProfileAvatar = findViewById(R.id.iv_avatar);
        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileRole = findViewById(R.id.tv_profile_role);
        tvProfileUid = findViewById(R.id.tv_uid);
        tvProfileEmail = findViewById(R.id.tv_email);
        tvProfilePhone = findViewById(R.id.tv_phone);
        tvProfileGender = findViewById(R.id.tv_gender);
        tvProfileBirthdate = findViewById(R.id.tv_birthdate);
        tvProfileStatus = findViewById(R.id.tv_status);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);

        // Bottom Navigation Bar elements
        bottomNavContainer = findViewById(R.id.bottomNavContainer);
        navHomeCard = findViewById(R.id.navStaffHomeCard);
        navScanCard = findViewById(R.id.navStaffScanCard);
        navNotifCard = findViewById(R.id.navStaffNotifCard);
        navProfileCard = findViewById(R.id.navStaffProfileCard);

        navHomeLabel = findViewById(R.id.navStaffHomeLabel);
        navScanLabel = findViewById(R.id.navStaffScanLabel);
        navNotifLabel = findViewById(R.id.navStaffNotifLabel);
        navProfileLabel = findViewById(R.id.navStaffProfileLabel);

        navHomeIcon = findViewById(R.id.navStaffHomeIcon);
        navScanIcon = findViewById(R.id.navStaffScanIcon);
        navNotifIcon = findViewById(R.id.navStaffNotifIcon);
        navProfileIcon = findViewById(R.id.navStaffProfileIcon);
    }

    private void setupBottomNavigation() {
        navHomeCard.setOnClickListener(v -> switchTab(0));
        navScanCard.setOnClickListener(v -> switchTab(1));
        navNotifCard.setOnClickListener(v -> switchTab(2));
        navProfileCard.setOnClickListener(v -> switchTab(3));

        applyStaffBottomNavState(0);
    }

    private void switchTab(int index) {
        if (index == 1) {
            // Launch the built-in QR scanning activity
            startActivity(new Intent(this, StaffScanQRActivity.class));
            return;
        }

        currentTab = index;
        applyStaffBottomNavState(index);

        scrollContent.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        notificationContent.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        profileContent.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
    }

    private void applyStaffBottomNavState(int index) {
        applyStaffBottomState(navHomeCard, navHomeLabel, navHomeIcon, index == 0, "Tổng quan");
        applyStaffBottomState(navScanCard, navScanLabel, navScanIcon, index == 1, "Quét QR");
        applyStaffBottomState(navNotifCard, navNotifLabel, navNotifIcon, index == 2, "Thông báo");
        applyStaffBottomState(navProfileCard, navProfileLabel, navProfileIcon, index == 3, "Cài đặt");
        if (bottomNavContainer != null) {
            bottomNavContainer.requestLayout();
        }
    }

    private void applyStaffBottomState(MaterialCardView card, TextView label, ImageView icon, boolean selected, String text) {
        LinearLayout.LayoutParams params;
        if (selected) {
            params = new LinearLayout.LayoutParams(0, dp(48), 1.2f);
            params.setMarginStart(dp(4));
            params.setMarginEnd(dp(4));
            card.setCardBackgroundColor(Color.parseColor("#121212")); // Dark pill color as requested
            card.setStrokeWidth(0);
            label.setText(text);
            label.setVisibility(View.VISIBLE);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(Color.WHITE));
            label.setTextColor(Color.WHITE);
            card.animate().scaleX(1.03f).scaleY(1.03f).setDuration(150).start();
        } else {
            params = new LinearLayout.LayoutParams(dp(48), dp(48));
            params.setMarginStart(dp(4));
            params.setMarginEnd(dp(4));
            card.setCardBackgroundColor(Color.WHITE);
            card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#D3CAD7")));
            card.setStrokeWidth(dp(1));
            label.setVisibility(View.GONE);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(Color.parseColor("#4A4650")));
            card.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
        }
        card.setLayoutParams(params);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void setupRecyclerViews() {
        // Upcoming Showtimes Horizontal list
        rvUpcomingShowtimes = findViewById(R.id.rv_upcoming_showtimes);
        rvUpcomingShowtimes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        upcomingAdapter = new UpcomingShowtimeAdapter(upcomingShowtimes);
        rvUpcomingShowtimes.setAdapter(upcomingAdapter);

        // Realtime Ticket Scan Logs
        rvTicketLogs = findViewById(R.id.rv_ticket_logs);
        rvTicketLogs.setLayoutManager(new LinearLayoutManager(this));
        ticketLogsAdapter = new LogItemAdapter(ticketLogsList);
        rvTicketLogs.setAdapter(ticketLogsAdapter);

        // Realtime Customer Support Logs
        rvSupportLogs = findViewById(R.id.rv_support_logs);
        rvSupportLogs.setLayoutManager(new LinearLayoutManager(this));
        supportLogsAdapter = new LogItemAdapter(supportLogsList);
        rvSupportLogs.setAdapter(supportLogsAdapter);
    }

    private void bindActions() {
        btnStaffProfileHeader.setOnClickListener(v -> switchTab(3));

        // Operation button actions
        btnOpSearch.setOnClickListener(v -> startActivity(new Intent(this, StaffSearchBookingActivity.class)));
        btnOpSupport.setOnClickListener(v -> startActivity(new Intent(this, StaffPaymentSupportActivity.class)));
        btnOpSeats.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffShowtimesActivity.class);
            intent.putExtra("mode", "seat_support");
            startActivity(intent);
        });
        btnOpCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffShowtimesActivity.class);
            intent.putExtra("mode", "showtimes");
            startActivity(intent);
        });

        findViewById(R.id.tv_action_view_showtimes).setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffShowtimesActivity.class);
            intent.putExtra("mode", "showtimes");
            startActivity(intent);
        });

        findViewById(R.id.tv_action_view_logs).setOnClickListener(v -> {
            startActivity(new Intent(this, StaffAuditLogActivity.class));
        });

        // Profile tab actions
        btnLogout.setOnClickListener(v -> performLogout());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void loadUserProfileAndCheckRole() {
        ServiceProvider.getInstance().getAuthenticationService().getCurrentAuthUser(new ResultCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    AppNavigator.goToLogin(StaffDashboardActivity.this);
                    return;
                }

                // Check authorization
                String role = user.role == null ? "" : user.role.trim().toLowerCase();
                if (!"staff".equals(role) && !"admin".equals(role)) {
                    showToast("Tài khoản không có quyền truy cập module Staff");
                    AppNavigator.goToHomeByRole(StaffDashboardActivity.this, user.role);
                    return;
                }

                // Set welcome greetings
                String displayName = (user.name != null && !user.name.isEmpty()) ? user.name : user.email;
                tvWelcome.setText("Xin chào, " + displayName + " 👋");

                // Bind to Profile UI Elements
                tvProfileName.setText(displayName);
                tvProfileUid.setText(user.uid);
                tvProfileEmail.setText(user.email);
                tvProfilePhone.setText(user.phone != null && !user.phone.isEmpty() ? user.phone : "Chưa cập nhật");
                tvProfileGender.setText(user.gender != null && !user.gender.isEmpty() ? user.gender : "Chưa cập nhật");
                tvProfileBirthdate.setText(user.birthDate != null && !user.birthDate.isEmpty() ? user.birthDate : "Chưa cập nhật");
                tvProfileStatus.setText(user.status != null && !user.status.isEmpty() ? user.status : "Hoạt động");
                
                if ("admin".equalsIgnoreCase(user.role)) {
                    tvProfileRole.setText("Quản trị viên");
                } else {
                    tvProfileRole.setText("Nhân viên");
                }

                // Load Avatars
                if (user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
                    Glide.with(StaffDashboardActivity.this)
                            .load(user.avatarUrl)
                            .placeholder(R.drawable.user_solid_full)
                            .into(ivStaffAvatar);

                    Glide.with(StaffDashboardActivity.this)
                            .load(user.avatarUrl)
                            .placeholder(R.drawable.user_solid_full)
                            .into(ivProfileAvatar);
                }
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải thông tin tài khoản: " + message);
            }
        });
    }

    private void loadStatsAndShowtimes() {
        // Fetch movies first to map titles
        MovieApiService movieApi = RetrofitClient.getInstance().create(MovieApiService.class);
        movieApi.getAllMovies(0, 100).enqueue(new Callback<ApiResponse<List<Movie>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Movie>>> call, Response<ApiResponse<List<Movie>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    movieTitleMap.clear();
                    for (Movie m : response.body().getData()) {
                        movieTitleMap.put(m.movieId, m.title);
                    }
                }
                fetchShowtimesData();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Movie>>> call, Throwable t) {
                fetchShowtimesData();
            }
        });

        // Set static incident counter or map it to 0
        tvStatIncidents.setText("0");
    }

    private void fetchShowtimesData() {
        ShowtimeApiService showtimeApi = RetrofitClient.getInstance().create(ShowtimeApiService.class);
        showtimeApi.getAllShowtimes().enqueue(new Callback<ApiResponse<List<Showtime>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Showtime>>> call, Response<ApiResponse<List<Showtime>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Showtime> list = response.body().getData();
                    long now = System.currentTimeMillis();
                    long startOfToday = getStartOfDay(now);
                    long endOfToday = startOfToday + 24 * 60 * 60 * 1000 - 1;

                    int totalTodayCount = 0;
                    int upcomingTodayCount = 0;
                    upcomingShowtimes.clear();

                    for (Showtime s : list) {
                        if (s.deleted) continue;

                        if (s.startAt >= startOfToday && s.startAt <= endOfToday) {
                            totalTodayCount++;
                            if (s.startAt >= now) {
                                upcomingTodayCount++;
                                upcomingShowtimes.add(s);
                            }
                        }
                    }

                    // Sort upcoming showtimes by start time ascending
                    upcomingShowtimes.sort((s1, s2) -> Long.compare(s1.startAt, s2.startAt));

                    tvStatShowtimes.setText(String.valueOf(totalTodayCount));
                    tvStatUpcoming.setText(String.valueOf(upcomingTodayCount));
                    upcomingAdapter.notifyDataSetChanged();
                } else {
                    tvStatShowtimes.setText("--");
                    tvStatUpcoming.setText("--");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Showtime>>> call, Throwable t) {
                tvStatShowtimes.setText("--");
                tvStatUpcoming.setText("--");
            }
        });
    }

    private long getStartOfDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void startHeldSeatsListener() {
        if (heldSeatsListener != null) {
            heldSeatsListener.remove();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
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

    private void startAuditLogsListener() {
        if (auditLogsListener != null) {
            auditLogsListener.remove();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        auditLogsListener = db.collection("audit_logs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(40)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        tvEmptyTicketLogs.setVisibility(View.VISIBLE);
                        tvEmptySupportLogs.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (snapshots != null) {
                        ticketLogsList.clear();
                        supportLogsList.clear();

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            AuditLogDTO log = doc.toObject(AuditLogDTO.class);
                            if (log == null) continue;

                            String action = log.action != null ? log.action.toUpperCase(Locale.getDefault()) : "";
                            if (action.contains("CHECKIN") || action.contains("SCAN")) {
                                if (ticketLogsList.size() < 3) {
                                    ticketLogsList.add(log);
                                }
                            } else {
                                if (supportLogsList.size() < 3) {
                                    supportLogsList.add(log);
                                }
                            }
                        }

                        ticketLogsAdapter.notifyDataSetChanged();
                        supportLogsAdapter.notifyDataSetChanged();

                        tvEmptyTicketLogs.setVisibility(ticketLogsList.isEmpty() ? View.VISIBLE : View.GONE);
                        tvEmptySupportLogs.setVisibility(supportLogsList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        tvEmptyTicketLogs.setVisibility(View.VISIBLE);
                        tvEmptySupportLogs.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void stopAuditLogsListener() {
        if (auditLogsListener != null) {
            auditLogsListener.remove();
            auditLogsListener = null;
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
                    // Re-select previously active tab
                    applyStaffBottomNavState(currentTab);
                })
                .addOnFailureListener(e -> {
                    applyStaffBottomNavState(currentTab);
                    showToast("Quét thất bại: " + e.getMessage());
                });
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    ServiceProvider.getInstance().getAuthenticationService().logOut();
                    showToast("Đã đăng xuất");
                    AppNavigator.goToLogin(this);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String newPass = etNewPassword.getText().toString();
                    String confirmPass = etConfirmPassword.getText().toString();

                    if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
                        showToast("Mật khẩu mới phải ít nhất 6 ký tự");
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        showToast("Mật khẩu xác nhận không khớp");
                        return;
                    }

                    showLoading(true);
                    ServiceProvider.getInstance().getAuthenticationService().updatePassword(newPass)
                            .addOnSuccessListener(aVoid -> {
                                showLoading(false);
                                showToast("Đổi mật khẩu thành công");
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                showToast("Lỗi: " + e.getMessage());
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ==========================================
    // Inner Adapters to prevent redundant files
    // ==========================================

    private class UpcomingShowtimeAdapter extends RecyclerView.Adapter<UpcomingShowtimeAdapter.ViewHolder> {
        private final List<Showtime> items;

        public UpcomingShowtimeAdapter(List<Showtime> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_dashboard_upcoming_showtime, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Showtime showtime = items.get(position);

            // Start time formatting
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.tvStartTime.setText(timeFormat.format(new Date(showtime.startAt)));

            // Countdown timer
            long now = System.currentTimeMillis();
            long diffMs = showtime.startAt - now;
            long minutes = diffMs / 60000;
            if (minutes > 60) {
                long hours = minutes / 60;
                long mins = minutes % 60;
                holder.tvCountdown.setText("Còn " + hours + "g " + mins + "p");
            } else if (minutes > 0) {
                holder.tvCountdown.setText("Còn " + minutes + " phút");
            } else if (minutes > -120) {
                holder.tvCountdown.setText("Đang chiếu");
                holder.tvCountdown.setTextColor(Color.parseColor("#4CAF50")); // Green for active
            } else {
                holder.tvCountdown.setText("Đã kết thúc");
            }

            String titleStr = movieTitleMap.getOrDefault(showtime.movieId, "Phim");
            holder.tvMovieTitle.setText(titleStr);
            holder.tvRoom.setText("Phòng " + (showtime.roomId != null ? showtime.roomId : ""));
            
            // Format badges e.g. 2D / 3D / IMAX
            String formatStr = (showtime.format != null ? showtime.format : "2D").trim();
            holder.tvFormat.setText(formatStr);
            if (formatStr.toUpperCase().contains("IMAX")) {
                holder.tvFormat.setTextColor(Color.parseColor("#E91E63")); // Pink for IMAX
                holder.tvFormat.setBackgroundColor(Color.parseColor("#FFF0F2"));
            } else {
                holder.tvFormat.setTextColor(Color.parseColor("#3F51B5")); // Blue for standard 2D
                holder.tvFormat.setBackgroundColor(Color.parseColor("#E8EAF6"));
            }

            // Click listener opens seat support check-in mode
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(StaffDashboardActivity.this, StaffShowtimesActivity.class);
                intent.putExtra("mode", "seat_support");
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStartTime, tvCountdown, tvMovieTitle, tvRoom, tvFormat;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStartTime = itemView.findViewById(R.id.tv_showtime_start);
                tvCountdown = itemView.findViewById(R.id.tv_showtime_countdown);
                tvMovieTitle = itemView.findViewById(R.id.tv_showtime_movie_title);
                tvRoom = itemView.findViewById(R.id.tv_showtime_room);
                tvFormat = itemView.findViewById(R.id.tv_showtime_format);
            }
        }
    }

    private class LogItemAdapter extends RecyclerView.Adapter<LogItemAdapter.ViewHolder> {
        private final List<AuditLogDTO> items;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        public LogItemAdapter(List<AuditLogDTO> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_dashboard_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AuditLogDTO log = items.get(position);

            holder.tvTime.setText(timeFormat.format(new Date(log.createdAt)));

            String action = log.action != null ? log.action.toUpperCase(Locale.getDefault()) : "HOẠT ĐỘNG";
            String note = log.note != null ? log.note : "Nhật ký vận hành";

            holder.tvTitle.setText(note);
            holder.tvSubtitle.setText("Hành động: " + action + " (bởi " + log.actorId + ")");

            // Styling colors and icons dynamically based on action types
            if (action.contains("CHECKIN") || action.contains("SCAN")) {
                holder.ivIcon.setImageResource(R.drawable.ic_check);
                ImageViewCompat.setImageTintList(holder.ivIcon, ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.cvIconBg.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light Green
            } else if (action.contains("PAYMENT") || action.contains("CONFIRM")) {
                holder.ivIcon.setImageResource(R.drawable.coins_solid_full);
                ImageViewCompat.setImageTintList(holder.ivIcon, ColorStateList.valueOf(Color.parseColor("#FF9800")));
                holder.cvIconBg.setCardBackgroundColor(Color.parseColor("#FFF3E0")); // Light Orange
            } else if (action.contains("RELEASE") || action.contains("FREE")) {
                holder.ivIcon.setImageResource(R.drawable.couch_solid_normal);
                ImageViewCompat.setImageTintList(holder.ivIcon, ColorStateList.valueOf(Color.parseColor("#E91E63")));
                holder.cvIconBg.setCardBackgroundColor(Color.parseColor("#FFF0F2")); // Light Pink
            } else {
                holder.ivIcon.setImageResource(R.drawable.circle_info_solid_full);
                ImageViewCompat.setImageTintList(holder.ivIcon, ColorStateList.valueOf(Color.parseColor("#2196F3")));
                holder.cvIconBg.setCardBackgroundColor(Color.parseColor("#E3F2FD")); // Light Blue
            }

            // Click goes to audit logs activity for detailed inspection
            holder.itemView.setOnClickListener(v -> {
                startActivity(new Intent(StaffDashboardActivity.this, StaffAuditLogActivity.class));
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cvIconBg;
            ImageView ivIcon;
            TextView tvTitle, tvSubtitle, tvTime;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cvIconBg = itemView.findViewById(R.id.cv_log_icon_bg);
                ivIcon = itemView.findViewById(R.id.iv_log_icon);
                tvTitle = itemView.findViewById(R.id.tv_log_title);
                tvSubtitle = itemView.findViewById(R.id.tv_log_subtitle);
                tvTime = itemView.findViewById(R.id.tv_log_time);
            }
        }
    }
}