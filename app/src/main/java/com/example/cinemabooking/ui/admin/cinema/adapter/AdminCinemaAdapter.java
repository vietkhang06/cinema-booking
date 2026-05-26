package com.example.cinemabookingapp.ui.admin.cinema.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Cinema;

import java.util.ArrayList;
import java.util.List;

public class AdminCinemaAdapter extends RecyclerView.Adapter<AdminCinemaAdapter.Holder> {

    public interface OnCinemaActionListener {
        void onEditClick(Cinema cinema);
        void onDeleteClick(Cinema cinema);
        void onViewDetailsClick(Cinema cinema);
    }

    private List<Cinema> list = new ArrayList<>();
    private OnCinemaActionListener listener;

    public void setListener(OnCinemaActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Cinema> newList) {
        list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new Holder(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_admin_cinema, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        Cinema c = list.get(i);

        h.name.setText(c.name != null ? c.name : "N/A");

        String dist = c.district != null ? c.district : "N/A";
        String cityVal = c.city != null ? c.city : "N/A";
        h.location.setText(dist + ", " + cityVal);

        h.address.setText(c.address != null ? c.address : "N/A");

        if ("active".equalsIgnoreCase(c.status)) {
            h.status.setText("HOẠT ĐỘNG");
            h.status.setTextColor(android.graphics.Color.parseColor("#10B981"));
        } else {
            h.status.setText("NGỪNG HĐ");
            h.status.setTextColor(android.graphics.Color.parseColor("#C62828"));
        }

        int roomCount = c.roomIds != null ? c.roomIds.size() : 0;
        h.roomCount.setText("Phòng chiếu: " + roomCount);

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(c);
        });

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(c);
        });

        h.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetailsClick(c);
        });

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetailsClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, location, address, status, roomCount;
        View btnEdit, btnDelete, btnViewDetails;

        Holder(View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            location = v.findViewById(R.id.tvLocation);
            address = v.findViewById(R.id.tvAddress);
            status = v.findViewById(R.id.tvStatus);
            roomCount = v.findViewById(R.id.tvRoomCount);

            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnViewDetails = v.findViewById(R.id.btnViewDetails);
        }
    }
}