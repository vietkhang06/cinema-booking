package com.example.cinemabooking.ui.customer.movie;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.example.cinemabooking.domain.model.Review;
import com.example.cinemabooking.domain.model.Showtime;
import com.example.cinemabooking.domain.repository.MovieRepository;
import com.example.cinemabooking.domain.usecase.movie.GetMovieByIdUseCase;
import com.example.cinemabooking.domain.usecase.review.AddReviewUseCase;
import com.example.cinemabooking.domain.usecase.review.GetReviewsByMovieUseCase;
import com.example.cinemabooking.ui.customer.SeatSelectionActivity;
import com.example.cinemabooking.ui.customer.model.MovieDetailScheduleCatalog;
import com.example.cinemabooking.ui.customer.model.MovieDetailScheduleCatalog.CinemaSection;
import com.example.cinemabooking.ui.customer.model.MovieDetailScheduleCatalog.DateOption;
import com.example.cinemabooking.ui.customer.model.MovieDetailScheduleCatalog.ShowtimeGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;


import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("SpellCheckingInspection")
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
    private MaterialButton btnTabNews;

    private LinearLayout layoutScheduleSection;
    private LinearLayout layoutInfoSection;
    private LinearLayout layoutNewsSection;

    private MaterialAutoCompleteTextView actvCity;
    private MaterialAutoCompleteTextView actvCinema;
    private LinearLayout layoutDateChips;
    private LinearLayout layoutCinemaGroups;

    private MaterialButton btnBookTickets;

    private androidx.recyclerview.widget.RecyclerView rvReviews;
    private EditText etCommentInput;
    private Button btnPostComment;

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
    /** Lưu đối tượng suất chiếu đang chọn — dùng khi nhấn nút "Đặt vé ngay" */
    private MovieDetailScheduleCatalog.ShowtimeItem selectedShowtimeItem = null;

    private AddReviewUseCase addReviewUseCase;
    private GetReviewsByMovieUseCase getReviewsByMovieUseCase;
    private com.example.cinemabooking.ui.customer.adapter.ReviewAdapter reviewAdapter;

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
        // scrollMovieDetail (đã xóa — không dùng)
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
        btnTabNews = findViewById(R.id.btnTabNews);

        layoutScheduleSection = findViewById(R.id.layoutScheduleSection);
        layoutInfoSection = findViewById(R.id.layoutInfoSection);
        layoutNewsSection = findViewById(R.id.layoutNewsSection);

        actvCity = findViewById(R.id.actvCity);
        actvCinema = findViewById(R.id.actvCinema);
        layoutDateChips = findViewById(R.id.layoutDateChips);
        layoutCinemaGroups = findViewById(R.id.layoutCinemaGroups);

        btnBookTickets = findViewById(R.id.btnBookTickets);
        btnBookTickets.setVisibility(View.GONE);

        rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        reviewAdapter = new com.example.cinemabooking.ui.customer.adapter.ReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);

        etCommentInput = findViewById(R.id.etCommentInput);
        btnPostComment = findViewById(R.id.btnPostComment);
    }

    private void initUseCase() {
        MovieRepository movieRepository = new MovieRepositoryImpl(new MovieRemoteDataSource());
        getMovieByIdUseCase = new GetMovieByIdUseCase(movieRepository);
        showtimeRepository = new ShowtimeRepositoryImpl(true);
        cinemaRepository = new CinemaRepositoryImpl();
        addReviewUseCase = appContainer.getAddReviewUseCase();
        getReviewsByMovieUseCase = appContainer.getGetReviewsByMovieUseCase();
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
        tvRating.setText(String.format("★ %s", rating));
        tvAgeRating.setText(ageRating);
        tvDuration.setText(String.format("⏱ %s", duration));
        tvReleaseDate.setText(String.format("📅 %s", releaseDate));
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
            public void onError(@NonNull String errorMessage) {
                // giữ fallback từ intent
            }
        });

        loadShowtimesFromFirestore();
        loadReviews();
    }

    private void loadShowtimesFromFirestore() {
        if (TextUtils.isEmpty(selectedMovieId)) {
            return;
        }

        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(@NonNull List<Cinema> cinemas) {
                cinemaMap.clear();
                for (Cinema c : cinemas) {
                    if (c.cinemaId != null) cinemaMap.put(c.cinemaId, c);
                }
                Log.d("MovieDetail", "cinemaMap loaded: " + cinemaMap.size() + " cinemas");
                loadShowtimesForMovie();
            }

            @Override
            public void onError(@NonNull String message) {
                // Ngay cả khi cinema load lỗi, vẫn tiếp tục load showtime với cinemaMap hiện tại
                Log.w("MovieDetail", "Error loading cinemas (will proceed anyway): " + message);
                loadShowtimesForMovie();
            }
        });
    }

    private void loadShowtimesForMovie() {
        showtimeRepository.getShowtimesByMovieId(selectedMovieId, new ResultCallback<List<Showtime>>() {
            @Override
            public void onSuccess(@NonNull List<Showtime> showtimes) {
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
                            selectedRoomType = getFirstRoomType(selectedCity, selectedCinema);
                            selectedShowtimeItem = getFirstShowtimeItem(selectedCity, selectedCinema, selectedRoomType);
                            selectedShowtime = selectedShowtimeItem != null ? selectedShowtimeItem.timeText : "";
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
            public void onError(@NonNull String message) {
                Log.e("MovieDetail", "Error loading showtimes: " + message);
            }
        });
    }

    // Hàm tải danh sách bình luận (gọi trong loadMovieFromFirestore)
    private void loadReviews() {
        getReviewsByMovieUseCase.execute(selectedMovieId, new ResultCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> data) {
                // Hiển thị data lên RecyclerView bình luận
                Log.d("Review", "Số lượng bình luận: " + data.size());
                if (reviewAdapter != null) {
                    reviewAdapter.setReviews(data);
                }
            }
            @Override
            public void onError(String message) {
                Log.e("Review", message);
            }
        });
    }

    private void postComment(String content, int rating) {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            showLoginRequiredDialog();
            return;
        }

        Review newReview = new Review();
        newReview.movieId = selectedMovieId;
        newReview.userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        newReview.content = content;
        newReview.rating = rating;
        newReview.movieTitleSnapshot = tvMovieTitle.getText().toString();

        addReviewUseCase.execute(newReview, new ResultCallback<Review>() {
            @Override
            public void onSuccess(Review data) {
                showToast("Đã đăng bình luận!");
                etCommentInput.setText("");
                loadReviews(); // Tải lại danh sách
            }
            @Override
            public void onError(String message) {
                showToast(message);
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
        tvRating.setText(String.format("★ %s", formatRating(movie.ratingAvg)));
        tvAgeRating.setText(safe(movie.ageRating, "T13"));
        tvDuration.setText(String.format("⏱ %d phút", movie.durationMinutes));

        if (movie.releaseDate > 0) {
            tvReleaseDate.setText(String.format("📅 %s", formatReleaseDate(movie.releaseDate)));
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
            List<String> cities = scheduleCatalog.getCityNames();
            if (cities != null && position >= 0 && position < cities.size()) {
                String city = cities.get(position);
                onCitySelected(city);
            }
        });

        actvCinema.setOnItemClickListener((parent, view, position, id) -> {
            List<String> cinemas = scheduleCatalog.getCinemaNames(selectedCity);
            if (cinemas != null && position >= 0 && position < cinemas.size()) {
                onCinemaSelected(cinemas.get(position));
            }
        });
    }

    private ArrayAdapter<String> createCenteredAdapter(List<String> values) {
        return new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, values) {
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setGravity(Gravity.CENTER);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setGravity(Gravity.CENTER);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                return tv;
            }
        };
    }

    private void onCitySelected(String city) {
        selectedCity = city;
        selectedShowtimeItem = null; // reset khi đổi thành phố
        actvCity.setText(city, false);

        selectedCinema = getFirstCinemaName(city);
        selectedRoomType = getFirstRoomType(city, selectedCinema);
        selectedShowtimeItem = getFirstShowtimeItem(city, selectedCinema, selectedRoomType);
        selectedShowtime = selectedShowtimeItem != null ? selectedShowtimeItem.timeText : "";

        scheduleCatalog.setExpandedCinema(city, selectedCinema);

        refreshCinemaDropdown();
        renderDateChips();
        renderCinemaGroups();
    }

    private void onCinemaSelected(String cinemaName) {
        selectedCinema = cinemaName;
        selectedRoomType = getFirstRoomType(selectedCity, cinemaName);
        selectedShowtimeItem = getFirstShowtimeItem(selectedCity, cinemaName, selectedRoomType);
        selectedShowtime = selectedShowtimeItem != null ? selectedShowtimeItem.timeText : "";

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
        selectedShowtimeItem = null; // reset khi đổi ngày
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
                selectedShowtimeItem = getFirstShowtimeItem(selectedCity, selectedCinema, selectedRoomType);
                selectedShowtime = selectedShowtimeItem != null ? selectedShowtimeItem.timeText : "";
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

            if (section.groups != null) {
                for (ShowtimeGroup group : section.groups) {
                    if (group == null) continue;
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
                    if (times == null || times.isEmpty()) continue;
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

        boolean selected = (cinemaName != null && cinemaName.equals(selectedCinema))
                && (roomType != null && roomType.equals(selectedRoomType))
                && (item != null && item.timeText != null && item.timeText.equals(selectedShowtime));

        styleTimeButton(button, selected);

        button.setOnClickListener(v -> {
            // 1. Kiểm tra xem suất chiếu này có đang được chọn hay không
            boolean isCurrentlySelected = (cinemaName != null && cinemaName.equals(selectedCinema))
                    && (roomType != null && roomType.equals(selectedRoomType))
                    && (item != null && item.timeText != null && item.timeText.equals(selectedShowtime));

            if (isCurrentlySelected) {
                // 2. Nếu đang chọn suất này rồi -> Bấm lại sẽ là BỎ CHỌN
                selectedShowtime = "";
                selectedShowtimeItem = null;
            } else {
            // Chỉ lưu lựa chọn, KHÔNG check đăng nhập — việc đó sẽ thực hiện khi nhấn "Đặt vé ngay"
            selectedCinema      = cinemaName;
            selectedRoomType    = roomType;
            selectedShowtime    = item.timeText;
            selectedShowtimeItem = item;   // lưu để dùng sau
            }
            renderCinemaGroups(); // làm mới UI nút thời gian để highlight suất đang chọn
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
                    selectedShowtimeItem = getFirstShowtimeItem(selectedCity, selectedCinema, selectedRoomType);
                    selectedShowtime = selectedShowtimeItem != null ? selectedShowtimeItem.timeText : "";
                }
            } else {
                section.expanded = false;
            }
        }

        actvCinema.setText(selectedCinema, false);
        renderCinemaGroups();
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
        boolean newsSelected = checkedId == R.id.btnTabNews;

        layoutScheduleSection.setVisibility(scheduleSelected ? View.VISIBLE : View.GONE);
        layoutInfoSection.setVisibility(infoSelected ? View.VISIBLE : View.GONE);
        layoutNewsSection.setVisibility(newsSelected ? View.VISIBLE : View.GONE);

        showBookingButton(scheduleSelected);

        applyTabStyle(btnTabSchedule, scheduleSelected);
        applyTabStyle(btnTabInfo, infoSelected);
        applyTabStyle(btnTabNews, newsSelected);
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

        btnPostComment.setOnClickListener(v -> {
            String content = etCommentInput.getText().toString().trim();

            if (TextUtils.isEmpty(content)) {
                showToast("Vui lòng nhập bình luận");
                return;
            }
            postComment(content, 0); // Rating is unused now
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
        if (TextUtils.isEmpty(selectedTrailerUrl) || "null".equalsIgnoreCase(selectedTrailerUrl.trim())) {
            showToast("Phim này chưa có trailer");
            return;
        }

        String trailerUrl = selectedTrailerUrl.trim();
        if (!trailerUrl.startsWith("http")) {
            trailerUrl = "https://" + trailerUrl;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
            startActivity(intent);
        } catch (Exception e) {
            showToast("Không thể mở trailer");
        }
    }

    private void prepareBookingPayload() {
        // 1. Kiểm tra đăng nhập trước
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            showLoginRequiredDialog();
            return;
        }

        // 2. Kiểm tra đã chọn đủ thông tin chưa
        if (selectedShowtimeItem == null
                || TextUtils.isEmpty(selectedCity)
                || TextUtils.isEmpty(selectedCinema)
                || TextUtils.isEmpty(selectedDateText)
                || TextUtils.isEmpty(selectedShowtime)) {
            showToast("Hãy chọn đầy đủ rạp, ngày và suất chiếu");
            return;
        }

        // 3. Chuyển sang màn hình chọn ghế
        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME_ID, selectedShowtimeItem.showtimeId);
        intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE_TITLE, tvMovieTitle.getText().toString());
        intent.putExtra(SeatSelectionActivity.EXTRA_POSTER_URL, selectedMoviePosterUrl);
        intent.putExtra(SeatSelectionActivity.EXTRA_CINEMA_NAME, selectedCinema);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME_START, selectedShowtimeItem.startAt);
        intent.putExtra(SeatSelectionActivity.EXTRA_BASE_PRICE, selectedShowtimeItem.basePrice);
        intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE_ID, selectedMovieId);
        startActivity(intent);
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
        if (TextUtils.isEmpty(city) || TextUtils.isEmpty(cinemaName)) {
            return "";
        }
        List<CinemaSection> sections = scheduleCatalog.getCinemas(city);
        if (sections == null) {
            return "";
        }
        for (CinemaSection section : sections) {
            if (section != null && cinemaName.equals(section.name) && section.groups != null && !section.groups.isEmpty()) {
                return section.groups.get(0).title;
            }
        }
        return "";
    }

    private MovieDetailScheduleCatalog.ShowtimeItem getFirstShowtimeItem(String city, String cinemaName, String roomType) {
        if (TextUtils.isEmpty(city) || TextUtils.isEmpty(cinemaName) || TextUtils.isEmpty(roomType)) {
            return null;
        }
        List<CinemaSection> sections = scheduleCatalog.getCinemas(city);
        if (sections == null) {
            return null;
        }
        for (CinemaSection section : sections) {
            if (section != null && cinemaName.equals(section.name) && section.groups != null) {
                for (ShowtimeGroup group : section.groups) {
                    if (group != null && roomType.equals(group.title) && group.showtimes != null && !group.showtimes.isEmpty()) {
                        return group.showtimes.get(0);
                    }
                }
            }
        }
        return null;
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

    /**
     * Hiển thị dialog yêu cầu đăng nhập khi người dùng chưa đăng nhập mà muốn đặt vé.
     * Nút "Đăng nhập" → mở LoginActivity (sau khi login xong sẽ quay về màn hình này).
     * Nút "Để sau" → đóng dialog, người dùng có thể tiếp tục xem phim.
     */
    private void showLoginRequiredDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(32), dp(24), dp(32));
        layout.setBackgroundColor(android.graphics.Color.WHITE);

        // Icon
        android.widget.ImageView icon = new android.widget.ImageView(this);
        icon.setImageResource(android.R.drawable.ic_lock_idle_lock);
        android.widget.LinearLayout.LayoutParams iconParams =
                new android.widget.LinearLayout.LayoutParams(dp(48), dp(48));
        iconParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        iconParams.bottomMargin = dp(16);
        icon.setLayoutParams(iconParams);
        layout.addView(icon);

        // Tiêu đề
        android.widget.TextView tvTitle = new android.widget.TextView(this);
        tvTitle.setText("Đăng nhập để đặt vé");
        tvTitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 20f);
        tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(android.graphics.Color.parseColor("#1A1A2E"));
        tvTitle.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams titleParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.bottomMargin = dp(8);
        tvTitle.setLayoutParams(titleParams);
        layout.addView(tvTitle);

        // Mô tả
        android.widget.TextView tvDesc = new android.widget.TextView(this);
        tvDesc.setText("Bạn cần đăng nhập để tiếp tục đặt vé. Sau khi đăng nhập, bạn sẽ được quay lại trang này.");
        tvDesc.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f);
        tvDesc.setTextColor(android.graphics.Color.parseColor("#666666"));
        tvDesc.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams descParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        descParams.bottomMargin = dp(28);
        tvDesc.setLayoutParams(descParams);
        layout.addView(tvDesc);

        // Nút Đăng nhập
        com.google.android.material.button.MaterialButton btnLogin =
                new com.google.android.material.button.MaterialButton(this);
        btnLogin.setText("Đăng nhập");
        btnLogin.setAllCaps(false);
        btnLogin.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15f);
        btnLogin.setCornerRadius(dp(12));
        btnLogin.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1E4F8F")));
        btnLogin.setTextColor(android.graphics.Color.WHITE);
        android.widget.LinearLayout.LayoutParams loginParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(50));
        loginParams.bottomMargin = dp(12);
        btnLogin.setLayoutParams(loginParams);
        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
            com.example.cinemabooking.core.navigation.AppNavigator.goToLoginForBooking(this);
        });
        layout.addView(btnLogin);

        // Nút Để sau
        com.google.android.material.button.MaterialButton btnLater =
                new com.google.android.material.button.MaterialButton(this,
                        null, android.R.attr.borderlessButtonStyle);
        btnLater.setText("Để sau");
        btnLater.setAllCaps(false);
        btnLater.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15f);
        btnLater.setTextColor(android.graphics.Color.parseColor("#888888"));
        android.widget.LinearLayout.LayoutParams laterParams =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(48));
        btnLater.setLayoutParams(laterParams);
        btnLater.setOnClickListener(v -> dialog.dismiss());
        layout.addView(btnLater);

        dialog.setContentView(layout);
        dialog.show();
    }
}
