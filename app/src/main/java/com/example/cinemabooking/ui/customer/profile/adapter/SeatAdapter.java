package com.example.cinemabooking.ui.customer.profile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.dto.SeatDTO;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//                                                     ↑ đổi thành RecyclerView.ViewHolder (generic)

    // Thêm 2 hằng số để phân biệt loại item
    private static final int TYPE_LABEL = 0;
    private static final int TYPE_SEAT  = 1;

    public interface OnSeatClickListener {
        void onSeatClick(SeatDTO seat, int position);
    }

    private final List<Object> items = new ArrayList<>();
    private final OnSeatClickListener listener;

    public SeatAdapter(List<SeatDTO> seatList, OnSeatClickListener listener) {
        this.listener = listener;
        setSeats(seatList); // gọi setSeats thay vì gán trực tiếp
    }

    public void setSeats(List<SeatDTO> seatList) {
        items.clear();
        String lastRow = null;
        for (SeatDTO seat : seatList) {
            if (!seat.rowName.equals(lastRow)) {
                items.add(seat.rowName); // chen label "A", "B"... vào trước mỗi hàng
                lastRow = seat.rowName;
            }
            items.add(seat);
        }
        notifyDataSetChanged();
    }

    // Thêm method này — quyết định item nào là label, cái nào là ghế
    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_LABEL : TYPE_SEAT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_LABEL) {
            View view = inflater.inflate(R.layout.item_seat_label, parent, false);
            return new LabelViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_seat, parent, false);
            return new SeatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LabelViewHolder) {
            ((LabelViewHolder) holder).bind((String) items.get(position));
        } else if (holder instanceof SeatViewHolder) {
            SeatDTO seat = (SeatDTO) items.get(position);
            ((SeatViewHolder) holder).bind(seat);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSeatClick(seat, position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size(); // dùng items thay vì seatList
    }

    // --- ViewHolder cho label A, B, C... ---
    static class LabelViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        LabelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvRowLabel);
        }
        void bind(String label) { tvLabel.setText(label); }
    }

    // --- ViewHolder cho ghế (giữ nguyên bind() của bạn) ---
    static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeat;
        SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeat = itemView.findViewById(R.id.tvSeat);
        }

        void bind(SeatDTO seat) {
            tvSeat.setText(seat.seatCode != null ? seat.seatCode : "");
            
            long now = System.currentTimeMillis();
            boolean isBooked = "booked".equalsIgnoreCase(seat.status);
            
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : "";
            
            boolean isHeldByMe = "held".equalsIgnoreCase(seat.status) 
                    && (seat.heldUntil > now) 
                    && currentUserId.equals(seat.heldBy);
            
            boolean isHeldByOther = "held".equalsIgnoreCase(seat.status) 
                    && (seat.heldUntil > now) 
                    && !currentUserId.equals(seat.heldBy);

            boolean isLocked = "LOCKED".equalsIgnoreCase(seat.status)
                    || "LOCKED".equalsIgnoreCase(seat.seatType);

            if (isLocked) {
                // PHYSICALLY LOCKED: dark gray color, disabled
                tvSeat.setBackgroundResource(R.drawable.couch_solid_full);
                tvSeat.setTextColor(0xFF444444);
                itemView.setEnabled(false);
                itemView.setAlpha(0.3f);
            } else if (isBooked) {
                // BOOKED: slate color
                tvSeat.setBackgroundResource(R.drawable.couch_solid_full);
                tvSeat.setTextColor(0xFF555566);
                itemView.setEnabled(false);
                itemView.setAlpha(0.6f);
            } else if (isHeldByOther) {
                // HELD BY OTHER: locked gray color
                tvSeat.setBackgroundResource(R.drawable.couch_solid_full);
                tvSeat.setTextColor(0xFF888888);
                itemView.setEnabled(false);
                itemView.setAlpha(0.4f);
            } else if (isHeldByMe || seat.isSelected) {
                // HELD BY ME / SELECTED: selected color
                tvSeat.setBackgroundResource(R.drawable.couch_solid_selection);
                tvSeat.setTextColor(0xFFFFFFFF);
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            } else if ("VIP".equalsIgnoreCase(seat.seatType)) {
                // VIP
                tvSeat.setBackgroundResource(R.drawable.couch_solid_vip);
                tvSeat.setTextColor(0xFF1A1A1A);
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            } else {
                // AVAILABLE STANDARD
                tvSeat.setBackgroundResource(R.drawable.couch_solid_normal);
                tvSeat.setTextColor(0xFFCCCCCC);
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            }
        }
    }
}