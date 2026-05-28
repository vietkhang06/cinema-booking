package com.example.cinemabooking.ui.component.EInvoice.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.ui.component.EInvoice.model.InvoiceSnackItem;

import java.util.List;

public class InvoiceSnackItemAdapter extends RecyclerView.Adapter<InvoiceSnackItemAdapter.ItemViewHolder> {
    private List<InvoiceSnackItem> itemList;

    public InvoiceSnackItemAdapter(List<InvoiceSnackItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invoice_snack_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        InvoiceSnackItem item = itemList.get(position);
        holder.itemNameAndQuantityTextView.setText(String.format("%sx%s", item.itemName, item.quantity));
        holder.itemPriceTextView.setText(String.format("%,d vnd", item.price));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameAndQuantityTextView, itemPriceTextView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            itemNameAndQuantityTextView = itemView.findViewById(R.id.item_name_x_quantity);
            itemPriceTextView = itemView.findViewById(R.id.item_price);
        }
    }
}
