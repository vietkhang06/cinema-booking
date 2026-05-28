package com.example.cinemabooking.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffSearchBookingActivity extends AuthActivity {

    private TextInputEditText searchInput;
    private MaterialButton btnSearch;
    private RecyclerView resultsRv;
    private TextView tvNoResults;
    private View backBtn;
    private BookingAdapter adapter;
    private List<BookingDTO> bookingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_search_booking);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
    }

    private void initViews() {
        searchInput = findViewById(R.id.search_input);
        btnSearch = findViewById(R.id.btn_search);
        resultsRv = findViewById(R.id.search_results_rv);
        tvNoResults = findViewById(R.id.tv_no_results);
        backBtn = findViewById(R.id.back_btn);

        resultsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(bookingList);
        resultsRv.setAdapter(adapter);
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> doSearch());
    }

    private void doSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            showToast("Vui lòng nhập từ khóa tìm kiếm");
            return;
        }

        showLoading(true);
        BookingApiService bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
        bookingApi.searchBookings(query).enqueue(new Callback<ApiResponse<List<BookingDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingDTO>>> call, Response<ApiResponse<List<BookingDTO>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bookingList.clear();
                    bookingList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();

                    if (bookingList.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                        resultsRv.setVisibility(View.GONE);
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                        resultsRv.setVisibility(View.VISIBLE);
                    }
                } else {
                    showToast("Không tìm thấy kết quả hoặc lỗi kết nối");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingDTO>>> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi: " + t.getMessage());
            }
        });
    }

    private class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
        private List<BookingDTO> items;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public BookingAdapter(List<BookingDTO> items) {
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

            // Status design
            String status = dto.paymentStatus != null ? dto.paymentStatus.toUpperCase(Locale.getDefault()) : "PENDING";
            holder.tvStatus.setText(status);
            if ("CONFIRMED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
                holder.tvStatus.setBackgroundColor(0xFF4CAF50); // Green
            } else if ("PENDING".equalsIgnoreCase(status)) {
                holder.tvStatus.setBackgroundColor(0xFFFF9800); // Orange
            } else {
                holder.tvStatus.setBackgroundColor(0xFFE53935); // Red
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(StaffSearchBookingActivity.this, StaffInvoiceActivity.class);
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
