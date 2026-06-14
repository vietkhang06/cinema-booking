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
import java.util.Calendar;
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
    private LinearLayout layoutDateChips;

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

    private final List<Showtime> allShowtimes = new ArrayList<>();
    private final Map<String, Movie> movieMap = new HashMap<>();
    private final List<DateOption> dateOptions = new ArrayList<>();
    private int selectedDateIndex = 0;

    private static class DateOption {
        final String label;
        final String dateText;
        final String dateKey;

        DateOption(String label, String dateText, String dateKey) {
            this.label = label;
            this.dateText = dateText;
            this.dateKey = dateKey;
        }
    }

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
        initDateOptions();
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
        layoutDateChips = findViewById(R.id.layoutDateChips);

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

    private void initDateOptions() {
        dateOptions.clear();
        SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateTextFormat = new SimpleDateFormat("dd / MM", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            String label;
            if (i == 0) {
                label = "Hôm nay";
            } else if (i == 1) {
                label = "Ngày mai";
            } else {
                label = getDayLabel(cal);
            }
            String dateText = dateTextFormat.format(cal.getTime());
            String dateKey = dateKeyFormat.format(cal.getTime());
            dateOptions.add(new DateOption(label, dateText, dateKey));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void renderDateChips() {
        if (layoutDateChips == null) return;
        layoutDateChips.removeAllViews();

        if (dateOptions.isEmpty()) {
            return;
        }

        for (int i = 0; i < dateOptions.size(); i++) {
            DateOption option = dateOptions.get(i);
            boolean selected = i == selectedDateIndex;

            MaterialCardView chip = new MaterialCardView(this);
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(dp(84), dp(64));
            chipParams.setMarginEnd(dp(10));
            chip.setLayoutParams(chipParams);
            chip.setRadius(dp(12));
            chip.setCardElevation(dp(0));
            chip.setStrokeWidth(dp(1));
            chip.setStrokeColor(ColorStateList.valueOf(Color.parseColor(selected ? "#0F56B3" : "#D8D8D8")));
            chip.setCardBackgroundColor(Color.parseColor(selected ? "#0F56B3" : "#FFFFFF"));

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setGravity(Gravity.CENTER);
            content.setPadding(dp(8), dp(4), dp(8), dp(4));

            TextView label = new TextView(this);
            label.setText(option.label);
            label.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#555555"));
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            label.setTypeface(label.getTypeface(), Typeface.BOLD);
            label.setGravity(Gravity.CENTER);

            TextView dateText = new TextView(this);
            dateText.setText(option.dateText);
            dateText.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#111111"));
            dateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            dateText.setTypeface(dateText.getTypeface(), Typeface.BOLD);
            dateText.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams dateTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dateTextParams.setMargins(0, dp(2), 0, 0);
            dateText.setLayoutParams(dateTextParams);

            content.addView(label);
            content.addView(dateText);
            chip.addView(content);

            final int index = i;
            chip.setOnClickListener(v -> {
                selectedDateIndex = index;
                renderDateChips();
                renderRealShowtimes(allShowtimes, movieMap);
            });

            layoutDateChips.addView(chip);
        }
    }

    private String getDayLabel(Calendar targetCal) {
        Calendar today = Calendar.getInstance();
        if (isSameDay(targetCal, today)) {
            return "Hôm nay";
        }
        today.add(Calendar.DAY_OF_YEAR, 1);
        if (isSameDay(targetCal, today)) {
            return "Ngày mai";
        }
        return dayLabel(targetCal.get(Calendar.DAY_OF_WEEK));
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private String dayLabel(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Thứ Hai";
            case Calendar.TUESDAY:
                return "Thứ Ba";
            case Calendar.WEDNESDAY:
                return "Thứ Tư";
            case Calendar.THURSDAY:
                return "Thứ Năm";
            case Calendar.FRIDAY:
                return "Thứ Sáu";
            case Calendar.SATURDAY:
                return "Thứ Bảy";
            case Calendar.SUNDAY:
            default:
                return "Chủ Nhật";
        }
    }

    private void loadShowtimesAndMovies() {
        if (TextUtils.isEmpty(cinemaId)) {
            return;
        }

        movieRepository.getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                movieMap.clear();
                if (movies != null) {
                    for (Movie m : movies) {
                        movieMap.put(m.movieId, m);
                    }
                }

                showtimeRepository.getShowtimesByCinemaId(cinemaId, new ResultCallback<List<Showtime>>() {
                    @Override
                    public void onSuccess(List<Showtime> showtimes) {
                        allShowtimes.clear();
                        if (showtimes != null) {
                            allShowtimes.addAll(showtimes);
                        }
                        renderDateChips();
                        renderRealShowtimes(allShowtimes, movieMap);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("CinemaDetailActivity", "Failed to fetch showtimes: " + message);
                        allShowtimes.clear();
                        renderDateChips();
                        renderRealShowtimes(allShowtimes, movieMap);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.e("CinemaDetailActivity", "Failed to fetch movies: " + message);
                showtimeRepository.getShowtimesByCinemaId(cinemaId, new ResultCallback<List<Showtime>>() {
                    @Override
                    public void onSuccess(List<Showtime> showtimes) {
                        allShowtimes.clear();
                        if (showtimes != null) {
                            allShowtimes.addAll(showtimes);
                        }
                        movieMap.clear();
                        renderDateChips();
                        renderRealShowtimes(allShowtimes, movieMap);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        allShowtimes.clear();
                        movieMap.clear();
                        renderDateChips();
                        renderRealShowtimes(allShowtimes, movieMap);
                    }
                });
            }
        });
    }

    private void renderRealShowtimes(List<Showtime> showtimes, Map<String, Movie> movieMap) {
        layoutNowShowing.removeAllViews();

        if (selectedDateIndex < 0 || selectedDateIndex >= dateOptions.size()) {
            return;
        }
        String selectedDateKey = dateOptions.get(selectedDateIndex).dateKey;

        Map<String, List<Showtime>> grouped = new HashMap<>();
        long now = System.currentTimeMillis();
        SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (showtimes != null) {
            for (Showtime st : showtimes) {
                if (st.deleted) continue;

                // Filter by selected date
                String showtimeDateKey = dateKeyFormat.format(new Date(st.startAt));
                if (!showtimeDateKey.equals(selectedDateKey)) {
                    continue;
                }

                // If selected date is today, show only future/current showtimes.
                boolean isToday = selectedDateIndex == 0;
                if ((isToday && st.startAt < now) || !isBookableStatus(st.status)) {
                    continue;
                }
                if (TextUtils.isEmpty(st.movieId)) continue;

                String formatLanguage = (st.format != null ? st.format : "2D") + (st.language != null ? " " + st.language : " PHỤ ĐỀ");
                String key = st.movieId + "|" + formatLanguage.toUpperCase();
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

        // Horizontal Layout for Poster + Details
        LinearLayout horizontalLayout = new LinearLayout(this);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setGravity(Gravity.TOP);

        // Movie Poster (on the left)
        MaterialCardView posterCard = new MaterialCardView(this);
        LinearLayout.LayoutParams posterParams = new LinearLayout.LayoutParams(dp(72), dp(108));
        posterCard.setLayoutParams(posterParams);
        posterCard.setRadius(dp(8));
        posterCard.setCardElevation(0);
        posterCard.setStrokeWidth(0);

        ImageView posterView = new ImageView(this);
        posterView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        posterView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (schedule.movie != null && !TextUtils.isEmpty(schedule.movie.posterUrl)) {
            Glide.with(this)
                    .load(schedule.movie.posterUrl)
                    .placeholder(R.drawable.ic_cinemax_logo)
                    .error(R.drawable.ic_cinemax_logo)
                    .into(posterView);
        } else {
            posterView.setImageResource(R.drawable.ic_cinemax_logo);
        }
        posterCard.addView(posterView);

        // Movie Info (on the right)
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        infoParams.setMarginStart(dp(14));
        infoLayout.setLayoutParams(infoParams);

        // Title
        TextView title = new TextView(this);
        title.setText(schedule.movie != null ? schedule.movie.title : "Phim");
        title.setTextColor(Color.parseColor("#111111"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        infoLayout.addView(title);

        // Meta (Age rating, Duration, Release Date)
        LinearLayout metaLayout = new LinearLayout(this);
        metaLayout.setOrientation(LinearLayout.HORIZONTAL);
        metaLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams metaLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        metaLayoutParams.setMargins(0, dp(6), 0, 0);
        metaLayout.setLayoutParams(metaLayoutParams);

        TextView ageBadge = new TextView(this);
        String ageRatingStr = (schedule.movie != null && schedule.movie.ageRating != null) ? schedule.movie.ageRating : "P";
        ageBadge.setText(ageRatingStr);
        ageBadge.setGravity(Gravity.CENTER);
        ageBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        ageBadge.setTypeface(ageBadge.getTypeface(), Typeface.BOLD);

        int ageBgColor = Color.parseColor("#FFF0E8");
        int ageTextColor = Color.parseColor("#E06A00");
        if ("P".equalsIgnoreCase(ageRatingStr)) {
            ageBgColor = Color.parseColor("#E6FBF3");
            ageTextColor = Color.parseColor("#10B981");
        } else if ("T13".equalsIgnoreCase(ageRatingStr)) {
            ageBgColor = Color.parseColor("#FFF4D8");
            ageTextColor = Color.parseColor("#B56B00");
        } else if ("T16".equalsIgnoreCase(ageRatingStr) || "T18".equalsIgnoreCase(ageRatingStr)) {
            ageBgColor = Color.parseColor("#FEE2E2");
            ageTextColor = Color.parseColor("#EF4444");
        }
        ageBadge.setBackgroundColor(ageBgColor);
        ageBadge.setTextColor(ageTextColor);
        ageBadge.setPadding(dp(6), dp(2), dp(6), dp(2));
        metaLayout.addView(ageBadge);

        TextView durationText = new TextView(this);
        String durationStr = "⏱ " + (schedule.movie != null ? schedule.movie.durationMinutes : 0) + " Phút";
        durationText.setText(durationStr);
        durationText.setTextColor(Color.parseColor("#555555"));
        durationText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams durationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        durationParams.setMarginStart(dp(10));
        durationText.setLayoutParams(durationParams);
        metaLayout.addView(durationText);

        TextView releaseText = new TextView(this);
        String releaseDateStr = "📅 --";
        if (schedule.movie != null && schedule.movie.releaseDate > 0) {
            SimpleDateFormat releaseSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            releaseDateStr = "📅 " + releaseSdf.format(new Date(schedule.movie.releaseDate));
        }
        releaseText.setText(releaseDateStr);
        releaseText.setTextColor(Color.parseColor("#555555"));
        releaseText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LinearLayout.LayoutParams releaseParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        releaseParams.setMarginStart(dp(10));
        releaseText.setLayoutParams(releaseParams);
        metaLayout.addView(releaseText);

        infoLayout.addView(metaLayout);

        // Rating
        TextView ratingText = new TextView(this);
        double ratingVal = schedule.movie != null ? schedule.movie.ratingAvg : 0.0;
        ratingText.setText(String.format(Locale.getDefault(), "★ %.1f", ratingVal));
        ratingText.setTextColor(Color.parseColor("#FFB300"));
        ratingText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        ratingText.setTypeface(ratingText.getTypeface(), Typeface.BOLD);
        LinearLayout.LayoutParams ratingParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        ratingParams.setMargins(0, dp(6), 0, 0);
        ratingText.setLayoutParams(ratingParams);
        infoLayout.addView(ratingText);

        horizontalLayout.addView(posterCard);
        horizontalLayout.addView(infoLayout);
        body.addView(horizontalLayout);

        // Format Label
        TextView format = new TextView(this);
        LinearLayout.LayoutParams formatParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        formatParams.setMargins(0, dp(12), 0, 0);
        format.setLayoutParams(formatParams);
        format.setGravity(Gravity.CENTER);
        format.setText(schedule.format);
        format.setTextColor(Color.parseColor("#666666"));
        format.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        format.setTypeface(format.getTypeface(), Typeface.BOLD);
        body.addView(format);

        // Showtime Buttons
        LinearLayout timesWrap = new LinearLayout(this);
        LinearLayout.LayoutParams timesWrapParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        timesWrapParams.setMargins(0, dp(8), 0, 0);
        timesWrap.setLayoutParams(timesWrapParams);
        timesWrap.setOrientation(LinearLayout.VERTICAL);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        int totalShowtimes = schedule.showtimes != null ? schedule.showtimes.size() : 0;
        if (totalShowtimes > 0) {
            for (int start = 0; start < totalShowtimes; start += 4) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                int end = Math.min(start + 4, totalShowtimes);
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
