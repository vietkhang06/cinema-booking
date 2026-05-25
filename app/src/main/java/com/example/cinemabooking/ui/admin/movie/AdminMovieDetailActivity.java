package com.example.cinemabooking.ui.admin.movie;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.repository.MovieRepository;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminMovieDetailActivity extends BaseActivity {

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private ImageView imgPoster;
    private TextView tvTitle, tvGenres, tvLanguage, tvDuration, tvReleaseDate, tvAgeRating, tvStatus, tvDescription;
    private MaterialButton btnEdit, btnDelete;

    private MovieRepository movieRepository;
    private String movieId;
    private Movie currentMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_movie_detail);

        movieRepository = new MovieRepositoryImpl();
        movieId = getIntent().getStringExtra(AdminMovieListActivity.EXTRA_MOVIE_ID);

        initViews();
        bindActions();

        if (TextUtils.isEmpty(movieId)) {
            showToast("Thiếu movieId");
            finish();
            return;
        }

        loadMovie(movieId);
    }

    private void initViews() {
        imgPoster = findViewById(R.id.imgPoster);
        tvTitle = findViewById(R.id.tvTitle);
        tvGenres = findViewById(R.id.tvGenres);
        tvLanguage = findViewById(R.id.tvLanguage);
        tvDuration = findViewById(R.id.tvDuration);
        tvReleaseDate = findViewById(R.id.tvReleaseDate);
        tvAgeRating = findViewById(R.id.tvAgeRating);
        tvStatus = findViewById(R.id.tvStatus);
        tvDescription = findViewById(R.id.tvDescription);

        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void bindActions() {
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminMovieFormActivity.class);
            intent.putExtra(AdminMovieListActivity.EXTRA_MOVIE_ID, movieId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadMovie(String id) {
        movieRepository.getMovieById(id, new ResultCallback<Movie>() {
            @Override
            public void onSuccess(Movie data) {
                if (data == null) {
                    showToast("Không tìm thấy phim");
                    finish();
                    return;
                }

                currentMovie = data;
                bindMovie(data);
            }

            @Override
            public void onError(String message) {
                showToast(message);
                finish();
            }
        });
    }

    private void bindMovie(Movie movie) {
        tvTitle.setText(safe(movie.title));
        tvGenres.setText("Thể loại: " + joinGenres(movie.genres));
        tvLanguage.setText("Ngôn ngữ: " + safe(movie.language));
        tvDuration.setText("Thời lượng: " + safeDuration(movie.durationMinutes));
        tvReleaseDate.setText("Ngày phát hành: " + formatDate(movie.releaseDate));
        tvAgeRating.setText("Độ tuổi: " + safe(movie.ageRating));
        tvStatus.setText("Trạng thái: " + statusLabel(movie.status));
        tvDescription.setText(safe(movie.description));

        Glide.with(this)
                .load(movie.posterUrl)
                .placeholder(R.drawable.login_icon)
                .error(R.drawable.login_icon)
                .into(imgPoster);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xoá phim")
                .setMessage("Bạn có chắc muốn xoá phim này?")
                .setPositiveButton("Xoá", (dialog, which) ->
                        movieRepository.softDeleteMovie(movieId, new ResultCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                showToast("Đã xoá phim");
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                showToast(message);
                            }
                        }))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeDuration(int durationMinutes) {
        if (durationMinutes <= 0) return "Chưa có";
        return durationMinutes + " phút";
    }

    private String joinGenres(List<String> genres) {
        if (genres == null || genres.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (String genre : genres) {
            if (genre == null) continue;
            String text = genre.trim();
            if (text.isEmpty()) continue;
            if (builder.length() > 0) builder.append(", ");
            builder.append(text);
        }
        return builder.toString();
    }

    private String formatDate(long time) {
        if (time <= 0) return "";
        return dateFormat.format(new Date(time));
    }

    private String statusLabel(String value) {
        if (value == null) return "";
        String key = value.trim().toUpperCase(Locale.getDefault());
        if ("NOW_SHOWING".equals(key)) return "Đang chiếu";
        if ("COMING_SOON".equals(key)) return "Sắp chiếu";
        if ("ENDED".equals(key)) return "Ngừng chiếu";
        return value;
    }
}