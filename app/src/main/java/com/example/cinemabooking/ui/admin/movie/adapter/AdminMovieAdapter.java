package com.example.cinemabooking.ui.admin.movie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.domain.model.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminMovieAdapter extends RecyclerView.Adapter<AdminMovieAdapter.ViewHolder> {

    public interface OnMovieAction {
        void onClick(Movie movie);
        void onEdit(Movie movie);
        void onDelete(Movie movie);
    }

    private final List<Movie> movies = new ArrayList<>();
    private final OnMovieAction listener;

    public AdminMovieAdapter(OnMovieAction listener) {
        this.listener = listener;
    }

    public void submitList(List<Movie> data) {
        movies.clear();
        if (data != null) {
            movies.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        Movie m = movies.get(i);

        h.tvTitle.setText(safe(m.title));
        h.tvDuration.setText(safeDuration(m.durationMinutes));
        h.tvStatus.setText(statusLabel(m.status));
        h.tvLanguage.setText("Ngôn ngữ: " + safe(m.language));

        Glide.with(h.itemView.getContext())
                .load(m.posterUrl)
                .placeholder(R.drawable.login_icon)
                .error(R.drawable.login_icon)
                .into(h.imgPoster);

        h.itemView.setOnClickListener(v -> listener.onClick(m));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(m));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(m));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeDuration(int durationMinutes) {
        if (durationMinutes <= 0) return "Chưa có thời lượng";
        return durationMinutes + " phút";
    }

    private String statusLabel(String status) {
        if (status == null) return "Chưa có trạng thái";

        String key = status.trim().toUpperCase(Locale.getDefault());
        if ("NOW_SHOWING".equals(key)) return "Đang chiếu";
        if ("COMING_SOON".equals(key)) return "Sắp chiếu";
        if ("ENDED".equals(key)) return "Ngừng chiếu";
        return status;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle, tvDuration, tvLanguage, tvStatus;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View v) {
            super(v);
            imgPoster = v.findViewById(R.id.imgPoster);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvDuration = v.findViewById(R.id.tvDuration);
            tvLanguage = v.findViewById(R.id.tvLanguage);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}