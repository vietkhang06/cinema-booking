package com.example.cinemabookingapp.ui.admin.room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Room;

import java.util.ArrayList;
import java.util.List;

public class AdminRoomAdapter extends RecyclerView.Adapter<AdminRoomAdapter.Holder> {

    public interface OnRoomActionListener {
        void onEditClick(Room room);
        void onDeleteClick(Room room);
        void onViewSeatsClick(Room room);
    }

    private List<Room> list = new ArrayList<>();
    private OnRoomActionListener listener;

    public void setListener(OnRoomActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Room> newList) {
        list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_room, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Room room = list.get(position);

        holder.tvName.setText(room.name != null ? room.name : "N/A");

        String layout = room.layoutType != null ? room.layoutType : "2D";
        int total = room.totalSeats > 0 ? room.totalSeats : (room.seatRows * room.seatCols);
        holder.tvDetails.setText("Loại: " + layout + " | Sơ đồ: " + room.seatRows + " Hàng x " + room.seatCols + " Cột (" + total + " Ghế)");

        if ("active".equalsIgnoreCase(room.status)) {
            holder.tvStatus.setText("HOẠT ĐỘNG");
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
        } else {
            holder.tvStatus.setText("TẠM DỪNG");
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#C62828"));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(room);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(room);
        });

        holder.btnViewSeats.setOnClickListener(v -> {
            if (listener != null) listener.onViewSeatsClick(room);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvStatus;
        View btnEdit, btnDelete, btnViewSeats;

        Holder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRoomName);
            tvDetails = itemView.findViewById(R.id.tvRoomDetails);
            tvStatus = itemView.findViewById(R.id.tvRoomStatus);

            btnEdit = itemView.findViewById(R.id.btnEditRoom);
            btnDelete = itemView.findViewById(R.id.btnDeleteRoom);
            btnViewSeats = itemView.findViewById(R.id.btnViewSeats);
        }
    }
}
