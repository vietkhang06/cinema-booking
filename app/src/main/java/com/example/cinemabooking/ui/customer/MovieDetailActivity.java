package com.example.cinemabooking.ui.customer;

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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.data.repository.ShowtimeRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.model.Showtime;
import com.example.cinemabooking.domain.repository.MovieRepository;
import com.example.cinemabooking.domain.usecase.movie.GetMovieByIdUseCase;
import com.example.cinemabooking.ui.customer.model.MovieDetailScheduleCatalog;
import com.example.cinemabooking.ui.customer.model.MovieDetailScheduleCatalog.CinemaSection;
import com.example.cinemabooking.ui.customer.model.MovieDetailScheduleCatalog.DateOption;
import com.example.cinemabooking.ui.customer.model.MovieDetailScheduleCatalog.ShowtimeGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

// Import thêm cho chức năng Đánh giá + Bình luận
import com.example.cinemabooking.domain.model.Review;
import com.example.cinemabooking.domain.repository.ReviewRepository;
import com.example.cinemabooking.data.repository.ReviewRepositoryImpl;
import com.example.cinemabooking.ui.customer.adapter.ReviewAdapter;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.domain.repository.BookingRepository;
import com.example.cinemabooking.data.repository.BookingRepositoryImpl;
import com.example.cinemabooking.service.BookingService;
import com.example.cinemabooking.di.ServiceProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.RatingBar;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MovieDetailActivity extends BaseActivity {

    public static final String EXTRA_MOVIE_ID = "extra_movie_id";
    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String EXTRA_MOVIE_POSTER_URL = "extra_movie_poster_url";
    public static final String EXTRA_MOVIE_RATING = "extra_movie_rating";
    public static final String EXTRA_MOVIE_AGE_RATING = "extra_movie_age_rating";
    public static final String EXTRA_MOVIE_DURATION = "extra_movie_duration";
    public static final String EXTRA_MOVIE_RELEASE_DATE = "extra_movie_release_date";
    public static final String EXTRA_MOVIE_DESCRIPTION = "extra_movie_description";
    public static final String EXTRA_MOVIE_TRAILER_URL = "extra_movie_trailer_url";
    public static final String EXTRA_MOVIE_TAGLINE = "extra_movie_tagline";

    public static final String EXTRA_BOOKING_MOVIE_ID = "extra_booking_movie_id";
    public static final String EXTRA_BOOKING_MOVIE_TITLE = "extra_booking_movie_title";
    public static final String EXTRA_BOOKING_CITY = "extra_booking_city";
    public static final String EXTRA_BOOKING_CINEMA = "extra_booking_cinema";
    public static final String EXTRA_BOOKING_DATE_LABEL = "extra_booking_date_label";
    public static final String EXTRA_BOOKING_DATE_TEXT = "extra_booking_date_text";
    public static final String EXTRA_BOOKING_ROOM_TYPE = "extra_booking_room_type";
    public static final String EXTRA_BOOKING_SHOWTIME = "extra_booking_showtime";

    private NestedScrollView scrollMovieDetail;

    private ImageView imgHeroBackdrop;
    private ImageView imgPosterThumb;

    private ImageView btnBack;
    private ImageView btnShare;
    private MaterialCardView btnPlayTrailer;

    private TextView tvMovieTitle;
    private TextView tvMovieTagline;
    private TextView tvRating;
    private TextView tvAgeRating;
    private TextView tvDuration;
    private TextView tvReleaseDate;
    private TextView tvSynopsis;

    private MaterialButtonToggleGroup toggleSections;
    private MaterialButton btnTabSchedule;
    private MaterialButton btnTabInfo;
    private MaterialButton btnTabReviews; // Đổi từ btnTabNews

    private LinearLayout layoutScheduleSection;
    private LinearLayout layoutInfoSection;
    private LinearLayout layoutReviewsSection; // Đổi từ layoutNewsSection

    // Khai báo thêm các View cho Đánh giá & Bình luận
    private TextView tvAvgRatingScore;
    private RatingBar rbAvgStars;
    private TextView tvTotalRatingsCount;
    private MaterialCardView cardWriteReview;
    private TextView tvWriteReviewHint;
    private RecyclerView rvReviews;

    private ReviewAdapter reviewAdapter;
    private ReviewRepository reviewRepository;
    private BookingService bookingService;
    private boolean hasWatchedMovie = false;

    private MaterialAutoCompleteTextView actvCity;
    private MaterialAutoCompleteTextView actvCinema;
    private LinearLayout layoutDateChips;
    private LinearLayout layoutCinemaGroups;

    private MaterialButton btnBookTickets;

    private MovieDetailScheduleCatalog scheduleCatalog;
    private GetMovieByIdUseCase getMovieByIdUseCase;
    private ShowtimeRepositoryImpl showtimeRepository;
    private CinemaRepositoryImpl cinemaRepository;
    private final Map<String, Cinema> cinemaMap = new HashMap<>();

    private String selectedMovieId = "";
    private String selectedMoviePosterUrl = "";
    private String selectedTrailerUrl = "";
    private String selectedCity = "";
    private String selectedCinema = "";
    private String selectedRoomType = "";
    private int selectedDateIndex = 0;
    private String selectedDateLabel = "";
    private String selectedDateText = "";
    private String selectedShowtime = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        initViews();
        initUseCase();
        initScheduleCatalog();
        bindFallbackExtras();
        setupDropdowns();
        renderDateChips();
        renderCinemaGroups();
        setupTabs();
        setupActions();
        loadMovieFromFirestore();
    }

    private void initViews() {
        scrollMovieDetail = findViewById(R.id.scrollMovieDetail);

        imgHeroBackdrop = findViewById(R.id.imgHeroBackdrop);
        imgPosterThumb = findViewById(R.id.imgPosterThumb);

        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnPlayTrailer = findViewById(R.id.btnPlayTrailer);

        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvMovieTagline = findViewById(R.id.tvMovieTagline);
        tvRating = findViewById(R.id.tvRating);
        tvAgeRating = findViewById(R.id.tvAgeRating);
        tvDuration = findViewById(R.id.tvDuration);
        tvReleaseDate = findViewById(R.id.tvReleaseDate);
        tvSynopsis = findViewById(R.id.tvSynopsis);

        toggleSections = findViewById(R.id.toggleSections);
        btnTabSchedule = findViewById(R.id.btnTabSchedule);
        btnTabInfo = findViewById(R.id.btnTabInfo);
        btnTabReviews = findViewById(R.id.btnTabReviews);

        layoutScheduleSection = findViewById(R.id.layoutScheduleSection);
        layoutInfoSection = findViewById(R.id.layoutInfoSection);
        layoutReviewsSection = findViewById(R.id.layoutReviewsSection);

        // Khởi tạo các View cho Đánh giá & Bình luận
        tvAvgRatingScore = findViewById(R.id.tvAvgRatingScore);
        rbAvgStars = findViewById(R.id.rbAvgStars);
        tvTotalRatingsCount = findViewById(R.id.tvTotalRatingsCount);
        cardWriteReview = findViewById(R.id.cardWriteReview);
        tvWriteReviewHint = findViewById(R.id.tvWriteReviewHint);
        rvReviews = findViewById(R.id.rvReviews);

        actvCity = findViewById(R.id.actvCity);
        actvCinema = findViewById(R.id.actvCinema);
        layoutDateChips = findViewById(R.id.layoutDateChips);
        layoutCinemaGroups = findViewById(R.id.layoutCinemaGroups);

        btnBookTickets = findViewById(R.id.btnBookTickets);
        btnBookTickets.setVisibility(View.GONE);
    }

    private void initUseCase() {
        MovieRepository movieRepository = new MovieRepositoryImpl(new MovieRemoteDataSource());
        getMovieByIdUseCase = new GetMovieByIdUseCase(movieRepository);
        showtimeRepository = new ShowtimeRepositoryImpl(true);
        cinemaRepository = new CinemaRepositoryImpl();

        // Khởi tạo Review repo và Booking service
        reviewRepository = new ReviewRepositoryImpl();
        bookingService = ServiceProvider.getInstance().getBookingService();

        // Khởi tạo Adapter và RecyclerView cho Đánh giá
        reviewAdapter = new ReviewAdapter(this, this::showReplyDialog);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
    }

    private void initScheduleCatalog() {
        scheduleCatalog = MovieDetailScheduleCatalog.createDefault();
    }

    private void bindFallbackExtras() {
        Intent intent = getIntent();

        selectedMovieId = safe(intent.getStringExtra(EXTRA_MOVIE_ID), selectedMovieId);
        selectedTrailerUrl = safe(intent.getStringExtra(EXTRA_MOVIE_TRAILER_URL), selectedTrailerUrl);

        String title = safe(intent.getStringExtra(EXTRA_MOVIE_TITLE), "Phim");
        String posterUrl = safe(intent.getStringExtra(EXTRA_MOVIE_POSTER_URL), "");
        String rating = safe(intent.getStringExtra(EXTRA_MOVIE_RATING), "8.5");
        String ageRating = safe(intent.getStringExtra(EXTRA_MOVIE_AGE_RATING), "T13");
        String duration = safe(intent.getStringExtra(EXTRA_MOVIE_DURATION), "103 phút");
        String releaseDate = safe(intent.getStringExtra(EXTRA_MOVIE_RELEASE_DATE), "20/04/2026");
        String description = safe(intent.getStringExtra(EXTRA_MOVIE_DESCRIPTION), "Phần mô tả phim sẽ được cập nhật từ Firestore.");
        String tagline = safe(intent.getStringExtra(EXTRA_MOVIE_TAGLINE), "Khám phá lịch chiếu và suất vé");

        tvMovieTitle.setText(title);
        tvMovieTagline.setText(tagline);
        tvRating.setText("★ " + rating);
        tvAgeRating.setText(ageRating);
        tvDuration.setText("⏱ " + duration);
        tvReleaseDate.setText("📅 " + releaseDate);
        tvSynopsis.setText(description);
        selectedMoviePosterUrl = posterUrl;
        loadImage(posterUrl);
    }

    private void loadMovieFromFirestore() {
        if (TextUtils.isEmpty(selectedMovieId)) {
            return;
        }

        getMovieByIdUseCase.execute(selectedMovieId, new ResultCallback<Movie>() {
            @Override
            public void onSuccess(Movie movie) {
                if (movie != null) {
                    bindMovie(movie);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // giữ fallback từ intent
            }
        });

        loadShowtimesFromFirestore();
    }

    private void loadShowtimesFromFirestore() {
        if (TextUtils.isEmpty(selectedMovieId)) {
            return;
        }

        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> cinemas) {
                cinemaMap.clear();
                for (Cinema c : cinemas) {
                    if (c.cinemaId != null) cinemaMap.put(c.cinemaId, c);
                }
                Log.d("MovieDetail", "cinemaMap loaded: " + cinemaMap.size() + " cinemas");
                loadShowtimesForMovie();
            }

            @Override
            public void onError(String message) {
                // Ngay cả khi cinema load lỗi, vẫn tiếp tục load showtime với cinemaMap hiện tại
                Log.w("MovieDetail", "Error loading cinemas (will proceed anyway): " + message);
                loadShowtimesForMovie();
            }
        });
    }

    private void loadShowtimesForMovie() {
        showtimeRepository.getShowtimesByMovieId(selectedMovieId, new ResultCallback<List<Showtime>>() {
            @Override
            public void onSuccess(List<Showtime> showtimes) {
                Log.d("MovieDetail", "Showtimes loaded: " + showtimes.size() + " for movieId=" + selectedMovieId);

                // Nếu cinemaMap rỗng nhưng có showtimes, tạo placeholder Cinema từ cinemaId
                // Đảm bảo UI không bị trống hoàn toàn dù cinema API thất bại
                if (cinemaMap.isEmpty() && !showtimes.isEmpty()) {
                    for (Showtime s : showtimes) {
                        if (s.cinemaId != null && !cinemaMap.containsKey(s.cinemaId)) {
                            Cinema placeholder = new Cinema();
                            placeholder.cinemaId = s.cinemaId;
                            placeholder.name = "Rạp " + s.cinemaId;
                            placeholder.city = "Khác";
                            cinemaMap.put(s.cinemaId, placeholder);
                        }
                    }
                    Log.w("MovieDetail", "Using placeholder cinemas: " + cinemaMap.size());
                }

                scheduleCatalog.buildFromShowtimes(showtimes, cinemaMap);

                List<DateOption> dateOptions = scheduleCatalog.getDateOptions();
                Log.d("MovieDetail", "DateOptions count: " + dateOptions.size());

                if (!dateOptions.isEmpty()) {
                    selectedDateIndex = 0;
                    DateOption firstOption = dateOptions.get(0);
                    selectedDateLabel = firstOption.label;
                    selectedDateText = firstOption.dateText;
                    scheduleCatalog.selectDateKey(firstOption.dateKey);

                    List<String> cities = scheduleCatalog.getCityNames();
                    Log.d("MovieDetail", "Cities: " + cities);
                    if (!cities.isEmpty()) {
                        selectedCity = cities.get(0);
                        List<String> cinemasInCity = scheduleCatalog.getCinemaNames(selectedCity);
                        if (!cinemasInCity.isEmpty()) {
                            selectedCinema = cinemasInCity.get(0);
                            scheduleCatalog.setExpandedCinema(selectedCity, selectedCinema);
                        }
                    }
                } else {
                    selectedDateIndex = -1;
                    selectedDateLabel = "";
                    selectedDateText = "";
                    selectedCity = "";
                    selectedCinema = "";
                }

                // Update the dropdowns and UI
                setupDropdowns();
                renderDateChips();
                renderCinemaGroups();
            }

            @Override
            public void onError(String message) {
                Log.e("MovieDetail", "Error loading showtimes: " + message);
            }
        });
    }

    private void bindMovie(Movie movie) {
        if (movie == null) {
            return;
        }

        selectedMovieId = safe(movie.movieId, selectedMovieId);
        selectedTrailerUrl = safe(movie.trailerUrl, selectedTrailerUrl);

        tvMovieTitle.setText(safe(movie.title, tvMovieTitle.getText().toString()));
        tvMovieTagline.setText(buildTagline(movie));
        tvRating.setText("★ " + formatRating(movie.ratingAvg));
        tvAgeRating.setText(safe(movie.ageRating, "T13"));
        tvDuration.setText("⏱ " + movie.durationMinutes + " phút");

        if (movie.releaseDate > 0) {
            tvReleaseDate.setText("📅 " + formatReleaseDate(movie.releaseDate));
        }

        tvSynopsis.setText(safe(movie.description, tvSynopsis.getText().toString()));
        selectedMoviePosterUrl = movie.posterUrl;
        loadImage(movie.posterUrl);
    }

    private void loadImage(String posterUrl) {
        if (TextUtils.isEmpty(posterUrl)) {
            Glide.with(this).load(R.drawable.login_icon).into(imgHeroBackdrop);
            Glide.with(this).load(R.drawable.login_icon).into(imgPosterThumb);
            return;
        }

        Glide.with(this)
                .load(posterUrl)
                .placeholder(R.drawable.login_icon)
                .error(R.drawable.login_icon)
                .into(imgHeroBackdrop);

        Glide.with(this)
                .load(posterUrl)
                .placeholder(R.drawable.login_icon)
                .error(R.drawable.login_icon)
                .into(imgPosterThumb);
    }

    private void setupDropdowns() {
        actvCity.setAdapter(createCenteredAdapter(scheduleCatalog.getCityNames()));
        actvCity.setText(selectedCity, false);

        actvCinema.setAdapter(createCenteredAdapter(scheduleCatalog.getCinemaNames(selectedCity)));
        actvCinema.setText(selectedCinema, false);

        actvCity.setOnClickListener(v -> actvCity.showDropDown());
        actvCinema.setOnClickListener(v -> actvCinema.showDropDown());

        actvCity.setOnItemClickListener((parent, view, position, id) -> {
            String city = scheduleCatalog.getCityNames().get(position);
            onCitySelected(city);
        });

        actvCinema.setOnItemClickListener((parent, view, position, id) -> {
            List<String> cinemas = scheduleCatalog.getCinemaNames(selectedCity);
            if (position >= 0 && position < cinemas.size()) {
                onCinemaSelected(cinemas.get(position));
            }
        });
    }

    private ArrayAdapter<String> createCenteredAdapter(List<String> values) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setGravity(Gravity.CENTER);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setGravity(Gravity.CENTER);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                return tv;
            }
        };
    }

    private void onCitySelected(String city) {
        selectedCity = city;
        actvCity.setText(city, false);

        selectedCinema = getFirstCinemaName(city);
        selectedRoomType = getFirstRoomType(city, selectedCinema);
        selectedShowtime = getFirstShowtime(city, selectedCinema, selectedRoomType);

        scheduleCatalog.setExpandedCinema(city, selectedCinema);

        refreshCinemaDropdown();
        renderDateChips();
        renderCinemaGroups();
    }

    private void onCinemaSelected(String cinemaName) {
        selectedCinema = cinemaName;
        selectedRoomType = getFirstRoomType(selectedCity, cinemaName);
        selectedShowtime = getFirstShowtime(selectedCity, cinemaName, selectedRoomType);

        actvCinema.setText(cinemaName, false);
        scheduleCatalog.setExpandedCinema(selectedCity, cinemaName);
        renderCinemaGroups();
    }

    private void refreshCinemaDropdown() {
        List<String> cinemas = scheduleCatalog.getCinemaNames(selectedCity);
        actvCinema.setAdapter(createCenteredAdapter(cinemas));
        if (!cinemas.isEmpty()) {
            actvCinema.setText(selectedCinema, false);
        } else {
            actvCinema.setText("", false);
        }
    }

    private void renderDateChips() {
        layoutDateChips.removeAllViews();

        List<DateOption> dateOptions = scheduleCatalog.getDateOptions();
        if (dateOptions.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Chưa có lịch ngày.");
            empty.setTextColor(Color.parseColor("#555555"));
            layoutDateChips.addView(empty);
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
            chip.setStrokeColor(ColorStateList.valueOf(Color.parseColor(selected ? "#1E4F8F" : "#D8D8D8")));
            chip.setCardBackgroundColor(Color.parseColor(selected ? "#1E4F8F" : "#FFFFFF"));

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setGravity(Gravity.CENTER);
            content.setPadding(dp(8), dp(8), dp(8), dp(8));

            TextView label = new TextView(this);
            label.setText(option.label);
            label.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#111111"));
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            label.setTypeface(label.getTypeface(), Typeface.BOLD);
            label.setGravity(Gravity.CENTER);
            label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            TextView date = new TextView(this);
            date.setText(option.dateText);
            date.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#111111"));
            date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            date.setTypeface(date.getTypeface(), Typeface.BOLD);
            date.setGravity(Gravity.CENTER);
            date.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            content.addView(label);
            content.addView(date);
            chip.addView(content);

            final int index = i;
            chip.setOnClickListener(v -> selectDate(index));

            layoutDateChips.addView(chip);
        }
    }

    private void selectDate(int index) {
        List<DateOption> dateOptions = scheduleCatalog.getDateOptions();
        if (index < 0 || index >= dateOptions.size()) {
            return;
        }

        selectedDateIndex = index;
        DateOption option = dateOptions.get(index);
        selectedDateLabel = option.label;
        selectedDateText = option.dateText;

        scheduleCatalog.selectDateKey(option.dateKey);

        List<String> cities = scheduleCatalog.getCityNames();
        if (!cities.isEmpty()) {
            selectedCity = cities.get(0);
            List<String> cinemas = scheduleCatalog.getCinemaNames(selectedCity);
            if (!cinemas.isEmpty()) {
                selectedCinema = cinemas.get(0);
                scheduleCatalog.setExpandedCinema(selectedCity, selectedCinema);
                selectedRoomType = getFirstRoomType(selectedCity, selectedCinema);
                selectedShowtime = getFirstShowtime(selectedCity, selectedCinema, selectedRoomType);
            } else {
                selectedCinema = "";
                selectedRoomType = "";
                selectedShowtime = "";
            }
        } else {
            selectedCity = "";
            selectedCinema = "";
            selectedRoomType = "";
            selectedShowtime = "";
        }

        refreshCinemaDropdown();
        renderDateChips();
        renderCinemaGroups();
    }

    private void renderCinemaGroups() {
        layoutCinemaGroups.removeAllViews();

        List<CinemaSection> sections = scheduleCatalog.getCinemas(selectedCity);
        if (sections == null || sections.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Chưa có dữ liệu rạp cho khu vực này.");
            empty.setTextColor(Color.parseColor("#555555"));
            empty.setGravity(Gravity.CENTER);
            empty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layoutCinemaGroups.addView(empty);
            return;
        }

        for (CinemaSection section : sections) {
            boolean expanded = section.expanded || section.name.equals(selectedCinema);

            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, dp(12));
            card.setLayoutParams(cardParams);
            card.setRadius(dp(16));
            card.setCardElevation(dp(0));
            card.setStrokeWidth(dp(1));
            card.setStrokeColor(ColorStateList.valueOf(Color.parseColor(section.name.equals(selectedCinema) ? "#1E1A23" : "#E6E6E6")));
            card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setPadding(dp(16), dp(16), dp(16), dp(16));

            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);

            TextView name = new TextView(this);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            name.setLayoutParams(nameParams);
            name.setText(section.name);
            name.setTextColor(Color.parseColor("#111111"));
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            name.setTypeface(name.getTypeface(), Typeface.BOLD);
            name.setGravity(Gravity.CENTER_VERTICAL);

            TextView arrow = new TextView(this);
            arrow.setText(expanded ? "▴" : "▾");
            arrow.setTextColor(Color.parseColor("#777777"));
            arrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);

            header.addView(name);
            header.addView(arrow);

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setVisibility(expanded ? View.VISIBLE : View.GONE);

            for (ShowtimeGroup group : section.groups) {
                TextView groupTitle = new TextView(this);
                LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                groupParams.setMargins(0, dp(14), 0, 0);
                groupTitle.setLayoutParams(groupParams);
                groupTitle.setText(group.title);
                groupTitle.setTextColor(Color.parseColor("#111111"));
                groupTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
                groupTitle.setTypeface(groupTitle.getTypeface(), Typeface.BOLD);

                content.addView(groupTitle);

                LinearLayout rows = new LinearLayout(this);
                rows.setOrientation(LinearLayout.VERTICAL);
                rows.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                List<MovieDetailScheduleCatalog.ShowtimeItem> times = group.showtimes;
                for (int start = 0; start < times.size(); start += 4) {
                    LinearLayout row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));

                    int end = Math.min(start + 4, times.size());
                    for (int t = start; t < end; t++) {
                        MovieDetailScheduleCatalog.ShowtimeItem item = times.get(t);
                        MaterialButton timeButton = buildTimeButton(item, section.name, group.title);
                        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                dp(36)
                        );
                        timeParams.setMargins(0, 0, dp(8), dp(8));
                        timeButton.setLayoutParams(timeParams);
                        row.addView(timeButton);
                    }

                    rows.addView(row);
                }

                content.addView(rows);

                View divider = new View(this);
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                    );
                dividerParams.setMargins(0, dp(10), 0, dp(4));
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
                content.addView(divider);
            }

            header.setOnClickListener(v -> toggleCinemaSection(section.name));

            body.addView(header);
            body.addView(content);
            card.addView(body);

            layoutCinemaGroups.addView(card);
        }
    }

    private MaterialButton buildTimeButton(MovieDetailScheduleCatalog.ShowtimeItem item, String cinemaName, String roomType) {
        MaterialButton button = new MaterialButton(this);
        button.setText(item.timeText);
        button.setAllCaps(false);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        button.setCornerRadius(dp(10));
        button.setStrokeWidth(dp(1));
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#D8D8D8")));
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        button.setTextColor(Color.parseColor("#111111"));

        boolean selected = cinemaName.equals(selectedCinema)
                && roomType.equals(selectedRoomType)
                && item.timeText.equals(selectedShowtime);

        styleTimeButton(button, selected);

        button.setOnClickListener(v -> {
            selectedCinema = cinemaName;
            selectedRoomType = roomType;
            selectedShowtime = item.timeText;

            Intent intent = new Intent(MovieDetailActivity.this, SeatSelectionActivity.class);

            intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME_ID, item.showtimeId);
            intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE_TITLE, tvMovieTitle.getText().toString());
            intent.putExtra(SeatSelectionActivity.EXTRA_POSTER_URL, selectedMoviePosterUrl);
            intent.putExtra(SeatSelectionActivity.EXTRA_CINEMA_NAME, cinemaName);
            intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME_START, item.startAt);
            intent.putExtra(SeatSelectionActivity.EXTRA_BASE_PRICE, item.basePrice);
            intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE_ID, selectedMovieId);

            startActivity(intent);
        });

        return button;
    }

    private void styleTimeButton(MaterialButton button, boolean selected) {
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(selected ? "#1E1A23" : "#FFFFFF")));
        button.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#111111"));
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#D8D8D8")));
    }

    private void toggleCinemaSection(String cinemaName) {
        List<CinemaSection> sections = scheduleCatalog.getCinemas(selectedCity);
        if (sections == null) {
            return;
        }

        for (CinemaSection section : sections) {
            if (section.name.equals(cinemaName)) {
                section.expanded = !section.expanded;
                if (section.expanded) {
                    selectedCinema = section.name;
                    selectedRoomType = getFirstRoomType(selectedCity, selectedCinema);
                    selectedShowtime = getFirstShowtime(selectedCity, selectedCinema, selectedRoomType);
                }
            } else {
                section.expanded = false;
            }
        }

        actvCinema.setText(selectedCinema, false);
        renderCinemaGroups();
    }

    private void ensureSectionExpanded(String cinemaName) {
        List<CinemaSection> sections = scheduleCatalog.getCinemas(selectedCity);
        if (sections == null) {
            return;
        }

        for (CinemaSection section : sections) {
            section.expanded = section.name.equals(cinemaName);
        }
    }

    private void ensureCitySelectionFor(String city) {
        List<String> cinemaNames = scheduleCatalog.getCinemaNames(city);
        if (cinemaNames.isEmpty()) {
            selectedCinema = "";
            selectedRoomType = "";
            selectedShowtime = "";
            return;
        }

        selectedCinema = cinemaNames.get(0);
        selectedRoomType = getFirstRoomType(city, selectedCinema);
        selectedShowtime = getFirstShowtime(city, selectedCinema, selectedRoomType);
        scheduleCatalog.setExpandedCinema(city, selectedCinema);
    }

    private void setupTabs() {
        toggleSections.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }

            updateTabUi(checkedId);
        });

        updateTabUi(R.id.btnTabSchedule);
        toggleSections.check(R.id.btnTabSchedule);
    }

    private void updateTabUi(int checkedId) {
        boolean scheduleSelected = checkedId == R.id.btnTabSchedule;
        boolean infoSelected = checkedId == R.id.btnTabInfo;
        boolean reviewsSelected = checkedId == R.id.btnTabReviews;

        layoutScheduleSection.setVisibility(scheduleSelected ? View.VISIBLE : View.GONE);
        layoutInfoSection.setVisibility(infoSelected ? View.VISIBLE : View.GONE);
        layoutReviewsSection.setVisibility(reviewsSelected ? View.VISIBLE : View.GONE);

        showBookingButton(scheduleSelected);

        applyTabStyle(btnTabSchedule, scheduleSelected);
        applyTabStyle(btnTabInfo, infoSelected);
        applyTabStyle(btnTabReviews, reviewsSelected);

        if (reviewsSelected) {
            checkBookingStatus();
            loadReviews();
        }
    }

    private void applyTabStyle(MaterialButton button, boolean selected) {
        int activeColor = Color.parseColor("#1E1A23");
        int inactiveColor = Color.WHITE;

        button.animate().cancel();
        button.animate()
                .scaleX(selected ? 1.04f : 1f)
                .scaleY(selected ? 1.04f : 1f)
                .translationY(selected ? -dp(2) : 0f)
                .setDuration(160)
                .start();

        button.setElevation(selected ? dp(4) : 0f);
        button.setBackgroundTintList(ColorStateList.valueOf(selected ? activeColor : inactiveColor));
        button.setTextColor(Color.parseColor(selected ? "#FFFFFF" : "#1E1A23"));
        button.setStrokeWidth(selected ? 0 : dp(1));
        button.setStrokeColor(ColorStateList.valueOf(activeColor));
    }

    private void showBookingButton(boolean show) {
        if (show) {
            btnBookTickets.setVisibility(View.VISIBLE);
            btnBookTickets.setAlpha(0f);
            btnBookTickets.animate().alpha(1f).setDuration(160).start();
        } else {
            btnBookTickets.animate()
                    .alpha(0f)
                    .setDuration(120)
                    .withEndAction(() -> btnBookTickets.setVisibility(View.GONE))
                    .start();
        }
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> shareMovie());

        btnPlayTrailer.setOnClickListener(v -> openTrailer());

        btnBookTickets.setOnClickListener(v -> prepareBookingPayload());

        cardWriteReview.setOnClickListener(v -> {
            if (hasWatchedMovie) {
                showWriteReviewDialog();
            } else {
                Toast.makeText(this, "Chỉ những người đã đặt vé và xem phim mới được đánh giá.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void shareMovie() {
        String title = safe(tvMovieTitle.getText().toString(), "Phim");
        String shareText = title + (TextUtils.isEmpty(selectedTrailerUrl) ? "" : "\n" + selectedTrailerUrl);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.trim());

        startActivity(Intent.createChooser(shareIntent, "Chia sẻ phim"));
    }

    private void openTrailer() {
        if (TextUtils.isEmpty(selectedTrailerUrl)) {
            showToast("Phim này chưa có trailer");
            return;
        }

        if (!selectedTrailerUrl.startsWith("http")) {
            selectedTrailerUrl = "https://" + selectedTrailerUrl;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(selectedTrailerUrl));
            startActivity(intent);
        } catch (Exception e) {
            showToast("Không thể mở trailer");
        }
    }

    private void prepareBookingPayload() {
        if (TextUtils.isEmpty(selectedCity)
                || TextUtils.isEmpty(selectedCinema)
                || TextUtils.isEmpty(selectedDateText)
                || TextUtils.isEmpty(selectedShowtime)) {
            showToast("Hãy chọn đầy đủ rạp, ngày và suất chiếu");
            return;
        }

        Intent bookingResult = buildBookingPayloadIntent();
        setResult(RESULT_OK, bookingResult);
        showToast("Dữ liệu đặt vé đã sẵn sàng cho màn tiếp theo");
    }

    public Intent buildBookingPayloadIntent() {
        Intent bookingResult = new Intent();
        bookingResult.putExtra(EXTRA_BOOKING_MOVIE_ID, selectedMovieId);
        bookingResult.putExtra(EXTRA_BOOKING_MOVIE_TITLE, tvMovieTitle.getText().toString());
        bookingResult.putExtra(EXTRA_BOOKING_CITY, selectedCity);
        bookingResult.putExtra(EXTRA_BOOKING_CINEMA, selectedCinema);
        bookingResult.putExtra(EXTRA_BOOKING_DATE_LABEL, selectedDateLabel);
        bookingResult.putExtra(EXTRA_BOOKING_DATE_TEXT, selectedDateText);
        bookingResult.putExtra(EXTRA_BOOKING_ROOM_TYPE, selectedRoomType);
        bookingResult.putExtra(EXTRA_BOOKING_SHOWTIME, selectedShowtime);
        return bookingResult;
    }

    private String buildTagline(Movie movie) {
        String genre = "Phim";
        if (movie.genres != null && !movie.genres.isEmpty()) {
            genre = movie.genres.get(0);
        }
        String language = safe(movie.language, "Vietnamese");
        return genre + " • " + language;
    }

    private String formatRating(double rating) {
        return String.format(Locale.getDefault(), "%.1f", rating);
    }

    private String formatReleaseDate(long releaseDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(releaseDate);
    }

    private String getFirstCinemaName(String city) {
        List<String> cinemas = scheduleCatalog.getCinemaNames(city);
        if (cinemas.isEmpty()) {
            return "";
        }
        return cinemas.get(0);
    }

    private String getFirstRoomType(String city, String cinemaName) {
        List<CinemaSection> sections = scheduleCatalog.getCinemas(city);
        for (CinemaSection section : sections) {
            if (section.name.equals(cinemaName) && section.groups != null && !section.groups.isEmpty()) {
                return section.groups.get(0).title;
            }
        }
        return "";
    }

    private String getFirstShowtime(String city, String cinemaName, String roomType) {
        List<CinemaSection> sections = scheduleCatalog.getCinemas(city);
        for (CinemaSection section : sections) {
            if (section.name.equals(cinemaName)) {
                for (ShowtimeGroup group : section.groups) {
                    if (group.title.equals(roomType) && group.showtimes != null && !group.showtimes.isEmpty()) {
                        return group.showtimes.get(0).timeText;
                    }
                }
            }
        }
        return "";
    }

    private String safe(String value, String fallback) {
        if (TextUtils.isEmpty(value)) {
            return fallback;
        }
        return value;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    // --- PHẦN PHƯƠNG THỨC MỚI CHO RATING + COMMENT + REPLY ---

    private void checkBookingStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            hasWatchedMovie = false;
            updateWriteReviewUi();
            return;
        }

        bookingService.getMyBookings(new ResultCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                hasWatchedMovie = false;
                long now = System.currentTimeMillis();
                for (Booking booking : bookings) {
                    boolean isCorrectMovie = selectedMovieId.equals(booking.movieId);
                    boolean isPaid = "paid".equalsIgnoreCase(booking.paymentStatus)
                            || "confirmed".equalsIgnoreCase(booking.bookingStatus)
                            || "success".equalsIgnoreCase(booking.bookingStatus);
                    boolean hasWatched = booking.showtimeStartAtSnapshot > 0 && booking.showtimeStartAtSnapshot < now;

                    if (isCorrectMovie && isPaid && hasWatched) {
                        hasWatchedMovie = true;
                        break;
                    }
                }
                updateWriteReviewUi();
            }

            @Override
            public void onError(String message) {
                hasWatchedMovie = false;
                updateWriteReviewUi();
                Log.e("MovieDetail", "Lỗi kiểm tra vé: " + message);
            }
        });
    }

    private void updateWriteReviewUi() {
        runOnUiThread(() -> {
            if (hasWatchedMovie) {
                tvWriteReviewHint.setText("Viết đánh giá & bình luận phim");
                tvWriteReviewHint.setTextColor(Color.parseColor("#1E4F8F"));
                cardWriteReview.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            } else {
                tvWriteReviewHint.setText("Chỉ người đã đặt vé và xem phim mới được đánh giá");
                tvWriteReviewHint.setTextColor(Color.parseColor("#888888"));
                cardWriteReview.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            }
        });
    }

    private void loadReviews() {
        if (TextUtils.isEmpty(selectedMovieId)) return;

        reviewRepository.getReviewsByMovieId(selectedMovieId, new ResultCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> reviews) {
                List<Review> rootReviews = new ArrayList<>();
                Map<String, List<Review>> repliesMap = new HashMap<>();

                for (Review r : reviews) {
                    if (TextUtils.isEmpty(r.parentId)) {
                        rootReviews.add(r);
                    } else {
                        List<Review> replies = repliesMap.get(r.parentId);
                        if (replies == null) {
                            replies = new ArrayList<>();
                            repliesMap.put(r.parentId, replies);
                        }
                        replies.add(r);
                    }
                }

                rootReviews.sort((r1, r2) -> Long.compare(r2.createdAt, r1.createdAt));

                List<Review> flatList = new ArrayList<>();
                int totalRatingStars = 0;
                int ratingCount = 0;

                for (Review root : rootReviews) {
                    flatList.add(root);
                    if (root.rating > 0) {
                        totalRatingStars += root.rating;
                        ratingCount++;
                    }

                    List<Review> replies = repliesMap.get(root.reviewId);
                    if (replies != null) {
                        replies.sort((r1, r2) -> Long.compare(r1.createdAt, r2.createdAt));
                        flatList.addAll(replies);
                    }
                }

                reviewAdapter.setReviews(flatList);

                double avg = ratingCount > 0 ? (double) totalRatingStars / ratingCount : 0.0;
                tvAvgRatingScore.setText(String.format(Locale.getDefault(), "%.1f", avg));
                rbAvgStars.setRating((float) avg);
                tvTotalRatingsCount.setText("(" + ratingCount + " lượt đánh giá)");

                if (ratingCount > 0) {
                    tvRating.setText("★ " + String.format(Locale.getDefault(), "%.1f", avg));
                }
            }

            @Override
            public void onError(String message) {
                Log.e("MovieDetail", "Lỗi tải bình luận: " + message);
            }
        });
    }

    private void showWriteReviewDialog() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    String name = firebaseUser.getDisplayName();
                    String avatar = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "";

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        if (!TextUtils.isEmpty(doc.getString("name"))) {
                            name = doc.getString("name");
                        }
                        if (!TextUtils.isEmpty(doc.getString("avatarUrl"))) {
                            avatar = doc.getString("avatarUrl");
                        }
                    }

                    if (TextUtils.isEmpty(name)) {
                        name = firebaseUser.getEmail() != null ? firebaseUser.getEmail().split("@")[0] : "Người dùng";
                    }

                    showReviewFormDialog(name, avatar);
                });
    }

    private void showReviewFormDialog(String name, String avatar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đánh giá & Bình luận phim");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(16), dp(20), dp(16));

        TextView labelStars = new TextView(this);
        labelStars.setText("Chọn số sao:");
        labelStars.setTextColor(Color.parseColor("#555555"));
        labelStars.setTextSize(14);
        layout.addView(labelStars);

        RatingBar ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyle);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(5.0f);
        LinearLayout.LayoutParams rbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rbParams.topMargin = dp(8);
        rbParams.bottomMargin = dp(16);
        ratingBar.setLayoutParams(rbParams);
        layout.addView(ratingBar);

        TextView labelComment = new TextView(this);
        labelComment.setText("Nội dung nhận xét:");
        labelComment.setTextColor(Color.parseColor("#555555"));
        labelComment.setTextSize(14);
        layout.addView(labelComment);

        EditText etComment = new EditText(this);
        etComment.setHint("Nhập cảm nghĩ của bạn về phim...");
        etComment.setTextSize(14);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        etParams.topMargin = dp(8);
        etComment.setLayoutParams(etParams);
        layout.addView(etComment);

        builder.setView(layout);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String commentText = etComment.getText().toString().trim();
            int stars = Math.round(ratingBar.getRating());

            if (TextUtils.isEmpty(commentText)) {
                Toast.makeText(this, "Nội dung nhận xét không được để trống.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (stars < 1 || stars > 5) {
                Toast.makeText(this, "Vui lòng đánh giá từ 1 đến 5 sao.", Toast.LENGTH_SHORT).show();
                return;
            }

            Review review = new Review();
            review.movieId = selectedMovieId;
            review.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            review.userName = name;
            review.userAvatar = avatar;
            review.rating = stars;
            review.content = commentText;
            review.movieTitleSnapshot = tvMovieTitle.getText().toString();

            reviewRepository.createReview(review, new ResultCallback<Review>() {
                @Override
                public void onSuccess(Review created) {
                    Toast.makeText(MovieDetailActivity.this, "Đã gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    loadReviews();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(MovieDetailActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showReplyDialog(Review parentReview) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để phản hồi.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    String name = firebaseUser.getDisplayName();
                    String avatar = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "";

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        if (!TextUtils.isEmpty(doc.getString("name"))) {
                            name = doc.getString("name");
                        }
                        if (!TextUtils.isEmpty(doc.getString("avatarUrl"))) {
                            avatar = doc.getString("avatarUrl");
                        }
                    }

                    if (TextUtils.isEmpty(name)) {
                        name = firebaseUser.getEmail() != null ? firebaseUser.getEmail().split("@")[0] : "Người dùng";
                    }

                    showReplyFormDialog(parentReview, name, avatar);
                });
    }

    private void showReplyFormDialog(Review parentReview, String name, String avatar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Trả lời bình luận của " + parentReview.userName);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(16), dp(20), dp(16));

        EditText etComment = new EditText(this);
        etComment.setHint("Nhập nội dung phản hồi...");
        etComment.setTextSize(14);
        layout.addView(etComment);

        builder.setView(layout);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String commentText = etComment.getText().toString().trim();
            if (TextUtils.isEmpty(commentText)) {
                Toast.makeText(this, "Nội dung phản hồi không được để trống.", Toast.LENGTH_SHORT).show();
                return;
            }

            Review review = new Review();
            review.movieId = selectedMovieId;
            review.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            review.userName = name;
            review.userAvatar = avatar;
            review.rating = 0;
            review.content = commentText;
            review.parentId = TextUtils.isEmpty(parentReview.parentId) ? parentReview.reviewId : parentReview.parentId;
            review.movieTitleSnapshot = tvMovieTitle.getText().toString();

            reviewRepository.createReview(review, new ResultCallback<Review>() {
                @Override
                public void onSuccess(Review created) {
                    Toast.makeText(MovieDetailActivity.this, "Đã gửi phản hồi thành công!", Toast.LENGTH_SHORT).show();
                    loadReviews();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(MovieDetailActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
