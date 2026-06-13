package com.example.cinemabooking.ui.admin.movie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.repository.MovieRepository;
import com.example.cinemabooking.ui.admin.AdminBottomNavHelper;
import com.example.cinemabooking.ui.admin.movie.adapter.AdminMovieAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminMovieListActivity extends BaseActivity {

    public static final String EXTRA_MOVIE_ID = "extra_movie_id";
    private static final String TAG = "AdminMovieList";

    private RecyclerView rvMovies;
    private TextView tvEmpty;
    private MaterialButton btnAddMovie;

    private MovieRepository movieRepository;
    private AdminMovieAdapter adapter;
    private final List<Movie> movieList = new ArrayList<>();
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_movie_list);

        AdminBottomNavHelper.setupAdminBottomNavigation(this, 2);

        // ✅ FIX: inject đúng datasource
        movieRepository = new MovieRepositoryImpl(new MovieRemoteDataSource());

        initViews();
        setupList();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMovies();
    }

    private void initViews() {
        rvMovies = findViewById(R.id.rvMovies);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAddMovie = findViewById(R.id.btnAddMovie);
        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupList() {
        adapter = new AdminMovieAdapter(new AdminMovieAdapter.OnMovieAction() {
            @Override
            public void onClick(Movie movie) {
                if (movie == null) return;
                openDetail(movie.movieId);
            }

            @Override
            public void onEdit(Movie movie) {
                if (movie == null) return;
                openForm(movie.movieId);
            }

            @Override
            public void onDelete(Movie movie) {
                if (movie == null) return;
                confirmDelete(movie);
            }
        });

        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setHasFixedSize(true);
        rvMovies.setAdapter(adapter);
    }

    private void bindActions() {
        btnAddMovie.setOnClickListener(v -> openForm(null));
    }

    // =========================
    // LOAD DATA
    // =========================
    private void loadMovies() {
        if (isLoading) return;
        isLoading = true;

        showLoading(true);

        movieRepository.getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> data) {
                isLoading = false;
                showLoading(false);

                movieList.clear();

                if (data != null) {
                    movieList.addAll(data);
                    Log.d(TAG, "Loaded movies: " + data.size());
                } else {
                    Log.d(TAG, "Data null");
                }

                adapter.submitList(new ArrayList<>(movieList)); // tránh bug reference

                tvEmpty.setVisibility(movieList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                isLoading = false;
                showLoading(false);

                Log.e(TAG, "Load error: " + message);
                showToast(message != null ? message : "Lỗi tải dữ liệu");
            }
        });
    }

    // =========================
    // NAVIGATION
    // =========================
    private void openForm(String movieId) {
        Intent intent = new Intent(this, AdminMovieFormActivity.class);
        if (movieId != null) {
            intent.putExtra(EXTRA_MOVIE_ID, movieId);
        }
        startActivity(intent);
    }

    private void openDetail(String movieId) {
        Intent intent = new Intent(this, AdminMovieDetailActivity.class);
        intent.putExtra(EXTRA_MOVIE_ID, movieId);
        startActivity(intent);
    }

    // =========================
    // DELETE
    // =========================
    private void confirmDelete(Movie movie) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá phim")
                .setMessage("Bạn có chắc muốn xoá \"" + movie.title + "\"?")
                .setPositiveButton("Xoá", (dialog, which) -> {

                    showLoading(true);

                    movieRepository.softDeleteMovie(movie.movieId, new ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            com.example.cinemabooking.ui.admin.log.AdminAuditLogger.log(
                                    "DELETE_MOVIE", "MOVIE", movie.movieId, "Đã xóa phim: " + movie.title
                            );
                            showLoading(false);
                            showToast("Đã xoá phim");
                            loadMovies();
                        }

                        @Override
                        public void onError(String message) {
                            showLoading(false);
                            showToast(message != null ? message : "Xoá thất bại");
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}