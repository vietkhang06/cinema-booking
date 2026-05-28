package com.example.cinemabooking.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.AuthActivity;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.remote.api.CinemaApiService;
import com.example.cinemabooking.data.remote.api.MovieApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.example.cinemabooking.data.remote.api.ShowtimeApiService;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.model.Showtime;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class StaffShowtimesActivity extends AuthActivity {

    private RecyclerView showtimesRv;
    private TextView tvNoShowtimes;
    private View backBtn;
    private ShowtimeAdapter adapter;
    private final List<Showtime> showtimeList = new ArrayList<>();
    private final Map<String, String> movieTitleMap = new HashMap<>();
    private final Map<String, String> moviePosterMap = new HashMap<>();
    private final Map<String, String> cinemaNameMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_showtimes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        showtimesRv = findViewById(R.id.showtimes_rv);
        tvNoShowtimes = findViewById(R.id.tv_no_showtimes);
        backBtn = findViewById(R.id.back_btn);

        showtimesRv.setLayoutManager(new LinearLayoutManager(this));
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadData() {
        showLoading(true);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MovieApiService movieApi = RetrofitClient.getInstance().create(MovieApiService.class);
                CinemaApiService cinemaApi = RetrofitClient.getInstance().create(CinemaApiService.class);
                ShowtimeApiService showtimeApi = RetrofitClient.getInstance().create(ShowtimeApiService.class);

                // Load all movies to build lookup map
                Response<ApiResponse<List<Movie>>> movieResp = movieApi.getAllMovies(0, 100).execute();
                if (movieResp.isSuccessful() && movieResp.body() != null && movieResp.body().getData() != null) {
                    movieTitleMap.clear();
                    moviePosterMap.clear();
                    for (Movie m : movieResp.body().getData()) {
                        movieTitleMap.put(m.movieId, m.title);
                        moviePosterMap.put(m.movieId, m.posterUrl);
                    }
                }

                // Load all cinemas to build lookup map
                Response<ApiResponse<List<Cinema>>> cinemaResp = cinemaApi.getAllCinemas().execute();
                if (cinemaResp.isSuccessful() && cinemaResp.body() != null && cinemaResp.body().getData() != null) {
                    cinemaNameMap.clear();
                    for (Cinema c : cinemaResp.body().getData()) {
                        cinemaNameMap.put(c.cinemaId, c.name);
                    }
                }

                // Load showtimes
                Response<ApiResponse<List<Showtime>>> showtimeResp = showtimeApi.getAllShowtimes().execute();
                if (showtimeResp.isSuccessful() && showtimeResp.body() != null && showtimeResp.body().getData() != null) {
                    showtimeList.clear();

                    // Hiển thị tất cả showtimes từ đầu ngày hôm nay trở về sau
                    // (không giới hạn chỉ hôm nay — Staff cần thấy đầy đủ lịch chiếu)
                    long startToday = getStartOfToday();

                    for (Showtime s : showtimeResp.body().getData()) {
                        if (!s.deleted && s.startAt >= startToday) {
                            showtimeList.add(s);
                        }
                    }

                    // Sort showtimes chronologically
                    showtimeList.sort((s1, s2) -> Long.compare(s1.startAt, s2.startAt));
                }

                runOnUiThread(() -> {
                    showLoading(false);
                    if (showtimeList.isEmpty()) {
                        tvNoShowtimes.setVisibility(View.VISIBLE);
                        showtimesRv.setVisibility(View.GONE);
                    } else {
                        tvNoShowtimes.setVisibility(View.GONE);
                        showtimesRv.setVisibility(View.VISIBLE);
                        adapter = new ShowtimeAdapter(showtimeList, movieTitleMap, moviePosterMap, cinemaNameMap);
                        showtimesRv.setAdapter(adapter);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showToast("Lỗi tải dữ liệu lịch chiếu: " + e.getMessage());
                });
            }
        });
    }

    private long getStartOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ViewHolder> {
        private final List<Showtime> items;
        private final Map<String, String> movies;
        private final Map<String, String> posters;
        private final Map<String, String> cinemas;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        public ShowtimeAdapter(List<Showtime> items, Map<String, String> movies, Map<String, String> posters, Map<String, String> cinemas) {
            this.items = items;
            this.movies = movies;
            this.posters = posters;
            this.cinemas = cinemas;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_showtime_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Showtime s = items.get(position);

            holder.tvStartTime.setText(timeFormat.format(new Date(s.startAt)));

            String movieTitle = movies.getOrDefault(s.movieId, "Phim không xác định");
            holder.tvMovieTitle.setText(movieTitle);

            String cinemaName = cinemas.getOrDefault(s.cinemaId, "Rạp không xác định");
            holder.tvCinemaRoom.setText(cinemaName + " - Phòng " + s.roomId);

            holder.tvFormatLang.setText((s.format != null ? s.format : "2D") + " | " + (s.language != null ? s.language : "Phụ đề"));

            int bookedCount = s.bookedSeatsCount;
            int total = s.totalSeats > 0 ? s.totalSeats : 90;
            holder.tvSeatsCount.setText("Ghế: " + bookedCount + "/" + total);

            int progressPercent = (bookedCount * 100) / total;
            holder.pbSeats.setProgress(progressPercent);

            // Load poster image
            String posterUrl = posters.get(s.movieId);
            if (posterUrl != null && !posterUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(posterUrl)
                        .placeholder(R.drawable.login_icon)
                        .into(holder.imgPoster);
            } else {
                holder.imgPoster.setImageResource(R.drawable.login_icon);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(StaffShowtimesActivity.this, StaffCheckSeat.class);
                intent.putExtra("showtimeId", s.showtimeId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStartTime, tvMovieTitle, tvCinemaRoom, tvFormatLang, tvSeatsCount;
            ProgressBar pbSeats;
            android.widget.ImageView imgPoster;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStartTime = itemView.findViewById(R.id.tv_start_time);
                tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
                tvCinemaRoom = itemView.findViewById(R.id.tv_cinema_room);
                tvFormatLang = itemView.findViewById(R.id.tv_format_lang);
                tvSeatsCount = itemView.findViewById(R.id.tv_seats_count);
                pbSeats = itemView.findViewById(R.id.pb_seats);
                imgPoster = itemView.findViewById(R.id.img_poster);
            }
        }
    }
}
