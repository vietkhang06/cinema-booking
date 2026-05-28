package com.example.cinemabooking.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.AuthActivity;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.AuditLogDTO;
import com.example.cinemabooking.data.remote.api.AuditLogApiService;
import com.example.cinemabooking.data.remote.api.BookingApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.service.InvoiceService;
import com.example.cinemabooking.ui.component.EInvoice.EInvoiceView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffInvoiceActivity extends AuthActivity {

    private InvoiceService invoiceService;
    private InvoiceService.InvoiceDetail invoiceDetail;

    private View backButton;
    private MaterialButton updatePaymentButton, checkinButton, checkSeatButton, checkOrderButton;
    private TextView paymentStatusTV, transactionDateTV, checkinStatusTV;
    private ImageView paymentStatusImg;
    private EInvoiceView eInvoiceView;
    private String invoiceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_invoice);

        invoiceService = ServiceProvider.getInstance().getInvoiceService();

        initViews();
        bindActions();
        retrieveDataFromNavigator();
    }

    private void initViews() {
        eInvoiceView = findViewById(R.id.staff_einvoice);
        backButton = findViewById(R.id.staff_invoice_back_btn);
        updatePaymentButton = findViewById(R.id.staff_invoice_update_payment);
        checkinButton = findViewById(R.id.staff_invoice_checkin_btn);
        paymentStatusTV = findViewById(R.id.staff_invoice_payment_status_tv);
        transactionDateTV = findViewById(R.id.staff_invoice_transaction_date);
        checkinStatusTV = findViewById(R.id.staff_invoice_checkin_status_tv);
        paymentStatusImg = findViewById(R.id.staff_invoice_payment_status_img);
        checkSeatButton = findViewById(R.id.staff_invoice_check_seats);
        checkOrderButton = findViewById(R.id.staff_invoice_check_order);
    }

    private void bindActions() {
        backButton.setOnClickListener(v -> finish());

        checkOrderButton.setOnClickListener(v -> {
            if (invoiceDetail != null && invoiceDetail.booking != null) {
                Intent intent = new Intent(this, StaffCheckOrder.class);
                intent.putExtra("invoiceId", invoiceDetail.booking.bookingId);
                startActivity(intent);
            }
        });

        checkSeatButton.setOnClickListener(v -> {
            if (invoiceDetail != null && invoiceDetail.booking != null) {
                Intent intent = new Intent(this, StaffCheckSeat.class);
                intent.putExtra("invoiceId", invoiceDetail.booking.bookingId);
                intent.putExtra("showtimeId", invoiceDetail.booking.showtimeId);
                startActivity(intent);
            }
        });

        updatePaymentButton.setOnClickListener(v -> {
            if (invoiceDetail == null || invoiceDetail.booking == null) return;
            showLoading(true);
            BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
            bookingApi.confirmPayment(invoiceDetail.booking.bookingId).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful()) {
                        writeAuditLog("CONFIRM_PAYMENT", "Confirmed payment at counter");
                        showToast("Đã xác nhận thanh toán");
                        retrieveDataFromNavigator();
                    } else {
                        showLoading(false);
                        showToast("Không thể cập nhật thanh toán: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    showLoading(false);
                    showToast("Lỗi: " + t.getMessage());
                }
            });
        });

        checkinButton.setOnClickListener(v -> {
            if (invoiceDetail == null || invoiceDetail.booking == null) return;
            showLoading(true);
            BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
            bookingApi.checkInBooking(invoiceDetail.booking.bookingId).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful()) {
                        writeAuditLog("CHECKIN", "Successfully check-in customer");
                        showToast("Check-in thành công");
                        retrieveDataFromNavigator();
                    } else {
                        showLoading(false);
                        showToast("Check-in thất bại: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    showLoading(false);
                    showToast("Lỗi: " + t.getMessage());
                }
            });
        });
    }

    private void retrieveDataFromNavigator() {
        Intent intent = getIntent();
        invoiceId = intent.getStringExtra("invoiceId");

        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            showToast("Hóa đơn không hợp lệ");
            finish();
            return;
        }

        showLoading(true);
        invoiceService.getInvoiceFromId(invoiceId, new ResultCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                if (booking == null) {
                    showLoading(false);
                    showToast("Không tìm thấy hóa đơn: " + invoiceId);
                    finish();
                    return;
                }

                Executors.newSingleThreadExecutor().execute(() -> {
                    invoiceDetail = invoiceService.getInvoiceDetail(invoiceId);
                    runOnUiThread(() -> {
                        showLoading(false);
                        if (invoiceDetail != null) {
                            bindDataView(invoiceDetail);
                        } else {
                            showToast("Lỗi tải chi tiết hóa đơn");
                        }
                    });
                });
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showToast("Lỗi: " + message);
            }
        });
    }

    private void bindDataView(InvoiceService.InvoiceDetail detail) {
        eInvoiceView.setInvoiceDetail(detail);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        transactionDateTV.setText("Ngày tạo: " + sdf.format(new Date(detail.booking.createdAt)));

        String payStatus = detail.booking.paymentStatus;
        if ("confirmed".equalsIgnoreCase(payStatus) || "paid".equalsIgnoreCase(payStatus)) {
            paymentStatusTV.setText("ĐÃ THANH TOÁN");
            paymentStatusTV.setTextColor(0xFF4CAF50);
            updatePaymentButton.setVisibility(View.GONE);
            checkinButton.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.ic_confirm).into(paymentStatusImg);

            if (detail.booking.checkInAt > 0) {
                checkinButton.setEnabled(false);
                checkinButton.setText("Đã Check-in");
                checkinStatusTV.setText("Đã Check-in lúc: " + sdf.format(new Date(detail.booking.checkInAt)));
                checkinStatusTV.setTextColor(0xFF4CAF50);
            } else {
                checkinButton.setEnabled(true);
                checkinButton.setText("Check-in Vé");
                checkinStatusTV.setText("Trạng thái: Chưa Check-in");
                checkinStatusTV.setTextColor(0xFFE53935);
            }
        } else {
            paymentStatusTV.setText("CHỜ THANH TOÁN");
            paymentStatusTV.setTextColor(0xFFFF9800);
            updatePaymentButton.setVisibility(View.VISIBLE);
            checkinButton.setVisibility(View.GONE);
            Glide.with(this).load(R.drawable.ic_cross_circle).into(paymentStatusImg);
            checkinStatusTV.setText("Vé chưa thanh toán, không thể check-in");
            checkinStatusTV.setTextColor(0xFFE53935);
        }
    }

    private void writeAuditLog(String action, String note) {
        AuditLogDTO log = new AuditLogDTO();
        log.actorId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "unknown_staff";
        log.actorRole = "staff";
        log.action = action;
        log.targetType = "booking";
        log.targetId = invoiceId;
        log.note = note;
        log.createdAt = System.currentTimeMillis();

        AuditLogApiService auditLogApi = RetrofitClient.getInstance().create(AuditLogApiService.class);
        auditLogApi.createAuditLog(log).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {}
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
        });
    }
}