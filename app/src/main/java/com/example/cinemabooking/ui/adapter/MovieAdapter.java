package com.example.cinemabooking.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private List<String> movies;

    public MovieAdapter(List<String> movies) {
        this.movies = movies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_movie, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = movies.get(position);
        holder.tvTitle.setText(name);

        holder.img.setImageResource(R.drawable.login_icon); // demo
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}