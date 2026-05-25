package com.example.cinemabooking.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
// import com.example.cinemabooking.core.navigation.AppNavigator;
import com.example.cinemabooking.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabooking.data.repository.BookingRepositoryImpl;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.data.repository.ShowtimeRepositoryImpl;
import com.example.cinemabooking.data.repository.UserRepositoryImpl;
// import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.model.Showtime;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.ui.admin.adapter.AdminFeatureAdapter;
import com.example.cinemabooking.ui.admin.cinema.AdminCinemaListActivity;
import com.example.cinemabooking.ui.admin.log.AdminAuditLogActivity;
import com.example.cinemabooking.ui.admin.model.AdminFeatureItem;
import com.example.cinemabooking.ui.admin.movie.AdminMovieListActivity;
import com.example.cinemabooking.ui.admin.promotion.AdminPromotionListActivity;
import com.example.cinemabooking.ui.admin.report.AdminReportActivity;
import com.example.cinemabooking.ui.admin.room.AdminRoomListActivity;
import com.example.cinemabooking.ui.admin.showtime.AdminShowtimeListActivity;
import com.example.cinemabooking.ui.admin.user.AdminUserManagementActivity;
import com.example.cinemabooking.ui.admin.widget.AdminHorizontalBarChartView;
import com.example.cinemabooking.ui.admin.widget.AdminLineChartView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";

    private TextView tvMoviesCount;
    private TextView tvShowtimesCount;
    private TextView tvUsersCount;
    private TextView tvRevenueCount;

    private MovieRepositoryImpl movieRepository;
    private UserRepositoryImpl userRepository;
    private BookingRepositoryImpl bookingRepository;
    private ShowtimeRepositoryImpl showtimeRepository;

    private AdminLineChartView lineChartUsers;
    private AdminHorizontalBarChartView barChartMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        ImageButton btnLogout = findViewById(R.id.btnAdminLogout);
        tvMoviesCount = findViewById(R.id.tvMoviesCount);
        tvShowtimesCount = findViewById(R.id.tvShowtimesCount);
        tvUsersCount = findViewById(R.id.tvUsersCount);
        tvRevenueCount = findViewById(R.id.tvRevenueCount);

        // Initialize Repositories
        movieRepository = new MovieRepositoryImpl(new MovieRemoteDataSource());
        userRepository = new UserRepositoryImpl();
        bookingRepository = new BookingRepositoryImpl();
        showtimeRepository = new ShowtimeRepositoryImpl();

        lineChartUsers = findViewById(R.id.lineChartUsers);
        barChartMovies = findViewById(R.id.barChartMovies);

        // Group 1: Cinema & Schedule
        RecyclerView rvCinema = findViewById(R.id.rvAdminGroupCinema);
        rvCinema.setLayoutManager(new GridLayoutManager(this, 2));
        rvCinema.setAdapter(new AdminFeatureAdapter(this, createCinemaFeatures()));

        // Group 2: Movies & Commerce
        RecyclerView rvMovies = findViewById(R.id.rvAdminGroupMovies);
        rvMovies.setLayoutManager(new GridLayoutManager(this, 2));
        rvMovies.setAdapter(new AdminFeatureAdapter(this, createMovieFeatures()));

        // Group 3: Operations & Accounts
        RecyclerView rvOperations = findViewById(R.id.rvAdminGroupOperations);
        rvOperations.setLayoutManager(new GridLayoutManager(this, 2));
        rvOperations.setAdapter(new AdminFeatureAdapter(this, createOperationFeatures()));

        loadRealStats();

//        btnLogout.setOnClickListener(v -> {
//            ServiceProvider.getInstance(getApplicationContext()).getAuthenticationService().logOut();
//            AppNavigator.goToLogin(this);
//        });
    }

    private void loadRealStats() {
        tvMoviesCount.setText("...");
        tvShowtimesCount.setText("...");
        tvUsersCount.setText("...");
        tvRevenueCount.setText("...");

        // Load Movies
        movieRepository.getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (movies != null) {
                    int count = 0;
                    for (Movie m : movies) {
                        if (!m.deleted) count++;
                    }
                    tvMoviesCount.setText(String.valueOf(count));
                    Log.d(TAG, "Fetched " + count + " active movies successfully.");
                } else {
                    tvMoviesCount.setText("0");
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching movies: " + message);
                tvMoviesCount.setText("Lỗi");
            }
        });

        // Load Showtimes
        showtimeRepository.getAllShowtimes(new ResultCallback<List<Showtime>>() {
            @Override
            public void onSuccess(List<Showtime> showtimes) {
                if (showtimes != null) {
                    int count = 0;
                    for (Showtime s : showtimes) {
                        if (!s.deleted) count++;
                    }
                    tvShowtimesCount.setText(String.valueOf(count));
                    Log.d(TAG, "Fetched " + count + " active showtimes successfully.");
                } else {
                    tvShowtimesCount.setText("0");
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching showtimes: " + message);
                tvShowtimesCount.setText("Lỗi");
            }
        });

        // Load Users
        userRepository.getAllUsers(new ResultCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users != null) {
                    int count = 0;
                    long now = System.currentTimeMillis();
                    long oneDay = 24L * 60 * 60 * 1000;
                    float[] dayCounts = new float[7];

                    for (User u : users) {
                        if (!u.deleted) {
                            count++;
                            long diff = now - u.createdAt;
                            int daysAgo = (int) (diff / oneDay);
                            if (daysAgo >= 0 && daysAgo < 7) {
                                dayCounts[6 - daysAgo]++;
                            }
                        }
                    }
                    tvUsersCount.setText(String.valueOf(count));
                    Log.d(TAG, "Fetched " + count + " active users successfully.");

                    List<Float> lineData = new ArrayList<>();
                    List<String> lineLabels = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    for (int i = 0; i < 7; i++) {
                        lineData.add(dayCounts[i]);
                        long dayTime = now - ((6 - i) * oneDay);
                        lineLabels.add(sdf.format(new Date(dayTime)));
                    }
                    if (lineChartUsers != null) {
                        lineChartUsers.setData(lineData, lineLabels);
                    }
                } else {
                    tvUsersCount.setText("0");
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching users: " + message);
                tvUsersCount.setText("Lỗi");
            }
        });

        // Load Bookings
        bookingRepository.getAllBookings(new ResultCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (bookings != null) {
                    int activeBookings = 0;
                    double totalRevenue = 0;
                    Map<String, Integer> movieSales = new HashMap<>();

                    for (Booking b : bookings) {
                        if (!b.deleted) {
                            activeBookings++;
                            // Sum paid/confirmed bookings for revenue
                            if ("confirmed".equalsIgnoreCase(b.bookingStatus) || "paid".equalsIgnoreCase(b.paymentStatus) || "completed".equalsIgnoreCase(b.paymentStatus)) {
                                totalRevenue += b.total;
                                int tickets = (b.seatCodes != null) ? b.seatCodes.size() : 1;
                                String title = (b.movieTitleSnapshot != null && !b.movieTitleSnapshot.isEmpty()) ? b.movieTitleSnapshot : "Khác";
                                movieSales.put(title, movieSales.getOrDefault(title, 0) + tickets);
                            }
                        }
                    }
                    NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    String formattedRevenue = nf.format(totalRevenue) + " đ";
                    tvRevenueCount.setText(formattedRevenue);
                    Log.d(TAG, "Fetched " + activeBookings + " active bookings successfully. Total Revenue: " + totalRevenue);

                    List<Map.Entry<String, Integer>> list = new ArrayList<>(movieSales.entrySet());
                    list.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                    List<String> barLabels = new ArrayList<>();
                    List<Float> barValues = new ArrayList<>();
                    for (int i = 0; i < Math.min(5, list.size()); i++) {
                        barLabels.add(list.get(i).getKey());
                        barValues.add((float) list.get(i).getValue());
                    }
                    if (barChartMovies != null) {
                        barChartMovies.setData(barLabels, barValues);
                    }
                } else {
                    tvRevenueCount.setText("0 đ");
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching bookings: " + message);
                tvRevenueCount.setText("Lỗi");
            }
        });
    }

    private List<AdminFeatureItem> createCinemaFeatures() {
        List<AdminFeatureItem> items = new ArrayList<>();
        items.add(new AdminFeatureItem("Rạp chiếu", "Quản lý hệ thống rạp", R.drawable.house_user_solid_full, AdminCinemaListActivity.class));
        items.add(new AdminFeatureItem("Phòng chiếu", "Quản lý phòng chiếu", R.drawable.glasses_solid_full, AdminRoomListActivity.class));
        items.add(new AdminFeatureItem("Ghế mẫu", "Cấu hình sơ đồ ghế", R.drawable.glasses_solid_full, AdminRoomListActivity.class));
        items.add(new AdminFeatureItem("Suất chiếu", "Tạo và cập nhật lịch chiếu", R.drawable.calendar_days_solid_full, AdminShowtimeListActivity.class));
        return items;
    }

    private List<AdminFeatureItem> createMovieFeatures() {
        List<AdminFeatureItem> items = new ArrayList<>();
        items.add(new AdminFeatureItem("Phim", "Quản lý danh sách phim", R.drawable.clapperboard_solid_full, AdminMovieListActivity.class));
        items.add(new AdminFeatureItem("CineShop", "Quản lý sản phẩm, combos", R.drawable.cart_shopping_solid_full, null));
        items.add(new AdminFeatureItem("Duyệt thanh toán", "Xử lý chuyển khoản & MoMo", R.drawable.clipboard_solid_full, AdminPaymentListActivity.class));
        return items;
    }

    private List<AdminFeatureItem> createOperationFeatures() {
        List<AdminFeatureItem> items = new ArrayList<>();
        items.add(new AdminFeatureItem("Người dùng", "Quản lý customer/staff/admin", R.drawable.user_solid_full, AdminUserManagementActivity.class));
        items.add(new AdminFeatureItem("Khuyến mãi", "Quản lý promotion", R.drawable.tag_solid_full, AdminPromotionListActivity.class));
        items.add(new AdminFeatureItem("Báo cáo", "Thống kê doanh thu", R.drawable.chart_line_solid_full, AdminReportActivity.class));
        items.add(new AdminFeatureItem("Nhật ký", "Audit log hệ thống", R.drawable.clipboard_solid_full, AdminAuditLogActivity.class));
        return items;
    }
}