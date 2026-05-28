package com.example.cinemabooking.ui.staff.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ItemViewHolder> {
    private List<String> itemList;

    public OrderItemAdapter(List<String> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_order_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        String raw = itemList.get(position);
        try {
            String[] parts = raw.split(",");
            String name = parts[0];
            double price = Double.parseDouble(parts[1]);
            int quantity = Integer.parseInt(parts[2]);

            holder.itemNameTV.setText(name);
            holder.itemPriceTV.setText(String.format("Đơn giá: %,.0f vnd", price));
            holder.itemQuantityTV.setText("x" + quantity);
            
            // Set default popcorn/snack indicator icon
            holder.snackImageView.setImageResource(R.drawable.login_icon);
        } catch (Exception e) {
            holder.itemNameTV.setText("Đồ ăn");
            holder.itemPriceTV.setText("");
            holder.itemQuantityTV.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTV, itemPriceTV, itemQuantityTV;
        ImageView snackImageView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            itemNameTV = itemView.findViewById(R.id.snack_item_name);
            itemPriceTV = itemView.findViewById(R.id.snack_item_price);
            itemQuantityTV = itemView.findViewById(R.id.snack_item_quantity);
            snackImageView = itemView.findViewById(R.id.snacK_item_image);
        }
    }
}
