package com.example.cinemabooking.ui.admin.room.seatplan;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class SeatPlanCellAdapter extends RecyclerView.Adapter<SeatPlanCellAdapter.CellVH> {

    public interface OnSeatCellClickListener {
        void onSeatCellClicked(int seatPosition);
    }

    private final List<com.example.cinemabooking.ui.admin.room.seatplan.SeatPlanCell> cells;
    private final OnSeatCellClickListener listener;

    public SeatPlanCellAdapter(List<com.example.cinemabooking.ui.admin.room.seatplan.SeatPlanCell> cells, OnSeatCellClickListener listener) {
        this.cells = cells;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CellVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_seat_plan_cell, parent, false);
        return new CellVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CellVH holder, int position) {
        com.example.cinemabooking.ui.admin.room.seatplan.SeatPlanCell cell = cells.get(position);

        holder.tvSeatCode.setText(cell.seatCode);

        switch (cell.type) {
            case com.example.cinemabooking.ui.admin.room.seatplan.SeatPlanCell.TYPE_VIP:
                holder.tvSeatCode.setBackgroundResource(R.drawable.couch_solid_vip);
                holder.tvSeatCode.setTextColor(Color.WHITE);
                holder.tvSeatCode.setAlpha(1f);
                break;
            case com.example.cinemabooking.ui.admin.room.seatplan.SeatPlanCell.TYPE_COUPLE:
                holder.tvSeatCode.setBackgroundResource(R.drawable.couch_solid_normal);
                holder.tvSeatCode.getBackground().setTint(Color.parseColor("#C026D3"));
                holder.tvSeatCode.setTextColor(Color.WHITE);
                holder.tvSeatCode.setAlpha(1f);
                break;
            case com.example.cinemabooking.ui.admin.room.seatplan.SeatPlanCell.TYPE_LOCKED:
                holder.tvSeatCode.setBackgroundResource(R.drawable.couch_solid_full);
                holder.tvSeatCode.getBackground().setTint(Color.parseColor("#475569"));
                holder.tvSeatCode.setTextColor(Color.parseColor("#A0AEC0"));
                holder.tvSeatCode.setAlpha(0.6f);
                break;
            default: // TYPE_NORMAL
                holder.tvSeatCode.setBackgroundResource(R.drawable.couch_solid_normal);
                holder.tvSeatCode.getBackground().setTintList(null);
                holder.tvSeatCode.setTextColor(Color.parseColor("#E2E8F0"));
                holder.tvSeatCode.setAlpha(1f);
                break;
        }

        holder.itemView.setOnClickListener(v -> listener.onSeatCellClicked(position));
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static class CellVH extends RecyclerView.ViewHolder {
        TextView tvSeatCode;

        CellVH(@NonNull View itemView) {
            super(itemView);
            tvSeatCode = itemView.findViewById(R.id.tvSeatCode);
        }
    }
}