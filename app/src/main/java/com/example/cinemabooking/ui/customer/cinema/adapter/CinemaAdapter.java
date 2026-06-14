package com.example.cinemabooking.ui.customer.cinema.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.domain.model.Cinema;

import java.util.ArrayList;
import java.util.List;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.ViewHolder> {

    private final List<Cinema> cinemas = new ArrayList<>();
    private final OnCinemaClickListener listener;

    public interface OnCinemaClickListener {
        void onCinemaClick(Cinema cinema);
    }

    public CinemaAdapter(OnCinemaClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Cinema> items) {
        cinemas.clear();
        if (items != null) {
            cinemas.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cinema, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cinema cinema = cinemas.get(position);
        holder.tvName.setText(valueOrDefault(cinema.name, "Rạp phim"));
        holder.tvAddress.setText(valueOrDefault(cinema.address, "Chưa cập nhật địa chỉ"));
        holder.tvDistrict.setText(valueOrDefault(joinLocation(cinema.district, cinema.city), "Chưa cập nhật khu vực"));
        holder.tvPhone.setText(valueOrDefault(cinema.phone, "Chưa cập nhật hotline"));
        
        // Status Badge styling
        String status = cinema.status;
        String statusText = "Đang hoạt động";
        int textColor = android.graphics.Color.parseColor("#10B981");
        int bgColor = android.graphics.Color.parseColor("#E6FBF3");

        if (status != null && !status.trim().isEmpty()) {
            String cleanStatus = status.trim().toLowerCase();
            if ("active".equals(cleanStatus) || "available".equals(cleanStatus) || "scheduled".equals(cleanStatus) || "đang hoạt động".equals(cleanStatus)) {
                statusText = "Đang hoạt động";
                textColor = android.graphics.Color.parseColor("#10B981");
                bgColor = android.graphics.Color.parseColor("#E6FBF3");
            } else if ("inactive".equals(cleanStatus) || "tạm dừng".equals(cleanStatus)) {
                statusText = "Tạm dừng";
                textColor = android.graphics.Color.parseColor("#EF4444");
                bgColor = android.graphics.Color.parseColor("#FEE2E2");
            } else {
                statusText = status;
                textColor = android.graphics.Color.parseColor("#1E1A23");
                bgColor = android.graphics.Color.parseColor("#F3F4F6");
            }
        }
        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(textColor);
        holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bgColor));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCinemaClick(cinema);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cinemas.size();
    }

    private String joinLocation(String district, String city) {
        String safeDistrict = district == null ? "" : district.trim();
        String safeCity = city == null ? "" : city.trim();
        if (safeDistrict.isEmpty()) {
            return safeCity;
        }
        if (safeCity.isEmpty()) {
            return safeDistrict;
        }
        return safeDistrict + ", " + safeCity;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvAddress;
        final TextView tvDistrict;
        final TextView tvPhone;
        final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCinemaName);
            tvAddress = itemView.findViewById(R.id.tvCinemaAddress);
            tvDistrict = itemView.findViewById(R.id.tvCinemaDistrict);
            tvPhone = itemView.findViewById(R.id.tvCinemaPhone);
            tvStatus = itemView.findViewById(R.id.tvCinemaStatus);
        }
    }
}
