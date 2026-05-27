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
        holder.tvName.setText(valueOrDefault(cinema.name, "Rap phim"));
        holder.tvAddress.setText(valueOrDefault(cinema.address, "Chua cap nhat dia chi"));
        holder.tvDistrict.setText(valueOrDefault(joinLocation(cinema.district, cinema.city), "Chua cap nhat khu vuc"));
        holder.tvPhone.setText(valueOrDefault(cinema.phone, "Chua cap nhat hotline"));
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

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCinemaName);
            tvAddress = itemView.findViewById(R.id.tvCinemaAddress);
            tvDistrict = itemView.findViewById(R.id.tvCinemaDistrict);
            tvPhone = itemView.findViewById(R.id.tvCinemaPhone);
        }
    }
}
