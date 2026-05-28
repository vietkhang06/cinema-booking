package com.example.cinemabooking.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.AuthActivity;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.BookingDTO;
import com.example.cinemabooking.data.remote.api.BookingApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffPaymentSupportActivity extends AuthActivity {

    private RecyclerView pendingPaymentsRv;
    private TextView tvNoResults;
    private View backBtn;
    private PaymentAdapter adapter;
    private List<BookingDTO> pendingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_payment_support);

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
        loadPendingPayments();
    }

    private void initViews() {
        pendingPaymentsRv = findViewById(R.id.pending_payments_rv);
        tvNoResults = findViewById(R.id.tv_no_results);
        backBtn = findViewById(R.id.back_btn);

        pendingPaymentsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentAdapter(pendingList);
        pendingPaymentsRv.setAdapter(adapter);
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadPendingPayments() {
        showLoading(true);
        BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
        // query = "" gets all bookings, then filter client-side
        bookingApi.searchBookings("").enqueue(new Callback<ApiResponse<List<BookingDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingDTO>>> call, Response<ApiResponse<List<BookingDTO>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    pendingList.clear();
                    for (BookingDTO b : response.body().getData()) {
                        if ("PENDING".equalsIgnoreCase(b.paymentStatus)) {
                            pendingList.add(b);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (pendingList.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        pendingPaymentsRv.setVisibility(View.GONE);
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                        pendingPaymentsRv.setVisibility(View.VISIBLE);
                    }
                } else {
                    showToast("Không thể tải danh sách thanh toán");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingDTO>>> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi: " + t.getMessage());
            }
        });
    }

    private class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
        private List<BookingDTO> items;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public PaymentAdapter(List<BookingDTO> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_booking_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BookingDTO dto = items.get(position);
            holder.tvMovieTitle.setText(dto.movieTitleSnapshot);
            holder.tvBookingId.setText("Mã vé: " + dto.bookingId);
            holder.tvCinemaRoom.setText(dto.cinemaNameSnapshot + " - " + dto.roomNameSnapshot);

            String formattedTime = dateFormat.format(new Date(dto.showtimeStartAtSnapshot));
            holder.tvShowtime.setText("Suất chiếu: " + formattedTime);

            String seats = dto.seatCodes != null ? String.join(", ", dto.seatCodes) : "";
            holder.tvSeats.setText("Ghế: " + seats);

            holder.tvTotalPrice.setText(String.format("%,.0f vnd", dto.total));

            // Load poster image
            if (dto.movieImageUrlSnapshot != null && !dto.movieImageUrlSnapshot.isEmpty()) {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(dto.movieImageUrlSnapshot)
                        .placeholder(R.drawable.login_icon)
                        .into(holder.imgPoster);
            } else {
                holder.imgPoster.setImageResource(R.drawable.login_icon);
            }

            holder.tvStatus.setText("PENDING");
            holder.tvStatus.setBackgroundColor(0xFFFF9800); // Orange

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(StaffPaymentSupportActivity.this, StaffInvoiceActivity.class);
                intent.putExtra("invoiceId", dto.bookingId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMovieTitle, tvStatus, tvBookingId, tvCinemaRoom, tvShowtime, tvSeats, tvTotalPrice;
            android.widget.ImageView imgPoster;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
                tvStatus = itemView.findViewById(R.id.tv_status);
                tvBookingId = itemView.findViewById(R.id.tv_booking_id);
                tvCinemaRoom = itemView.findViewById(R.id.tv_cinema_room);
                tvShowtime = itemView.findViewById(R.id.tv_showtime);
                tvSeats = itemView.findViewById(R.id.tv_seats);
                tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
                imgPoster = itemView.findViewById(R.id.img_poster);
            }
        }
    }
}
