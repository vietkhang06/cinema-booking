package com.example.cinemabooking.ui.staff.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.dto.SeatDTO;

import java.util.ArrayList;
import java.util.List;

public class StaffSeatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LABEL = 0;
    private static final int TYPE_SEAT  = 1;

    public interface OnSeatClickListener {
        void onSeatClick(SeatDTO seat, int position);
    }

    private final List<Object> items = new ArrayList<>();
    private final OnSeatClickListener listener;

    public StaffSeatAdapter(List<SeatDTO> seatList, OnSeatClickListener listener) {
        this.listener = listener;
        setSeats(seatList);
    }

    public void setSeats(List<SeatDTO> seatList) {
        items.clear();
        String lastRow = null;
        for (SeatDTO seat : seatList) {
            if (seat.rowName == null) continue;
            if (!seat.rowName.equals(lastRow)) {
                items.add(seat.rowName);
                lastRow = seat.rowName;
            }
            items.add(seat);
        }
        notifyDataSetChanged();
    }

    /** Returns true if the item at position is a row label (not a seat). */
    public boolean isLabel(int position) {
        return position >= 0 && position < items.size() && items.get(position) instanceof String;
    }

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
        return items.size();
    }

    static class LabelViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        LabelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvRowLabel);
        }
        void bind(String label) { tvLabel.setText(label); }
    }

    static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeat;
        SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeat = itemView.findViewById(R.id.tvSeat);
        }

        void bind(SeatDTO seat) {
            tvSeat.setText(seat.seatCode != null ? seat.seatCode : "");
            tvSeat.setBackgroundTintList(null); // Clear any tint

            long now = System.currentTimeMillis();
            boolean isBooked = "booked".equalsIgnoreCase(seat.status);
            boolean isHeld = "held".equalsIgnoreCase(seat.status) && (seat.heldUntil != null && seat.heldUntil > now);

            if (isBooked) {
                // BOOKED: slate/gray
                tvSeat.setBackgroundResource(R.drawable.couch_solid_full);
                tvSeat.setTextColor(0xFFFFFFFF);
                itemView.setEnabled(true);
                itemView.setAlpha(1f);
            } else if (isHeld) {
                // HELD: Orange
                tvSeat.setBackgroundResource(R.drawable.couch_solid_selection);
                tvSeat.setBackgroundTintList(ColorStateList.valueOf(0xFFFF9800));
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
