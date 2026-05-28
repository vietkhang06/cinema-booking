package com.example.cinemabooking.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.ui.customer.model.HomeMovieItem;

import java.util.ArrayList;
import java.util.List;

public class HomeMovieAdapter extends RecyclerView.Adapter<HomeMovieAdapter.MovieViewHolder> {

    public interface OnMovieClickListener {
        void onMovieClick(HomeMovieItem item);
    }

    private final List<HomeMovieItem> items = new ArrayList<>();
    private OnMovieClickListener onMovieClickListener;

    public void setOnMovieClickListener(OnMovieClickListener listener) {
        this.onMovieClickListener = listener;
    }

    public void setItems(List<HomeMovieItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        HomeMovieItem item = items.get(position);

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.login_icon)
                .error(R.drawable.login_icon)
                .into(holder.imgPoster);

        holder.tvTitle.setText(item.getTitle());
        holder.tvRating.setText(item.getRating());
        holder.tvAge.setText(item.getAgeRating());

        holder.itemView.setOnClickListener(v -> {
            if (onMovieClickListener != null) {
                onMovieClickListener.onMovieClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPoster;
        TextView tvTitle;
        TextView tvRating;
        TextView tvAge;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvAge = itemView.findViewById(R.id.tvAge);
        }
    }
}