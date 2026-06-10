package com.example.cinemabooking.ui.admin.showtime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.data.repository.RoomRepositoryImpl;
import com.example.cinemabooking.data.repository.ShowtimeRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.model.Room;
import com.example.cinemabooking.domain.model.Showtime;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminShowtimeListActivity extends AppCompatActivity implements AdminShowtimeAdapter.OnShowtimeActionListener {

    private static final String TAG = "AdminShowtimeList";
    private static final int REQUEST_CODE_ADD_EDIT = 1001;

    private MaterialAutoCompleteTextView actvCinemaChooser;
    private RecyclerView rvShowtimes;
    private TextView tvEmptyShowtimes;

    private ShowtimeRepositoryImpl showtimeRepository;
    private CinemaRepositoryImpl cinemaRepository;
    private MovieRepositoryImpl movieRepository;
    private RoomRepositoryImpl roomRepository;

    private final List<Showtime> allShowtimes = new ArrayList<>();
    private final List<Showtime> displayedShowtimes = new ArrayList<>();
    private final List<Cinema> cinemaList = new ArrayList<>();
    
    private final Map<String, Movie> movieMap = new HashMap<>();
    private final Map<String, Cinema> cinemaMap = new HashMap<>();
    private final Map<String, Room> roomMap = new HashMap<>();

    private AdminShowtimeAdapter adapter;
    private String selectedCinemaIdFilter = null; // null means "All"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_showtime_list);

        showtimeRepository = new ShowtimeRepositoryImpl();
        cinemaRepository = new CinemaRepositoryImpl();
        movieRepository = new MovieRepositoryImpl(new com.example.cinemabooking.data.remote.datasource.MovieRemoteDataSource());
        roomRepository = new RoomRepositoryImpl();

        initViews();
        loadMetaDataAndShowtimes();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        actvCinemaChooser = findViewById(R.id.actvCinemaChooser);
        rvShowtimes = findViewById(R.id.rvShowtimes);
        tvEmptyShowtimes = findViewById(R.id.tvEmptyShowtimes);

        rvShowtimes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminShowtimeAdapter(displayedShowtimes, movieMap, cinemaMap, roomMap, this);
        rvShowtimes.setAdapter(adapter);

        findViewById(R.id.btnAddShowtime).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminShowtimeAddEditActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EDIT);
        });

        actvCinemaChooser.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedCinemaIdFilter = null;
            } else {
                selectedCinemaIdFilter = cinemaList.get(position - 1).cinemaId;
            }
            filterShowtimes();
        });
    }

    private void loadMetaDataAndShowtimes() {
        // Load movies, cinemas, and rooms first to ensure we can display names correctly
        movieRepository.getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                for (Movie m : movies) {
                    movieMap.put(m.movieId, m);
                }
                loadCinemas();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải thông tin phim: " + message);
            }
        });
    }

    private void loadCinemas() {
        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> cinemas) {
                cinemaList.clear();
                cinemaList.addAll(cinemas);
                for (Cinema c : cinemas) {
                    cinemaMap.put(c.cinemaId, c);
                }

                // Populate filter dropdown
                List<String> cinemaNames = new ArrayList<>();
                cinemaNames.add("Tất cả các rạp");
                for (Cinema c : cinemas) {
                    cinemaNames.add(c.name);
                }
                ArrayAdapter<String> adapterFilter = new ArrayAdapter<>(
                        AdminShowtimeListActivity.this,
                        android.R.layout.simple_list_item_1,
                        cinemaNames
                );
                actvCinemaChooser.setAdapter(adapterFilter);

                loadRooms();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách rạp: " + message);
            }
        });
    }

    private void loadRooms() {
        roomRepository.getAllRooms(new ResultCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                for (Room r : rooms) {
                    roomMap.put(r.roomId, r);
                }
                loadShowtimes();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách phòng: " + message);
            }
        });
    }

    private void loadShowtimes() {
        showtimeRepository.getAllShowtimes(new ResultCallback<List<Showtime>>() {
            @Override
            public void onSuccess(List<Showtime> showtimes) {
                allShowtimes.clear();
                allShowtimes.addAll(showtimes);
                filterShowtimes();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách suất chiếu: " + message);
            }
        });
    }

    private void filterShowtimes() {
        displayedShowtimes.clear();
        for (Showtime s : allShowtimes) {
            if (selectedCinemaIdFilter == null || selectedCinemaIdFilter.equals(s.cinemaId)) {
                displayedShowtimes.add(s);
            }
        }
        
        adapter.notifyDataSetChanged();

        if (displayedShowtimes.isEmpty()) {
            tvEmptyShowtimes.setVisibility(View.VISIBLE);
        } else {
            tvEmptyShowtimes.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(Showtime showtime) {
        Intent intent = new Intent(this, AdminShowtimeAddEditActivity.class);
        intent.putExtra("extra_showtime_id", showtime.showtimeId);
        startActivityForResult(intent, REQUEST_CODE_ADD_EDIT);
    }

    @Override
    public void onDelete(Showtime showtime) {
        // Business Rule validation: Check if showtime has active bookings
        FirebaseFirestore.getInstance().collection(FirestoreCollections.BOOKINGS)
                .whereEqualTo("showtimeId", showtime.showtimeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int bookingCount = 0;
                    if (queryDocumentSnapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String status = doc.getString("status");
                            if (!com.example.cinemabooking.core.constants.BookingStatus.CANCELLED.equals(status)) {
                                bookingCount++;
                            }
                        }
                    }

                    if (bookingCount > 0) {
                        new AlertDialog.Builder(this)
                                .setTitle("Cảnh báo: Suất chiếu đã có khách đặt")
                                .setMessage("Suất chiếu này đang có " + bookingCount + " vé được đặt. Việc hủy sẽ tự động hủy các vé liên quan, tạo yêu cầu hoàn tiền (REFUND_PENDING) và gửi Voucher bồi thường cho khách. Bạn có chắc chắn muốn tiếp tục?")
                                .setPositiveButton("Hủy suất chiếu", (dialog, which) -> cancelShowtimeWithBookings(showtime))
                                .setNegativeButton("Quay lại", null)
                                .show();
                    } else {
                        // Confirm deletion
                        new AlertDialog.Builder(this)
                                .setTitle("Xác nhận xóa")
                                .setMessage("Bạn có chắc chắn muốn xóa suất chiếu này không?")
                                .setPositiveButton("Xóa", (dialog, which) -> deleteShowtime(showtime))
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Lỗi kiểm tra vé đặt: " + e.getMessage());
                });
    }

    private void deleteShowtime(Showtime showtime) {
        showtimeRepository.softDeleteShowtime(showtime.showtimeId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Movie movie = movieMap.get(showtime.movieId);
                String movieTitle = movie != null ? movie.title : "ID: " + showtime.movieId;
                com.example.cinemabooking.ui.admin.log.AdminAuditLogger.log(
                        "DELETE_SHOWTIME", "SHOWTIME", showtime.showtimeId, "Đã xóa suất chiếu của phim: " + movieTitle
                );
                showToast("Đã xóa suất chiếu thành công");
                loadShowtimes();
            }

            @Override
            public void onError(String message) {
                showToast("Xóa suất chiếu thất bại: " + message);
            }
        });
    }

    private void cancelShowtimeWithBookings(Showtime showtime) {
        String adminId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null 
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() 
                : "UNKNOWN_ADMIN";
                
        showtimeRepository.cancelShowtime(showtime.showtimeId, adminId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String result) {
                showToast("Đã hủy suất chiếu và xử lý hoàn tiền thành công");
                loadShowtimes();
            }

            @Override
            public void onError(String message) {
                showToast("Hủy suất chiếu thất bại: " + message);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_EDIT && resultCode == RESULT_OK) {
            loadShowtimes();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}