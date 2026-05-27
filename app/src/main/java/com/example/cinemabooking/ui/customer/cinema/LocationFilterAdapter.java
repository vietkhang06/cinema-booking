// ui/customer/cinema/LocationFilterAdapter.java

package com.example.cinemabooking.ui.customer.cinema;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;

import java.util.ArrayList;
import java.util.List;

public class LocationFilterAdapter extends RecyclerView.Adapter<LocationFilterAdapter.VH> {

    public interface OnLocationClickListener {
        void onClick(String location);
    }

    private final List<String> items = new ArrayList<>();
    private String selectedLocation;
    private final OnLocationClickListener listener;

    public LocationFilterAdapter(String selectedLocation, OnLocationClickListener listener) {
        this.selectedLocation = selectedLocation;
        this.listener = listener;
    }

    public void setData(List<String> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String loc = items.get(position);
        holder.tvName.setText(loc);

        boolean isSelected = loc.equals(selectedLocation);
        holder.ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.tvName.setTextColor(isSelected
                ? holder.itemView.getContext().getColor(android.R.color.black)
                : 0xFF6B7280);

        holder.itemView.setOnClickListener(v -> {
            selectedLocation = loc;
            notifyDataSetChanged();
            if (listener != null) listener.onClick(loc);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivCheck;
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvLocationName);
            ivCheck = v.findViewById(R.id.ivLocationCheck);
        }
    }
}