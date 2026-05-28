package com.example.cinemabooking.ui.admin.showtime;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.data.repository.RoomRepositoryImpl;
//import com.example.cinemabooking.data.repository.SeatRepositoryImpl;
import com.example.cinemabooking.data.repository.ShowtimeRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.model.Room;
//import com.example.cinemabooking.domain.model.SeatTemplate;
import com.example.cinemabooking.domain.model.Showtime;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminShowtimeAddEditActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView actvMovie;
    private MaterialAutoCompleteTextView actvCinema;
    private MaterialAutoCompleteTextView actvRoom;
    private MaterialAutoCompleteTextView actvFormat;
    private MaterialAutoCompleteTextView actvLanguage;

    private TextInputEditText tietDate;
    private TextInputEditText tietStartTime;
    private TextInputEditText tietEndTime;
    private TextInputEditText tietBasePrice;

    private ShowtimeRepositoryImpl showtimeRepository;
    private CinemaRepositoryImpl cinemaRepository;
    private MovieRepositoryImpl movieRepository;
    private RoomRepositoryImpl roomRepository;
    //private SeatRepositoryImpl seatRepository;

    private boolean isEditMode = false;
    private String currentShowtimeId = null;
    private Showtime currentShowtime = null;

    private final List<Movie> moviesList = new ArrayList<>();
    private final List<Cinema> cinemasList = new ArrayList<>();
    private final List<Room> roomsList = new ArrayList<>();

    private String selectedMovieId = null;
    private String selectedCinemaId = null;
    private String selectedRoomId = null;

    private int selectedYear = -1;
    private int selectedMonth = -1;
    private int selectedDay = -1;
    
    private int selectedStartHour = -1;
    private int selectedStartMinute = -1;
    
    private int selectedEndHour = -1;
    private int selectedEndMinute = -1;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_showtime_add_edit);

        showtimeRepository = new ShowtimeRepositoryImpl();
        cinemaRepository = new CinemaRepositoryImpl();
        movieRepository = new MovieRepositoryImpl(new com.example.cinemabooking.data.remote.datasource.MovieRemoteDataSource());
        roomRepository = new RoomRepositoryImpl();
        //seatRepository = new SeatRepositoryImpl();

        Intent intent = getIntent();
        if (intent.hasExtra("extra_showtime_id")) {
            isEditMode = true;
            currentShowtimeId = intent.getStringExtra("extra_showtime_id");
        }

        initViews();
        //setupDateTimePickers();
        //setupStaticDropdowns();
        //loadMetaData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        //findViewById(R.id.btnSave).setOnClickListener(v -> saveShowtime());

        actvMovie = findViewById(R.id.actvMovie);
        actvCinema = findViewById(R.id.actvCinema);
        actvRoom = findViewById(R.id.actvRoom);
        actvFormat = findViewById(R.id.actvFormat);
        actvLanguage = findViewById(R.id.actvLanguage);

        tietDate = findViewById(R.id.tietDate);
        tietStartTime = findViewById(R.id.tietStartTime);
        tietEndTime = findViewById(R.id.tietEndTime);
        tietBasePrice = findViewById(R.id.tietBasePrice);

        android.widget.TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(isEditMode ? "Cập nhật suất chiếu" : "Thêm suất chiếu mới");

        actvMovie.setOnItemClickListener((parent, view, position, id) -> {
            selectedMovieId = moviesList.get(position).movieId;
        });

        actvCinema.setOnItemClickListener((parent, view, position, id) -> {
            selectedCinemaId = cinemasList.get(position).cinemaId;
            selectedRoomId = null;
            actvRoom.setText("", false);
           // loadRoomsForCinema(selectedCinemaId);
        });

        actvRoom.setOnItemClickListener((parent, view, position, id) -> {
            selectedRoomId = roomsList.get(position).roomId;
        });
    }

//    private void setupDateTimePickers() {
//        tietDate.setOnClickListener(v -> {
//            Calendar cal = Calendar.getInstance();
//            if (selectedYear != -1) {
//                cal.set(selectedYear, selectedMonth, selectedDay);
//            }
//            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
//                selectedYear = year;
//                selectedMonth = month;
//                selectedDay = dayOfMonth;
//                tietDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
//            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
//        });
//
//        tietStartTime.setOnClickListener(v -> {
//            Calendar cal = Calendar.getInstance();
//            if (selectedStartHour != -1) {
//                cal.set(Calendar.HOUR_OF_DAY, selectedStartHour);
//                cal.set(Calendar.MINUTE, selectedStartMinute);
//            }
//            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
//                selectedStartHour = hourOfDay;
//                selectedStartMinute = minute;
//                tietStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
//            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
//        });
//
//        tietEndTime.setOnClickListener(v -> {
//            Calendar cal = Calendar.getInstance();
//            if (selectedEndHour != -1) {
//                cal.set(Calendar.HOUR_OF_DAY, selectedEndHour);
//                cal.set(Calendar.MINUTE, selectedEndMinute);
//            }
//            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
//                selectedEndHour = hourOfDay;
//                selectedEndMinute = minute;
//                tietEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
//            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
//        });
//    }
//
//    private void setupStaticDropdowns() {
//        String[] formats = {"2D", "3D", "IMAX", "4DX"};
//        actvFormat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, formats));
//
//        String[] languages = {"Phụ đề tiếng Việt", "Thuyết minh tiếng Việt", "Lồng tiếng tiếng Việt", "Original English"};
//        actvLanguage.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, languages));
//    }
//
//    private void loadMetaData() {
//        movieRepository.getAllMovies(new ResultCallback<List<Movie>>() {
//            @Override
//            public void onSuccess(List<Movie> movies) {
//                moviesList.clear();
//                moviesList.addAll(movies);
//                List<String> movieTitles = new ArrayList<>();
//                for (Movie m : movies) {
//                    movieTitles.add(m.title);
//                }
//                actvMovie.setAdapter(new ArrayAdapter<>(AdminShowtimeAddEditActivity.this,
//                        android.R.layout.simple_list_item_1, movieTitles));
//
//                loadCinemas();
//            }
//
//            @Override
//            public void onError(String message) {
//                showToast("Lỗi tải danh sách phim: " + message);
//            }
//        });
//    }
//
//    private void loadCinemas() {
//        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
//            @Override
//            public void onSuccess(List<Cinema> cinemas) {
//                cinemasList.clear();
//                cinemasList.addAll(cinemas);
//                List<String> cinemaNames = new ArrayList<>();
//                for (Cinema c : cinemas) {
//                    cinemaNames.add(c.name);
//                }
//                actvCinema.setAdapter(new ArrayAdapter<>(AdminShowtimeAddEditActivity.this,
//                        android.R.layout.simple_list_item_1, cinemaNames));
//
//                if (isEditMode) {
//                    loadCurrentShowtime();
//                }
//            }
//
//            @Override
//            public void onError(String message) {
//                showToast("Lỗi tải danh sách rạp: " + message);
//            }
//        });
//    }
//
//    private void loadRoomsForCinema(String cinemaId) {
//        roomRepository.getRoomsByCinemaId(cinemaId, new ResultCallback<List<Room>>() {
//            @Override
//            public void onSuccess(List<Room> rooms) {
//                roomsList.clear();
//                roomsList.addAll(rooms);
//                List<String> roomNames = new ArrayList<>();
//                for (Room r : rooms) {
//                    roomNames.add(r.name);
//                }
//                actvRoom.setAdapter(new ArrayAdapter<>(AdminShowtimeAddEditActivity.this,
//                        android.R.layout.simple_list_item_1, roomNames));
//
//                if (isEditMode && currentShowtime != null && cinemaId.equals(currentShowtime.cinemaId)) {
//                    for (int i = 0; i < rooms.size(); i++) {
//                        if (rooms.get(i).roomId.equals(currentShowtime.roomId)) {
//                            actvRoom.setText(rooms.get(i).name, false);
//                            selectedRoomId = rooms.get(i).roomId;
//                            break;
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onError(String message) {
//                showToast("Lỗi tải danh sách phòng: " + message);
//            }
//        });
//    }
//
//    private void loadCurrentShowtime() {
//        showtimeRepository.getShowtimeById(currentShowtimeId, new ResultCallback<Showtime>() {
//            @Override
//            public void onSuccess(Showtime showtime) {
//                currentShowtime = showtime;
//                selectedMovieId = showtime.movieId;
//                selectedCinemaId = showtime.cinemaId;
//                selectedRoomId = showtime.roomId;
//
//                // Bind movie
//                for (Movie m : moviesList) {
//                    if (m.movieId.equals(showtime.movieId)) {
//                        actvMovie.setText(m.title, false);
//                        break;
//                    }
//                }
//
//                // Bind cinema
//                for (Cinema c : cinemasList) {
//                    if (c.cinemaId.equals(showtime.cinemaId)) {
//                        actvCinema.setText(c.name, false);
//                        break;
//                    }
//                }
//
//                // Load rooms for the cinema and bind room
//                loadRoomsForCinema(showtime.cinemaId);
//
//                // Bind time fields
//                Calendar calStart = Calendar.getInstance();
//                calStart.setTimeInMillis(showtime.startAt);
//                selectedYear = calStart.get(Calendar.YEAR);
//                selectedMonth = calStart.get(Calendar.MONTH);
//                selectedDay = calStart.get(Calendar.DAY_OF_MONTH);
//                selectedStartHour = calStart.get(Calendar.HOUR_OF_DAY);
//                selectedStartMinute = calStart.get(Calendar.MINUTE);
//
//                tietDate.setText(dateFormat.format(new Date(showtime.startAt)));
//                tietStartTime.setText(timeFormat.format(new Date(showtime.startAt)));
//
//                Calendar calEnd = Calendar.getInstance();
//                calEnd.setTimeInMillis(showtime.endAt);
//                selectedEndHour = calEnd.get(Calendar.HOUR_OF_DAY);
//                selectedEndMinute = calEnd.get(Calendar.MINUTE);
//                tietEndTime.setText(timeFormat.format(new Date(showtime.endAt)));
//
//                // Bind format, lang, price
//                actvFormat.setText(showtime.format, false);
//                actvLanguage.setText(showtime.language, false);
//                tietBasePrice.setText(String.valueOf((int) showtime.basePrice));
//            }
//
//            @Override
//            public void onError(String message) {
//                showToast("Lỗi tải thông tin suất chiếu: " + message);
//                finish();
//            }
//        });
//    }
//
//    private void saveShowtime() {
//        if (selectedMovieId == null || selectedCinemaId == null || selectedRoomId == null) {
//            showToast("Vui lòng điền đầy đủ Phim, Rạp và Phòng chiếu!");
//            return;
//        }
//
//        String format = actvFormat.getText().toString();
//        String language = actvLanguage.getText().toString();
//        String priceStr = tietBasePrice.getText().toString();
//
//        if (TextUtils.isEmpty(format) || TextUtils.isEmpty(language) || TextUtils.isEmpty(priceStr)) {
//            showToast("Vui lòng điền đầy đủ định dạng, ngôn ngữ và giá vé!");
//            return;
//        }
//
//        if (selectedYear == -1 || selectedStartHour == -1 || selectedEndHour == -1) {
//            showToast("Vui lòng điền đầy đủ ngày chiếu và giờ bắt đầu/kết thúc!");
//            return;
//        }
//
//        double basePrice;
//        try {
//            basePrice = Double.parseDouble(priceStr);
//        } catch (NumberFormatException e) {
//            showToast("Giá vé không hợp lệ!");
//            return;
//        }
//
//        // Construct timestamps
//        Calendar calStart = Calendar.getInstance();
//        calStart.set(selectedYear, selectedMonth, selectedDay, selectedStartHour, selectedStartMinute, 0);
//        calStart.set(Calendar.MILLISECOND, 0);
//        long startAt = calStart.getTimeInMillis();
//
//        Calendar calEnd = Calendar.getInstance();
//        calEnd.set(selectedYear, selectedMonth, selectedDay, selectedEndHour, selectedEndMinute, 0);
//        calEnd.set(Calendar.MILLISECOND, 0);
//        long endAt = calEnd.getTimeInMillis();
//
//        if (endAt <= startAt) {
//            showToast("Giờ kết thúc phải sau giờ bắt đầu!");
//            return;
//        }
//
//        // Validate overlap rule
//        validateOverlapAndSave(startAt, endAt, format, language, basePrice);
//    }
//
//    private void validateOverlapAndSave(long startAt, long endAt, String format, String language, double basePrice) {
//        showtimeRepository.getAllShowtimes(new ResultCallback<List<Showtime>>() {
//            @Override
//            public void onSuccess(List<Showtime> showtimes) {
//                // Check if any active showtime in the same room overlaps
//                for (Showtime s : showtimes) {
//                    if (s.roomId.equals(selectedRoomId) && !s.deleted) {
//                        // Skip checking current showtime if in edit mode
//                        if (isEditMode && s.showtimeId.equals(currentShowtimeId)) {
//                            continue;
//                        }
//
//                        // Overlap condition: (startAt < s.endAt) && (endAt > s.startAt)
//                        if (startAt < s.endAt && endAt > s.startAt) {
//                            new AlertDialog.Builder(AdminShowtimeAddEditActivity.this)
//                                    .setTitle("Trùng lịch chiếu")
//                                    .setMessage("Phòng chiếu này đã được lên lịch suất chiếu khác từ " +
//                                            timeFormat.format(new Date(s.startAt)) + " đến " +
//                                            timeFormat.format(new Date(s.endAt)) + " cùng ngày.")
//                                    .setPositiveButton("Đã hiểu", null)
//                                    .show();
//                            return;
//                        }
//                    }
//                }
//
//                // Retrieve seat templates count to determine totalSeats
//                seatRepository.getSeatTemplatesByRoomId(selectedRoomId, new ResultCallback<List<SeatTemplate>>() {
//                    @Override
//                    public void onSuccess(List<SeatTemplate> templates) {
//                        int totalSeats = templates.size();
//                        if (totalSeats == 0) {
//                            // If no templates configured, fall back to default count (e.g. 64)
//                            totalSeats = 64;
//                        }
//
//                        saveToFirestore(startAt, endAt, format, language, basePrice, totalSeats);
//                    }
//
//                    @Override
//                    public void onError(String message) {
//                        // Fall back to default total seats count
//                        saveToFirestore(startAt, endAt, format, language, basePrice, 64);
//                    }
//                });
//            }
//
//            @Override
//            public void onError(String message) {
//                showToast("Lỗi kiểm tra trùng lịch chiếu: " + message);
//            }
//        });
//    }
//
//    private void saveToFirestore(long startAt, long endAt, String format, String language, double basePrice, int totalSeats) {
//        Showtime showtime = isEditMode ? currentShowtime : new Showtime();
//        showtime.movieId = selectedMovieId;
//        showtime.cinemaId = selectedCinemaId;
//        showtime.roomId = selectedRoomId;
//        showtime.startAt = startAt;
//        showtime.endAt = endAt;
//        showtime.format = format;
//        showtime.language = language;
//        showtime.basePrice = basePrice;
//        showtime.totalSeats = totalSeats;
//
//        if (!isEditMode) {
//            showtime.status = "active";
//            showtime.bookedSeatsCount = 0;
//
//            showtimeRepository.createShowtime(showtime, new ResultCallback<Showtime>() {
//                @Override
//                public void onSuccess(Showtime resultShowtime) {
//                    // Generate showtime seats automatically!
//                    seatRepository.generateSeatsForShowtime(resultShowtime.showtimeId, resultShowtime.roomId, new ResultCallback<Void>() {
//                        @Override
//                        public void onSuccess(Void result) {
//                            showToast("Thêm suất chiếu và khởi tạo ghế thành công");
//                            setResult(RESULT_OK);
//                            finish();
//                        }
//
//                        @Override
//                        public void onError(String message) {
//                            showToast("Tạo suất chiếu thành công nhưng lỗi khởi tạo sơ đồ ghế: " + message);
//                            setResult(RESULT_OK);
//                            finish();
//                        }
//                    });
//                }
//
//                @Override
//                public void onError(String message) {
//                    showToast("Thêm suất chiếu thất bại: " + message);
//                }
//            });
//        } else {
//            showtimeRepository.updateShowtime(showtime, new ResultCallback<Showtime>() {
//                @Override
//                public void onSuccess(Showtime resultShowtime) {
//                    showToast("Cập nhật suất chiếu thành công");
//                    setResult(RESULT_OK);
//                    finish();
//                }
//
//                @Override
//                public void onError(String message) {
//                    showToast("Cập nhật suất chiếu thất bại: " + message);
//                }
//            });
//        }
//    }
//
//    private void showToast(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//    }
}
