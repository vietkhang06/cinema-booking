package com.example.cinemabooking.ui.admin.movie;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.repository.MovieRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class AdminMovieFormActivity extends BaseActivity {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private TextView tvTitle;
    private TextInputLayout tilTitle, tilDescription, tilGenres, tilLanguage, tilDuration,
            tilReleaseDate, tilPosterUrl, tilTrailerUrl;
    private TextInputEditText edtTitle, edtDescription, edtGenres, edtLanguage, edtDuration,
            edtReleaseDate, edtPosterUrl, edtTrailerUrl;
    private MaterialAutoCompleteTextView actvAgeRating, actvStatus;
    private MaterialButton btnSave;

    private MovieRepository movieRepository;
    private Movie currentMovie;
    private String movieId;
    private boolean isEditMode;

    private static final String[] AGE_RATINGS = {"P", "C13", "C16", "C18"};
    private static final String[] STATUS_LABELS = {"Đang chiếu", "Sắp chiếu", "Ngừng chiếu"};

    public static void start(Context context, @Nullable String movieId) {
        Intent intent = new Intent(context, AdminMovieFormActivity.class);
        if (movieId != null) {
            intent.putExtra(AdminMovieListActivity.EXTRA_MOVIE_ID, movieId);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_movie_form);

        movieRepository = new MovieRepositoryImpl();
        movieId = getIntent().getStringExtra(AdminMovieListActivity.EXTRA_MOVIE_ID);
        isEditMode = !TextUtils.isEmpty(movieId);

        initViews();
        setupDropdowns();
        bindActions();

        if (isEditMode) {
            tvTitle.setText("Sửa phim");
            loadMovie(movieId);
        } else {
            tvTitle.setText("Thêm phim");
            btnSave.setText("Thêm phim");
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);

        tilTitle = findViewById(R.id.tilTitle);
        tilDescription = findViewById(R.id.tilDescription);
        tilGenres = findViewById(R.id.tilGenres);
        tilLanguage = findViewById(R.id.tilLanguage);
        tilDuration = findViewById(R.id.tilDuration);
        tilReleaseDate = findViewById(R.id.tilReleaseDate);
        tilPosterUrl = findViewById(R.id.tilPosterUrl);
        tilTrailerUrl = findViewById(R.id.tilTrailerUrl);

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtGenres = findViewById(R.id.edtGenres);
        edtLanguage = findViewById(R.id.edtLanguage);
        edtDuration = findViewById(R.id.edtDuration);
        edtReleaseDate = findViewById(R.id.edtReleaseDate);
        edtPosterUrl = findViewById(R.id.edtPosterUrl);
        edtTrailerUrl = findViewById(R.id.edtTrailerUrl);

        actvAgeRating = findViewById(R.id.actvAgeRating);
        actvStatus = findViewById(R.id.actvStatus);

        btnSave = findViewById(R.id.btnSave);

        android.view.View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupDropdowns() {
        actvAgeRating.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                Arrays.asList(AGE_RATINGS)
        ));

        actvStatus.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                Arrays.asList(STATUS_LABELS)
        ));
    }

    private void bindActions() {
        btnSave.setOnClickListener(v -> saveMovie());
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
        edtTitle.setText(movie.title);
        edtDescription.setText(movie.description);
        edtGenres.setText(joinGenres(movie.genres));
        edtLanguage.setText(movie.language);
        edtDuration.setText(movie.durationMinutes > 0 ? String.valueOf(movie.durationMinutes) : "");
        edtReleaseDate.setText(formatDate(movie.releaseDate));
        edtPosterUrl.setText(movie.posterUrl);
        edtTrailerUrl.setText(movie.trailerUrl);
        actvAgeRating.setText(safe(movie.ageRating), false);
        actvStatus.setText(statusLabel(movie.status), false);
    }

    private void saveMovie() {
        clearErrors();

        String title = getText(edtTitle);
        String description = getText(edtDescription);
        String genresText = getText(edtGenres);
        String language = getText(edtLanguage);
        String durationText = getText(edtDuration);
        String releaseDateText = getText(edtReleaseDate);
        String posterUrl = getText(edtPosterUrl);
        String trailerUrl = getText(edtTrailerUrl);
        String ageRatingLabel = getText(actvAgeRating);
        String statusLabel = getText(actvStatus);

        if (TextUtils.isEmpty(title)) {
            tilTitle.setError("Nhập tên phim");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            tilDescription.setError("Nhập mô tả");
            return;
        }

        if (TextUtils.isEmpty(genresText)) {
            tilGenres.setError("Nhập thể loại");
            return;
        }

        if (TextUtils.isEmpty(language)) {
            tilLanguage.setError("Nhập ngôn ngữ");
            return;
        }

        if (TextUtils.isEmpty(durationText)) {
            tilDuration.setError("Nhập thời lượng");
            return;
        }

        int durationMinutes;
        try {
            durationMinutes = Integer.parseInt(durationText);
        } catch (Exception e) {
            tilDuration.setError("Thời lượng không hợp lệ");
            return;
        }

        if (TextUtils.isEmpty(releaseDateText)) {
            tilReleaseDate.setError("Nhập ngày phát hành");
            return;
        }

        long releaseDate;
        try {
            releaseDate = parseDate(releaseDateText);
        } catch (ParseException e) {
            tilReleaseDate.setError("Ngày phải theo dd/MM/yyyy");
            return;
        }

        Movie movie = currentMovie != null ? currentMovie : new Movie();
        movie.movieId = isEditMode ? movieId : movie.movieId;
        movie.title = title;
        movie.description = description;
        movie.genres = splitGenres(genresText);
        movie.language = language;
        movie.durationMinutes = durationMinutes;
        movie.releaseDate = releaseDate;
        movie.ageRating = ageRatingLabel;
        movie.posterUrl = posterUrl;
        movie.trailerUrl = trailerUrl;
        movie.status = statusValue(statusLabel);

        if (currentMovie == null) {
            movie.ratingAvg = 0;
            movie.ratingCount = 0;
            movie.createdAt = System.currentTimeMillis();
            movie.deleted = false;
        } else {
            movie.ratingAvg = currentMovie.ratingAvg;
            movie.ratingCount = currentMovie.ratingCount;
            movie.createdAt = currentMovie.createdAt;
            movie.deleted = currentMovie.deleted;
        }

        movie.updatedAt = System.currentTimeMillis();

        btnSave.setEnabled(false);

        if (isEditMode) {
            movieRepository.updateMovie(movie, new ResultCallback<Movie>() {
                @Override
                public void onSuccess(Movie data) {
                    com.example.cinemabooking.ui.admin.log.AdminAuditLogger.log(
                            "UPDATE_MOVIE", "MOVIE", data.movieId, "Đã cập nhật phim: " + data.title
                    );
                    showToast("Đã cập nhật phim");
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    showToast(message);
                }
            });
        } else {
            movieRepository.createMovie(movie, new ResultCallback<Movie>() {
                @Override
                public void onSuccess(Movie data) {
                    com.example.cinemabooking.ui.admin.log.AdminAuditLogger.log(
                            "CREATE_MOVIE", "MOVIE", data.movieId, "Đã thêm phim mới: " + data.title
                    );
                    showToast("Đã thêm phim");
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    showToast(message);
                }
            });
        }
    }

    private void clearErrors() {
        tilTitle.setError(null);
        tilDescription.setError(null);
        tilGenres.setError(null);
        tilLanguage.setError(null);
        tilDuration.setError(null);
        tilReleaseDate.setError(null);
        tilPosterUrl.setError(null);
        tilTrailerUrl.setError(null);
    }

    private String getText(TextInputEditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }

    private String getText(MaterialAutoCompleteTextView edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }

    private java.util.List<String> splitGenres(String value) {
        java.util.List<String> genres = new java.util.ArrayList<>();
        if (value == null) return genres;

        String[] parts = value.split(",");
        for (String part : parts) {
            String text = part.trim();
            if (!text.isEmpty()) genres.add(text);
        }
        return genres;
    }

    private String joinGenres(java.util.List<String> genres) {
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

    private long parseDate(String value) throws ParseException {
        Date date = DATE_FORMAT.parse(value);
        return date == null ? System.currentTimeMillis() : date.getTime();
    }

    private String formatDate(long time) {
        if (time <= 0) return "";
        return DATE_FORMAT.format(new Date(time));
    }

    private String statusValue(String label) {
        if ("Đang chiếu".equals(label)) return "NOW_SHOWING";
        if ("Sắp chiếu".equals(label)) return "COMING_SOON";
        if ("Ngừng chiếu".equals(label)) return "ENDED";
        return label;
    }

    private String statusLabel(String value) {
        if (value == null) return "";
        String key = value.trim().toUpperCase(Locale.getDefault());
        if ("NOW_SHOWING".equals(key)) return "Đang chiếu";
        if ("COMING_SOON".equals(key)) return "Sắp chiếu";
        if ("ENDED".equals(key)) return "Ngừng chiếu";
        return value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}