package com.example.cinemabooking.ui.customer.cinema;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.data.repository.ShowtimeRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.model.Showtime;
import com.example.cinemabooking.domain.repository.CinemaRepository;
import com.example.cinemabooking.domain.repository.MovieRepository;
import com.example.cinemabooking.domain.repository.ShowtimeRepository;
import com.example.cinemabooking.ui.customer.movie.MovieDetailActivity;
import com.example.cinemabooking.ui.customer.SeatSelectionActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CinemaDetailActivity extends BaseActivity {

    public static final String EXTRA_CINEMA_ID = "extra_cinema_id";
    public static final String EXTRA_CINEMA_NAME = "extra_cinema_name";
    public static final String EXTRA_CINEMA_ADDRESS = "extra_cinema_address";
    public static final String EXTRA_CINEMA_CITY = "extra_cinema_city";
    public static final String EXTRA_CINEMA_DISTRICT = "extra_cinema_district";
    public static final String EXTRA_CINEMA_PHONE = "extra_cinema_phone";
    public static final String EXTRA_CINEMA_STATUS = "extra_cinema_status";
    public static final String EXTRA_CINEMA_LATITUDE = "extra_cinema_latitude";
    public static final String EXTRA_CINEMA_LONGITUDE = "extra_cinema_longitude";

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView imgHeroBackdrop;
    private ImageView imgCinemaLogo;
    private TextView tvCinemaTitle;
    private TextView tvCinemaTagline;
    private TextView tvStatus;
    private TextView tvCityDistrict;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvAbout;
    private TextView tvMapInfo;
    private MaterialButton btnOpenMap;
    private MaterialButton btnCall;
    private LinearLayout layoutNowShowing;
    private LinearLayout layoutComingSoon;

    private View scrollCinemaDetail;
    private View progressBar;
    private View layoutError;
    private TextView tvErrorMessage;
    private MaterialButton btnRetry;

    private CinemaRepository cinemaRepository;
    private ShowtimeRepository showtimeRepository;
    private MovieRepository movieRepository;
    private String cinemaId = "";
    private String cinemaName = "Rạp phim";
    private String address = "";
    private String city = "";
    private String district = "";
    private String phone = "";
    private String status = "";
    private double latitude = 0;
    private double longitude = 0;

    public static Intent createIntent(Context context, Cinema cinema) {
        Intent intent = new Intent(context, CinemaDetailActivity.class);
        if (cinema == null) {
            return intent;
        }
        intent.putExtra(EXTRA_CINEMA_ID, cinema.cinemaId);
        intent.putExtra(EXTRA_CINEMA_NAME, cinema.name);
        intent.putExtra(EXTRA_CINEMA_ADDRESS, cinema.address);
        intent.putExtra(EXTRA_CINEMA_CITY, cinema.city);
        intent.putExtra(EXTRA_CINEMA_DISTRICT, cinema.district);
        intent.putExtra(EXTRA_CINEMA_PHONE, cinema.phone);
        intent.putExtra(EXTRA_CINEMA_STATUS, cinema.status);
        intent.putExtra(EXTRA_CINEMA_LATITUDE, cinema.latitude);
        intent.putExtra(EXTRA_CINEMA_LONGITUDE, cinema.longitude);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_cinema);

        cinemaRepository = new CinemaRepositoryImpl();
        showtimeRepository = new ShowtimeRepositoryImpl(true);
        movieRepository = new MovieRepositoryImpl();
        initViews();
        bindFallbackExtras();
        setupActions();
        loadCinemaFromFirestore();
        loadShowtimesAndMovies();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        imgHeroBackdrop = findViewById(R.id.imgHeroBackdrop);
        imgCinemaLogo = findViewById(R.id.imgCinemaLogo);
        tvCinemaTitle = findViewById(R.id.tvCinemaTitle);
        tvCinemaTagline = findViewById(R.id.tvCinemaTagline);
        tvStatus = findViewById(R.id.tvStatus);
        tvCityDistrict = findViewById(R.id.tvCityDistrict);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvCinemaFullAddress);
        tvAbout = findViewById(R.id.tvAbout);
        tvMapInfo = findViewById(R.id.tvMapInfo);
        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnCall = findViewById(R.id.btnCall);
        layoutNowShowing = findViewById(R.id.layoutNowShowing);
        layoutComingSoon = findViewById(R.id.layoutComingSoon);

        scrollCinemaDetail = findViewById(R.id.scrollCinemaDetail);
        progressBar = findViewById(R.id.progressBar);
        layoutError = findViewById(R.id.layoutError);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRetry = findViewById(R.id.btnRetry);
    }

    private void bindFallbackExtras() {
        Intent intent = getIntent();
        cinemaId = safe(intent.getStringExtra(EXTRA_CINEMA_ID), cinemaId);
        cinemaName = safe(intent.getStringExtra(EXTRA_CINEMA_NAME), cinemaName);
        address = safe(intent.getStringExtra(EXTRA_CINEMA_ADDRESS), address);
        city = safe(intent.getStringExtra(EXTRA_CINEMA_CITY), city);
        district = safe(intent.getStringExtra(EXTRA_CINEMA_DISTRICT), district);
        phone = safe(intent.getStringExtra(EXTRA_CINEMA_PHONE), phone);
        status = safe(intent.getStringExtra(EXTRA_CINEMA_STATUS), status);
        latitude = intent.getDoubleExtra(EXTRA_CINEMA_LATITUDE, latitude);
        longitude = intent.getDoubleExtra(EXTRA_CINEMA_LONGITUDE, longitude);

        bindCinemaInfo();
        loadCinemaImage();
    }

    private void loadCinemaFromFirestore() {
        if (TextUtils.isEmpty(cinemaId)) {
            return;
        }

        boolean hasFallback = !TextUtils.isEmpty(cinemaName) && !cinemaName.equals("Rạp phim");

        if (!hasFallback) {
            scrollCinemaDetail.setVisibility(View.GONE);
            layoutError.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.VISIBLE);

        cinemaRepository.getCinemaById(cinemaId, new ResultCallback<Cinema>() {
            @Override
            public void onSuccess(Cinema cinema) {
                progressBar.setVisibility(View.GONE);
                if (cinema == null) {
                    if (!hasFallback) {
                        scrollCinemaDetail.setVisibility(View.GONE);
                        layoutError.setVisibility(View.VISIBLE);
                        tvErrorMessage.setText("Không tìm thấy thông tin rạp này.");
                    } else {
                        showToast("Không tìm thấy thông tin rạp.");
                    }
                    return;
                }

                cinemaName = safe(cinema.name, cinemaName);
                address = safe(cinema.address, address);
                city = safe(cinema.city, city);
                district = safe(cinema.district, district);
                phone = safe(cinema.phone, phone);
                status = safe(cinema.status, status);
                latitude = cinema.latitude;
                longitude = cinema.longitude;

                bindCinemaInfo();
                loadCinemaImage();

                scrollCinemaDetail.setVisibility(View.VISIBLE);
                layoutError.setVisibility(View.GONE);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                if (!hasFallback) {
                    scrollCinemaDetail.setVisibility(View.GONE);
                    layoutError.setVisibility(View.VISIBLE);
                    tvErrorMessage.setText(TextUtils.isEmpty(message) ? "Lỗi kết nối. Vui lòng thử lại." : message);
                } else {
                    showToast(message == null ? "Không thể tải thông tin rạp mới nhất." : message);
                }
            }
        });
    }

    private void bindCinemaInfo() {
        tvCinemaTitle.setText(safe(cinemaName, "Rạp phim"));
        tvCinemaTagline.setText(buildLocationText());
        
        String statusText = "Đang hoạt động";
        int textColor = Color.parseColor("#10B981");
        int bgColor = Color.parseColor("#E6FBF3");
        
        if (!TextUtils.isEmpty(status)) {
            if ("active".equalsIgnoreCase(status) || "available".equalsIgnoreCase(status) || "scheduled".equalsIgnoreCase(status) || "đang hoạt động".equalsIgnoreCase(status)) {
                statusText = "Đang hoạt động";
                textColor = Color.parseColor("#10B981");
                bgColor = Color.parseColor("#E6FBF3");
            } else if ("inactive".equalsIgnoreCase(status) || "tạm dừng".equalsIgnoreCase(status)) {
                statusText = "Tạm dừng";
                textColor = Color.parseColor("#EF4444");
                bgColor = Color.parseColor("#FEE2E2");
            } else {
                statusText = status;
                textColor = Color.parseColor("#1E1A23");
                bgColor = Color.parseColor("#F3F4F6");
            }
        }
        tvStatus.setText(statusText);
        tvStatus.setTextColor(textColor);
        tvStatus.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        
        tvCityDistrict.setText(buildLocationText());
        tvPhone.setText(TextUtils.isEmpty(phone) ? "Chưa cập nhật hotline" : phone);
        tvAddress.setText(TextUtils.isEmpty(address) ? "Chưa cập nhật địa chỉ" : address);
        tvAbout.setText("Không gian rạp được thiết kế cho trải nghiệm xem phim thoải mái, âm thanh rõ nét và khu vực ghế ngồi hiện đại.");
        tvMapInfo.setText(hasCoordinate()
                ? String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude)
                : "Chưa cập nhật tọa độ");
    }

    private void loadCinemaImage() {
        String logoUrl = "https://play-lh.googleusercontent.com/nxo4BC4BQ5hXuNi-UCdPM5kC0uZH1lq7bglINlWNUA_v8yMfHHOtTjhLTvo5NDjVeqx-";
        String backdropUrl = "https://thelandmark81.com.vn/wp-content/uploads/2025/10/rap-chieu-phim-vincom-thu-duc-cgv-la-diem-hen-ly-tuong-cho-nhung-nguoi-yeu-thich-phim-dien-anh.jpg";
        
        if (cinemaName != null) {
            String nameLower = cinemaName.toLowerCase();
            if (nameLower.contains("quận 1") || nameLower.contains("quan 1")) {
                backdropUrl = "https://i1.wp.com/kenhhomestay.com/wp-content/uploads/2019/12/cgv-binh-duong-2.png";
            } else if (nameLower.contains("quận 2") || nameLower.contains("quan 2")) {
                backdropUrl = "https://lh7-us.googleusercontent.com/WdQhGK0lo8BkP7xAHPaRG-d0W1qVxgIyEyJ5J3hJGqkmiCXTmaVpXqcGgG3UCIP_4QoGoHnLEQPMHlww126sVxMZQ0NPUn0Hi2rY5GeY0tht6wuIbuY9NDpH3fDJeuwPcFNAZpAo8I94Q0-QWJvbE84";
            }
        }

        Glide.with(this)
                .load(backdropUrl)
                .error(R.drawable.ic_cinemax_logo)
                .into(imgHeroBackdrop);
        Glide.with(this)
                .load(logoUrl)
                .error(R.drawable.ic_cinemax_logo)
                .into(imgCinemaLogo);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
        btnShare.setOnClickListener(v -> shareCinema());
        btnOpenMap.setOnClickListener(v -> openMap());
        btnCall.setOnClickListener(v -> callCinema());
        btnRetry.setOnClickListener(v -> {
            loadCinemaFromFirestore();
            loadShowtimesAndMovies();
        });
    }

    private void shareCinema() {
        String shareText = cinemaName + "\n" + tvAddress.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText.trim());
        startActivity(Intent.createChooser(intent, "Chia sẻ rạp phim"));
    }

    private void openMap() {
        Uri uri;
        if (hasCoordinate()) {
            uri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + Uri.encode(cinemaName) + ")");
        } else if (!TextUtils.isEmpty(address)) {
            StringBuilder queryBuilder = new StringBuilder(address);
            if (!TextUtils.isEmpty(district)) {
                queryBuilder.append(", ").append(district);
            }
            if (!TextUtils.isEmpty(city)) {
                queryBuilder.append(", ").append(city);
            }
            uri = Uri.parse("geo:0,0?q=" + Uri.encode(queryBuilder.toString()));
        } else {
            showToast("Chưa có địa chỉ để mở bản đồ");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (Exception e) {
            showToast("Không thể mở bản đồ");
        }
    }

    private void callCinema() {
        if (TextUtils.isEmpty(phone)) {
            showToast("Chưa có số điện thoại");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void openMovieDetail(Movie movie) {
        if (movie == null) return;
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.movieId);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_TITLE, movie.title);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_POSTER_URL, movie.posterUrl);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_RATING, String.valueOf(movie.ratingAvg));
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_AGE_RATING, movie.ageRating != null ? movie.ageRating : "P");
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_DURATION, String.valueOf(movie.durationMinutes));
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_RELEASE_DATE, movie.releaseDate);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_DESCRIPTION, movie.description);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_TRAILER_URL, movie.trailerUrl);
        startActivity(intent);
    }

    private void loadShowtimesAndMovies() {
        if (TextUtils.isEmpty(cinemaId)) {
            return;
        }

        movieRepository.getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                Map<String, Movie> movieMap = new HashMap<>();
                List<Movie> comingSoonMovies = new ArrayList<>();
                if (movies != null) {
                    for (Movie m : movies) {
                        movieMap.put(m.movieId, m);
                        if ("COMING_SOON".equalsIgnoreCase(m.status) || "COMING SOON".equalsIgnoreCase(m.status)) {
                            comingSoonMovies.add(m);
                        }
                    }
                }

                showtimeRepository.getShowtimesByCinemaId(cinemaId, new ResultCallback<List<Showtime>>() {
                    @Override
                    public void onSuccess(List<Showtime> showtimes) {
                        renderRealShowtimes(showtimes, movieMap, comingSoonMovies);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("CinemaDetailActivity", "Failed to fetch showtimes: " + message);
                        renderRealShowtimes(new ArrayList<>(), movieMap, comingSoonMovies);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.e("CinemaDetailActivity", "Failed to fetch movies: " + message);
                showtimeRepository.getShowtimesByCinemaId(cinemaId, new ResultCallback<List<Showtime>>() {
                    @Override
                    public void onSuccess(List<Showtime> showtimes) {
                        renderRealShowtimes(showtimes, new HashMap<>(), new ArrayList<>());
                    }

                    @Override
                    public void onError(String errorMsg) {
                        renderRealShowtimes(new ArrayList<>(), new HashMap<>(), new ArrayList<>());
                    }
                });
            }
        });
    }

    private void renderRealShowtimes(List<Showtime> showtimes, Map<String, Movie> movieMap, List<Movie> comingSoonMovies) {
        layoutNowShowing.removeAllViews();
        layoutComingSoon.removeAllViews();

        Map<String, List<Showtime>> grouped = new HashMap<>();
        long now = System.currentTimeMillis();
        if (showtimes != null) {
            for (Showtime st : showtimes) {
                if (st.deleted) continue;
                if (st.startAt < now || !isBookableStatus(st.status)) {
                    continue;
                }
                if (TextUtils.isEmpty(st.movieId)) continue;
                
                String key = st.movieId + "|" + (st.format != null ? st.format : "2D");
                List<Showtime> list = grouped.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    grouped.put(key, list);
                }
                list.add(st);
            }
        }

        List<CinemaMovieSchedule> nowShowingSchedules = new ArrayList<>();
        for (Map.Entry<String, List<Showtime>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String movieId = parts[0];
            String format = parts[1];
            List<Showtime> stList = entry.getValue();
            
            stList.sort((s1, s2) -> Long.compare(s1.startAt, s2.startAt));

            Movie movie = movieMap.get(movieId);
            if (movie != null) {
                nowShowingSchedules.add(new CinemaMovieSchedule(movie, format, stList));
            }
        }

        nowShowingSchedules.sort((s1, s2) -> {
            if (s1.movie == null || s2.movie == null) return 0;
            return s1.movie.title.compareToIgnoreCase(s2.movie.title);
        });

        for (CinemaMovieSchedule schedule : nowShowingSchedules) {
            layoutNowShowing.addView(buildScheduleCard(schedule, true));
        }

        if (nowShowingSchedules.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Hiện không có suất chiếu nào tại rạp này.");
            tvEmpty.setTextColor(Color.parseColor("#666666"));
            tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, dp(20), 0, dp(20));
            layoutNowShowing.addView(tvEmpty);
        }

        List<Movie> realComingSoon = new ArrayList<>();
        for (Movie m : comingSoonMovies) {
            boolean hasShowtime = false;
            if (showtimes != null) {
                for (Showtime st : showtimes) {
                    if (m.movieId.equals(st.movieId) && !st.deleted) {
                        hasShowtime = true;
                        break;
                    }
                }
            }
            if (!hasShowtime) {
                realComingSoon.add(m);
            }
        }

        int count = 0;
        for (Movie m : realComingSoon) {
            if (count >= 3) break;
            CinemaMovieSchedule schedule = new CinemaMovieSchedule(m, "SẮP CHIẾU", new ArrayList<>());
            layoutComingSoon.addView(buildScheduleCard(schedule, false));
            count++;
        }

        if (realComingSoon.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có thông tin phim sắp chiếu.");
            tvEmpty.setTextColor(Color.parseColor("#666666"));
            tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, dp(20), 0, dp(20));
            layoutComingSoon.addView(tvEmpty);
        }
    }

    private View buildScheduleCard(CinemaMovieSchedule schedule, boolean bookable) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);
        card.setRadius(dp(18));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E6E6E6")));
        card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

        card.setOnClickListener(v -> {
            if (schedule.movie != null) {
                openMovieDetail(schedule.movie);
            }
        });

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleGroup = new LinearLayout(this);
        titleGroup.setOrientation(LinearLayout.VERTICAL);
        titleGroup.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText(schedule.movie != null ? schedule.movie.title : "Phim");
        title.setTextColor(Color.parseColor("#111111"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);

        TextView meta = new TextView(this);
        String genresText = "";
        if (schedule.movie != null && schedule.movie.genres != null && !schedule.movie.genres.isEmpty()) {
            genresText = TextUtils.join(" - ", schedule.movie.genres);
        }
        String metaStr = genresText + (schedule.movie != null && schedule.movie.durationMinutes > 0 ? " • " + schedule.movie.durationMinutes + " phút" : "");
        meta.setText(metaStr);
        meta.setTextColor(Color.parseColor("#666666"));
        meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        metaParams.setMargins(0, dp(4), 0, 0);
        meta.setLayoutParams(metaParams);

        titleGroup.addView(title);
        titleGroup.addView(meta);

        TextView age = new TextView(this);
        String ageRatingStr = (schedule.movie != null && schedule.movie.ageRating != null) ? schedule.movie.ageRating : "P";
        age.setText(ageRatingStr);
        age.setGravity(Gravity.CENTER);
        age.setTextColor(Color.parseColor("#B56B00"));
        age.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        age.setTypeface(age.getTypeface(), Typeface.BOLD);
        age.setBackgroundColor(Color.parseColor("#FFF4D8"));
        age.setPadding(dp(10), 0, dp(10), 0);
        age.setMinHeight(dp(28));

        header.addView(titleGroup);
        header.addView(age);
        body.addView(header);

        TextView format = new TextView(this);
        LinearLayout.LayoutParams formatParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(28)
        );
        formatParams.setMargins(0, dp(12), 0, 0);
        format.setLayoutParams(formatParams);
        format.setGravity(Gravity.CENTER);
        format.setPadding(dp(10), 0, dp(10), 0);
        format.setText(schedule.format);
        format.setTextColor(Color.parseColor("#1E1A23"));
        format.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        format.setTypeface(format.getTypeface(), Typeface.BOLD);
        format.setBackgroundResource(R.drawable.bg_cinema_status);
        body.addView(format);

        LinearLayout timesWrap = new LinearLayout(this);
        LinearLayout.LayoutParams timesWrapParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        timesWrapParams.setMargins(0, dp(12), 0, 0);
        timesWrap.setLayoutParams(timesWrapParams);
        timesWrap.setOrientation(LinearLayout.VERTICAL);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        int totalShowtimes = schedule.showtimes != null ? schedule.showtimes.size() : 0;
        if (totalShowtimes > 0) {
            for (int start = 0; start < totalShowtimes; start += 3) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                int end = Math.min(start + 3, totalShowtimes);
                for (int index = start; index < end; index++) {
                    Showtime st = schedule.showtimes.get(index);
                    String timeVal = sdf.format(new Date(st.startAt));
                    MaterialButton timeButton = buildTimeButton(schedule.movie, st, timeVal, bookable);
                    row.addView(timeButton);
                }
                timesWrap.addView(row);
            }
        } else {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            MaterialButton timeButton = buildTimeButton(schedule.movie, null, "Sắp cập nhật", false);
            row.addView(timeButton);
            timesWrap.addView(row);
        }

        body.addView(timesWrap);
        card.addView(body);
        return card;
    }

    private MaterialButton buildTimeButton(Movie movie, Showtime showtime, String time, boolean bookable) {
        MaterialButton button = new MaterialButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(40), 1f);
        params.setMargins(0, 0, dp(8), dp(8));
        button.setLayoutParams(params);
        button.setText(time);
        button.setAllCaps(false);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setCornerRadius(dp(12));
        button.setStrokeWidth(dp(1));
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor(bookable ? "#D8D8D8" : "#E5E7EB")));
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(bookable ? "#FFFFFF" : "#F8FAFC")));
        button.setTextColor(Color.parseColor(bookable ? "#111111" : "#9CA3AF"));
        button.setEnabled(bookable);
        if (bookable && showtime != null && movie != null) {
            button.setOnClickListener(v -> {
                Intent intent = new Intent(this, SeatSelectionActivity.class);
                intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME_ID, showtime.showtimeId);
                intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE_TITLE, movie.title);
                intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE_ID, movie.movieId);
                intent.putExtra(SeatSelectionActivity.EXTRA_POSTER_URL, movie.posterUrl);
                intent.putExtra(SeatSelectionActivity.EXTRA_CINEMA_NAME, cinemaName);
                intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME_START, showtime.startAt);
                intent.putExtra(SeatSelectionActivity.EXTRA_BASE_PRICE, showtime.basePrice);
                startActivity(intent);
            });
        } else {
            button.setOnClickListener(v -> {
                if (movie != null) {
                    openMovieDetail(movie);
                }
            });
        }
        return button;
    }

    private boolean hasCoordinate() {
        return latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0
                && (latitude != 0.0 || longitude != 0.0);
    }

    private boolean isBookableStatus(String status) {
        if (TextUtils.isEmpty(status)) {
            return true;
        }

        return "active".equalsIgnoreCase(status)
                || "available".equalsIgnoreCase(status)
                || "scheduled".equalsIgnoreCase(status);
    }

    private String buildLocationText() {
        if (!TextUtils.isEmpty(district) && !TextUtils.isEmpty(city)) {
            return district + ", " + city;
        }
        if (!TextUtils.isEmpty(city)) {
            return city;
        }
        if (!TextUtils.isEmpty(district)) {
            return district;
        }
        return "Khu vực đang cập nhật";
    }

    private String safe(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class CinemaMovieSchedule {
        final Movie movie;
        final String format;
        final List<Showtime> showtimes;

        CinemaMovieSchedule(Movie movie, String format, List<Showtime> showtimes) {
            this.movie = movie;
            this.format = format;
            this.showtimes = showtimes;
        }
    }
}
