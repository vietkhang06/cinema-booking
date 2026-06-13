package com.example.cinemabooking.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.ui.admin.adapter.AdminPaymentAdapter;
import com.example.cinemabooking.ui.admin.model.AdminPayment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminPaymentListActivity extends BaseActivity implements AdminPaymentAdapter.OnPaymentClickListener {

    private static final String TAG = "AdminPaymentList";

    private MaterialCardView tabPending;
    private MaterialCardView tabSuccess;
    private MaterialCardView tabFailed;
    private TextView tvTabPending;
    private TextView tvTabSuccess;
    private TextView tvTabFailed;

    private RecyclerView rvPayments;
    private LinearLayout layoutEmpty;

    private AdminPaymentAdapter adapter;
    private FirebaseFirestore db;
    private ListenerRegistration paymentListener;

    private final List<AdminPayment> allPayments = new ArrayList<>();
    private final List<AdminPayment> filteredPayments = new ArrayList<>();
    private String currentTab = "PENDING"; // PENDING, SUCCESS, FAILED

    private final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("HH:mm - EEEE, dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payment_list);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupList();
        setupTabs();
        startPaymentListener();
    }

    private void initViews() {
        tabPending = findViewById(R.id.tabPending);
        tabSuccess = findViewById(R.id.tabSuccess);
        tabFailed = findViewById(R.id.tabFailed);
        tvTabPending = findViewById(R.id.tvTabPending);
        tvTabSuccess = findViewById(R.id.tvTabSuccess);
        tvTabFailed = findViewById(R.id.tvTabFailed);

        rvPayments = findViewById(R.id.rvPayments);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupList() {
        adapter = new AdminPaymentAdapter(this);
        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        rvPayments.setAdapter(adapter);
    }

    private void setupTabs() {
        tabPending.setOnClickListener(v -> selectTab("PENDING"));
        tabSuccess.setOnClickListener(v -> selectTab("SUCCESS"));
        tabFailed.setOnClickListener(v -> selectTab("FAILED"));

        // Set default style
        updateTabStyles();
    }

    private void selectTab(String tab) {
        if (!currentTab.equals(tab)) {
            currentTab = tab;
            updateTabStyles();
            filterPayments();
        }
    }

    private void updateTabStyles() {
        // Reset all tabs to inactive state
        tabPending.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        tabPending.setStrokeColor(Color.parseColor("#ECE8F0"));
        tvTabPending.setTextColor(Color.parseColor("#7A757F"));

        tabSuccess.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        tabSuccess.setStrokeColor(Color.parseColor("#ECE8F0"));
        tvTabSuccess.setTextColor(Color.parseColor("#7A757F"));

        tabFailed.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        tabFailed.setStrokeColor(Color.parseColor("#ECE8F0"));
        tvTabFailed.setTextColor(Color.parseColor("#7A757F"));

        // Highlight the selected tab
        if ("PENDING".equals(currentTab)) {
            tabPending.setCardBackgroundColor(Color.parseColor("#1E1A23"));
            tabPending.setStrokeColor(Color.parseColor("#1E1A23"));
            tvTabPending.setTextColor(Color.parseColor("#FFFFFF"));
        } else if ("SUCCESS".equals(currentTab)) {
            tabSuccess.setCardBackgroundColor(Color.parseColor("#1E1A23"));
            tabSuccess.setStrokeColor(Color.parseColor("#1E1A23"));
            tvTabSuccess.setTextColor(Color.parseColor("#FFFFFF"));
        } else if ("FAILED".equals(currentTab)) {
            tabFailed.setCardBackgroundColor(Color.parseColor("#1E1A23"));
            tabFailed.setStrokeColor(Color.parseColor("#1E1A23"));
            tvTabFailed.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

    private void startPaymentListener() {
        showLoading(true);
        paymentListener = db.collection("payments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    showLoading(false);
                    if (e != null) {
                        Log.e(TAG, "SnapshotListener failed", e);
                        showToast("Lỗi tải dữ liệu real-time.");
                        return;
                    }
                    if (snapshots != null) {
                        allPayments.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            AdminPayment payment = doc.toObject(AdminPayment.class);
                            if (payment != null) {
                                // Ensure paymentId is populated from the document ID if not present in the model
                                if (payment.paymentId == null) {
                                    payment.paymentId = doc.getId();
                                }
                                allPayments.add(payment);
                            }
                        }
                        filterPayments();
                    }
                });
    }

    private void filterPayments() {
        filteredPayments.clear();
        for (AdminPayment p : allPayments) {
            String status = p.status != null ? p.status.toUpperCase() : "PENDING";
            if ("PENDING".equals(currentTab)) {
                if ("PENDING".equals(status) || "WAITING_CONFIRMATION".equals(status)) {
                    filteredPayments.add(p);
                }
            } else if ("SUCCESS".equals(currentTab)) {
                if ("SUCCESS".equals(status) || "PAID".equals(status)) {
                    filteredPayments.add(p);
                }
            } else if ("FAILED".equals(currentTab)) {
                if ("FAILED".equals(status) || "CANCELLED".equals(status)) {
                    filteredPayments.add(p);
                }
            }
        }

        adapter.setPayments(filteredPayments);

        if (filteredPayments.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvPayments.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvPayments.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPaymentClick(AdminPayment payment) {
        if (payment == null || payment.bookingId == null) return;

        showLoading(true);
        db.collection("bookings").document(payment.bookingId).get()
                .addOnSuccessListener(docSnapshot -> {
                    if (docSnapshot.exists()) {
                        showLoading(false);
                        Booking booking = docSnapshot.toObject(Booking.class);
                        if (booking != null) {
                            if (booking.bookingId == null) {
                                booking.bookingId = docSnapshot.getId();
                            }
                            showDetailBottomSheet(payment, booking);
                        } else {
                            showToast("Lỗi định dạng dữ liệu vé.");
                        }
                    } else {
                        // Fallback: Check in cine_shop_orders
                        db.collection("cine_shop_orders").document(payment.bookingId).get()
                                .addOnSuccessListener(shopDoc -> {
                                    showLoading(false);
                                    if (shopDoc.exists()) {
                                        Booking booking = new Booking();
                                        booking.bookingId = shopDoc.getId();
                                        booking.userId = shopDoc.getString("userId");
                                        booking.showtimeId = null; // CineShop indicator
                                        booking.movieTitleSnapshot = shopDoc.getString("itemName");
                                        booking.movieImageUrlSnapshot = shopDoc.getString("itemImageUrl");

                                        String pMethod = shopDoc.getString("paymentMethod");
                                        booking.cinemaNameSnapshot = "CineShop - Nhận tại rạp (" + (pMethod != null ? pMethod.toUpperCase() : "ZALOPAY") + ")";

                                        Long qtyObj = shopDoc.getLong("quantity");
                                        int qty = qtyObj != null ? qtyObj.intValue() : 1;
                                        booking.roomNameSnapshot = "Số lượng: " + qty;
                                        booking.showtimeStartAtSnapshot = 0L;

                                        Double priceObj = shopDoc.getDouble("totalPrice");
                                        double price = priceObj != null ? priceObj : 0.0;
                                        booking.total = price;
                                        booking.subtotal = price;

                                        booking.bookingStatus = shopDoc.getString("status");
                                        booking.paymentStatus = shopDoc.getString("status");
                                        booking.paymentCode = payment.paymentCode;

                                        showDetailBottomSheet(payment, booking);
                                    } else {
                                        showToast("Không tìm thấy thông tin đặt vé/đơn hàng cho giao dịch này.");
                                    }
                                })
                                .addOnFailureListener(err -> {
                                    showLoading(false);
                                    Log.e(TAG, "Error fetching shop order detail", err);
                                    showToast("Không thể tải thông tin đơn hàng.");
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error fetching booking detail", e);
                    showToast("Không thể tải thông tin đặt vé.");
                });
    }

    private void showDetailBottomSheet(AdminPayment payment, Booking booking) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_payment_detail, null);
        dialog.setContentView(view);

        // Bind data
        TextView tvDetailAmount = view.findViewById(R.id.tvDetailAmount);
        TextView tvDetailMethod = view.findViewById(R.id.tvDetailMethod);
        TextView tvDetailPaymentCode = view.findViewById(R.id.tvDetailPaymentCode);
        TextView tvDetailCustomerId = view.findViewById(R.id.tvDetailCustomerId);
        TextView tvDetailMovie = view.findViewById(R.id.tvDetailMovie);
        TextView tvDetailCinema = view.findViewById(R.id.tvDetailCinema);
        TextView tvDetailShowtime = view.findViewById(R.id.tvDetailShowtime);
        TextView tvDetailSeats = view.findViewById(R.id.tvDetailSeats);

        LinearLayout layoutActionButtons = view.findViewById(R.id.layoutActionButtons);
        MaterialButton btnApprovePayment = view.findViewById(R.id.btnApprovePayment);
        MaterialButton btnRejectPayment = view.findViewById(R.id.btnRejectPayment);

        tvDetailAmount.setText(currencyFormatter.format(payment.amount) + " đ");
        tvDetailPaymentCode.setText(payment.paymentCode != null ? payment.paymentCode : "—");
        tvDetailCustomerId.setText(booking.userId != null ? booking.userId : "Khách vãng lai");
        tvDetailMovie.setText(booking.movieTitleSnapshot != null ? booking.movieTitleSnapshot : "—");

        String cinemaInfo = (booking.cinemaNameSnapshot != null ? booking.cinemaNameSnapshot : "")
                + (booking.roomNameSnapshot != null ? " - " + booking.roomNameSnapshot : "");
        tvDetailCinema.setText(cinemaInfo.trim().isEmpty() ? "—" : cinemaInfo);

        if (booking.showtimeStartAtSnapshot > 0) {
            tvDetailShowtime.setText(dateTimeFormatter.format(new Date(booking.showtimeStartAtSnapshot)));
        } else {
            tvDetailShowtime.setText("—");
        }

        if (booking.seatCodes != null && !booking.seatCodes.isEmpty()) {
            tvDetailSeats.setText(TextUtils.join(", ", booking.seatCodes));
        } else {
            tvDetailSeats.setText("—");
        }

        if ("momo".equalsIgnoreCase(payment.provider)) {
            tvDetailMethod.setText("Ví điện tử MoMo");
        } else {
            tvDetailMethod.setText("Chuyển khoản Ngân hàng (MBBank)");
        }

        // Hide action buttons if the transaction is not pending
        String status = payment.status != null ? payment.status.toUpperCase() : "PENDING";
        if (!"PENDING".equals(status) && !"WAITING_CONFIRMATION".equals(status)) {
            layoutActionButtons.setVisibility(View.GONE);
        } else {
            layoutActionButtons.setVisibility(View.VISIBLE);

            btnApprovePayment.setOnClickListener(v -> showConfirmApproval(payment, booking, dialog));
            btnRejectPayment.setOnClickListener(v -> showConfirmRejection(payment, booking, dialog));
        }

        dialog.show();
    }

    private void showConfirmApproval(AdminPayment payment, Booking booking, BottomSheetDialog detailDialog) {
        new AlertDialog.Builder(this)
                .setTitle("Phê duyệt thanh toán")
                .setMessage("Xác nhận rằng bạn đã nhận được số tiền " + currencyFormatter.format(payment.amount) + " đ cho mã giao dịch " + payment.paymentCode + "? Vé sẽ được xuất ngay sau khi duyệt.")
                .setPositiveButton("Phê duyệt", (dialog, which) -> executeApproval(payment, booking, detailDialog))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showConfirmRejection(AdminPayment payment, Booking booking, BottomSheetDialog detailDialog) {
        new AlertDialog.Builder(this)
                .setTitle("Từ chối thanh toán")
                .setMessage("Bạn có chắc chắn muốn từ chối giao dịch " + payment.paymentCode + "? Trạng thái đặt vé sẽ được cập nhật thành đã hủy.")
                .setPositiveButton("Từ chối", (dialog, which) -> executeRejection(payment, booking, detailDialog))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void executeApproval(AdminPayment payment, Booking booking, BottomSheetDialog detailDialog) {
        showLoading(true);
        com.example.cinemabooking.data.remote.api.BookingApiService bookingApi =
                com.example.cinemabooking.data.remote.api.RetrofitClient.getInstance()
                        .create(com.example.cinemabooking.data.remote.api.BookingApiService.class);
        bookingApi.confirmPayment(booking.bookingId).enqueue(new retrofit2.Callback<com.example.cinemabooking.data.dto.ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.cinemabooking.data.dto.ApiResponse<Void>> call,
                                   retrofit2.Response<com.example.cinemabooking.data.dto.ApiResponse<Void>> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    detailDialog.dismiss();
                    showToast("Phê duyệt thanh toán thành công!");
                } else {
                    String errorMsg = "Duyệt thanh toán thất bại!";
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            com.example.cinemabooking.data.dto.ApiResponse<?> apiError =
                                    new com.google.gson.Gson().fromJson(errorStr, com.example.cinemabooking.data.dto.ApiResponse.class);
                            if (apiError != null && apiError.getMessage() != null) {
                                errorMsg = apiError.getMessage();
                            }
                        }
                    } catch (Exception ignored) {}
                    showToast(errorMsg);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.cinemabooking.data.dto.ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi: " + t.getMessage());
            }
        });
    }

    private void executeRejection(AdminPayment payment, Booking booking, BottomSheetDialog detailDialog) {
        showLoading(true);
        com.example.cinemabooking.data.remote.api.BookingApiService bookingApi =
                com.example.cinemabooking.data.remote.api.RetrofitClient.getInstance()
                        .create(com.example.cinemabooking.data.remote.api.BookingApiService.class);
        bookingApi.cancelBooking(booking.bookingId).enqueue(new retrofit2.Callback<com.example.cinemabooking.data.dto.ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.cinemabooking.data.dto.ApiResponse<Void>> call,
                                   retrofit2.Response<com.example.cinemabooking.data.dto.ApiResponse<Void>> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    detailDialog.dismiss();
                    showToast("Đã từ chối thanh toán giao dịch.");
                } else {
                    String errorMsg = "Từ chối thanh toán thất bại!";
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            com.example.cinemabooking.data.dto.ApiResponse<?> apiError =
                                    new com.google.gson.Gson().fromJson(errorStr, com.example.cinemabooking.data.dto.ApiResponse.class);
                            if (apiError != null && apiError.getMessage() != null) {
                                errorMsg = apiError.getMessage();
                            }
                        }
                    } catch (Exception ignored) {}
                    showToast(errorMsg);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.cinemabooking.data.dto.ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paymentListener != null) {
            paymentListener.remove();
            paymentListener = null;
        }
    }
}
