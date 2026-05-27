package com.example.cinemabooking.ui.customer.cinema_contents.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.ui.customer.cinema_contents.model.CinemaFeedItem;

import java.util.ArrayList;
import java.util.List;

public class CinemaFeedAdapter extends RecyclerView.Adapter<CinemaFeedAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CinemaFeedItem item);
    }

    private final OnItemClickListener listener;
    private final List<CinemaFeedItem> items = new ArrayList<>();

    public CinemaFeedAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CinemaFeedItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cinema_feed, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CinemaFeedItem item = items.get(position);

        holder.tvTag.setText(item.tag);
        holder.tvTitle.setText(item.title);
        holder.tvExcerpt.setText(item.excerpt);
        holder.tvMeta.setText(item.meta);

        Glide.with(holder.itemView.getContext())
                .load(item.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.ivCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        holder.tvReadMore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTag, tvTitle, tvExcerpt, tvMeta, tvReadMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTag = itemView.findViewById(R.id.tvTag);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvExcerpt = itemView.findViewById(R.id.tvExcerpt);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvReadMore = itemView.findViewById(R.id.tvReadMore);
        }
    }
}