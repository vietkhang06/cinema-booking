package com.example.cinemabooking.ui.admin.room.seatplan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;

import java.util.List;

public class SeatPlanRowAdapter extends RecyclerView.Adapter<SeatPlanRowAdapter.RowVH> {

    public interface OnSeatCellClickListener {
        void onSeatCellClicked(int rowPosition, int seatPosition);
    }

    private final List<SeatPlanRow> rows;
    private final OnSeatCellClickListener listener;

    public SeatPlanRowAdapter(List<SeatPlanRow> rows, OnSeatCellClickListener listener) {
        this.rows = rows;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_seat_plan_row, parent, false);
        return new RowVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RowVH holder, int position) {
        SeatPlanRow row = rows.get(position);
        holder.tvRowName.setText(row.rowName);

        SeatPlanCellAdapter cellAdapter = new SeatPlanCellAdapter(
                row.cells,
                seatPosition -> listener.onSeatCellClicked(position, seatPosition)
        );

        holder.rvCells.setAdapter(cellAdapter);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class RowVH extends RecyclerView.ViewHolder {
        TextView tvRowName;
        RecyclerView rvCells;

        RowVH(@NonNull View itemView) {
            super(itemView);
            tvRowName = itemView.findViewById(R.id.tvSeatRowName);
            rvCells = itemView.findViewById(R.id.rvSeatCells);
            rvCells.setLayoutManager(new LinearLayoutManager(itemView.getContext(), RecyclerView.HORIZONTAL, false));
            rvCells.setNestedScrollingEnabled(false);
            rvCells.setHasFixedSize(true);
        }
    }
}