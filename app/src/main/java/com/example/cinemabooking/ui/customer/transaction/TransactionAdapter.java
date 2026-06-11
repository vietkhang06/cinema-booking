package com.example.cinemabooking.ui.customer.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.domain.model.Booking;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<Booking> bookings = new ArrayList<>();
    private final OnItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(Booking booking);
    }

    public TransactionAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> newBookings) {
        this.bookings.clear();
        if (newBookings != null) {
            this.bookings.addAll(newBookings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle, tvCinema, tvShowtime, tvPrice, tvStatus;
        MaterialCardView cardStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_movie_poster);
            tvTitle = itemView.findViewById(R.id.tv_movie_title);
            tvCinema = itemView.findViewById(R.id.tv_cinema_name);
            tvShowtime = itemView.findViewById(R.id.tv_showtime);
            tvPrice = itemView.findViewById(R.id.tv_total_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            cardStatus = itemView.findViewById(R.id.card_status);
        }

        public void bind(Booking booking) {
            tvTitle.setText(booking.movieTitleSnapshot);
            
            // Display Cinema, Room and Seats information
            if (booking.showtimeId != null) {
                String room = booking.roomNameSnapshot != null ? booking.roomNameSnapshot : "Chưa xác định";
                String seats = booking.seatCodes != null ? android.text.TextUtils.join(", ", booking.seatCodes) : "Chưa xác định";
                tvCinema.setText(booking.cinemaNameSnapshot + " - " + room + "\nGhế: " + seats);
            } else {
                tvCinema.setText(booking.cinemaNameSnapshot + " - " + booking.roomNameSnapshot);
            }
            
            if (booking.showtimeStartAtSnapshot > 0) {
                tvShowtime.setText(dateFormat.format(new Date(booking.showtimeStartAtSnapshot)));
            } else if (booking.createdAt > 0) {
                tvShowtime.setText(dateFormat.format(new Date(booking.createdAt)));
            } else {
                tvShowtime.setText("Chưa xác định");
            }

            tvPrice.setText(String.format("%,.0fđ", booking.total).replace(',', '.'));

            long now = System.currentTimeMillis();
            boolean isExpired = (booking.showtimeStartAtSnapshot > 0) && (now > booking.showtimeStartAtSnapshot);
            boolean isUsed = booking.checkInAt > 0;
            String status = booking.bookingStatus != null ? booking.bookingStatus.toLowerCase() : "unknown";
            boolean isCancelled = "cancelled".equals(status) || "failed".equals(status);

            // Status Badge Logic
            if (isCancelled) {
                tvStatus.setText("Đã hủy");
                tvStatus.setTextColor(0xFFC62828);
                cardStatus.setCardBackgroundColor(0xFFFFEBEE);
            } else if (isUsed) {
                tvStatus.setText("ĐÃ DÙNG");
                tvStatus.setTextColor(0xFF0288D1);
                cardStatus.setCardBackgroundColor(0xFFE1F5FE);
            } else if (isExpired && ("confirmed".equals(status) || "success".equals(status))) {
                tvStatus.setText("Hết hạn");
                tvStatus.setTextColor(0xFF757575);
                cardStatus.setCardBackgroundColor(0xFFEEEEEE);
            } else {
                switch (status) {
                    case "confirmed":
                    case "success":
                        tvStatus.setText("Thành công");
                        tvStatus.setTextColor(0xFF2E7D32);
                        cardStatus.setCardBackgroundColor(0xFFE8F5E9);
                        break;
                    case "pending":
                        tvStatus.setText("Đang chờ");
                        tvStatus.setTextColor(0xFFE8640C);
                        cardStatus.setCardBackgroundColor(0xFFFFF3E0);
                        break;
                    default:
                        tvStatus.setText("Khác");
                        tvStatus.setTextColor(0xFF757575);
                        cardStatus.setCardBackgroundColor(0xFFF5F5F5);
                        break;
                }
            }

            // Expiration and Invalidity UI handling
            boolean isInvalid = isExpired || isUsed || isCancelled;
            if (isInvalid) {
                itemView.setAlpha(0.5f);
                itemView.setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "Vé này đã dùng, đã hết hạn hoặc đã bị hủy và không thể dùng mã QR nữa!", Toast.LENGTH_SHORT).show();
                });
            } else {
                itemView.setAlpha(1.0f);
                itemView.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(bookings.get(pos));
                    }
                });
            }
            
            Glide.with(itemView.getContext())
                    .load(!android.text.TextUtils.isEmpty(booking.movieImageUrlSnapshot) ? booking.movieImageUrlSnapshot : R.drawable.square_solid_full)
                    .placeholder(R.drawable.square_solid_full)
                    .into(imgPoster);
        }
    }
}
