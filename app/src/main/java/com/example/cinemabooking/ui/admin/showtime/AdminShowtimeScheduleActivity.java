package com.example.cinemabooking.ui.admin.showtime;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.data.repository.RoomRepositoryImpl;
import com.example.cinemabooking.data.repository.SeatRepositoryImpl;
import com.example.cinemabooking.data.repository.ShowtimeRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.model.Room;
import com.example.cinemabooking.domain.model.SeatTemplate;
import com.example.cinemabooking.domain.model.Showtime;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AdminShowtimeScheduleActivity extends AppCompatActivity {

    private static final String TAG = "AdminShowtimeSchedule";

    private MaterialAutoCompleteTextView actvMovie;
    private MaterialAutoCompleteTextView actvCinema;
    private MaterialAutoCompleteTextView actvRoom;
    private MaterialAutoCompleteTextView actvFormat;
    private MaterialAutoCompleteTextView actvLanguage;

    private TextInputEditText tietStartDate;
    private TextInputEditText tietEndDate;
    private TextInputEditText tietBasePrice;

    private View layoutFreqOnce;
    private View layoutFreqDaily;
    private View layoutFreqWeekdays;
    private View layoutFreqWeekends;
    private View layoutFreqCustom;

    private android.widget.RadioButton rbFreqOnce;
    private android.widget.RadioButton rbFreqDaily;
    private android.widget.RadioButton rbFreqWeekdays;
    private android.widget.RadioButton rbFreqWeekends;
    private android.widget.RadioButton rbFreqCustom;

    private com.google.android.material.textfield.TextInputLayout tilEndDate;
    private com.google.android.material.textfield.TextInputLayout tilSingleTime;
    private TextInputEditText tietSingleTime;
    private com.google.android.material.card.MaterialCardView cardTimeSlots;
    private CheckBox cbSlot09, cbSlot13, cbSlot16, cbSlot19, cbSlot22;
    private android.widget.GridLayout layoutCustomDays;
    private CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    private TextView tvPreviewTitle, tvPreviewDescription;

    private ShowtimeRepositoryImpl showtimeRepository;
    private CinemaRepositoryImpl cinemaRepository;
    private MovieRepositoryImpl movieRepository;
    private RoomRepositoryImpl roomRepository;
    private SeatRepositoryImpl seatRepository;

    private final List<Movie> moviesList = new ArrayList<>();
    private final List<Cinema> cinemasList = new ArrayList<>();
    private final List<Room> roomsList = new ArrayList<>();

    private String selectedMovieId = null;
    private String selectedCinemaId = null;
    private String selectedRoomId = null;

    private Calendar startCal = null;
    private Calendar endCal = null;
    private int singleHour = -1;
    private int singleMinute = -1;
    private int selectedFrequencyIndex = 0; // 0: Chỉ một lần, 1: Hàng ngày, 2: T2-T6, 3: Cuối tuần, 4: Tùy chỉnh

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_showtime_schedule);

        showtimeRepository = new ShowtimeRepositoryImpl();
        cinemaRepository = new CinemaRepositoryImpl();
        movieRepository = new MovieRepositoryImpl(new com.example.cinemabooking.data.remote.datasource.MovieRemoteDataSource());
        roomRepository = new RoomRepositoryImpl();
        seatRepository = new SeatRepositoryImpl();

        initViews();
        setupDateTimePickers();
        setupStaticDropdowns();
        loadMetaData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> generateSchedule());

        actvMovie = findViewById(R.id.actvMovie);
        actvCinema = findViewById(R.id.actvCinema);
        actvRoom = findViewById(R.id.actvRoom);
        actvFormat = findViewById(R.id.actvFormat);
        actvLanguage = findViewById(R.id.actvLanguage);

        tietStartDate = findViewById(R.id.tietStartDate);
        tietEndDate = findViewById(R.id.tietEndDate);
        tietBasePrice = findViewById(R.id.tietBasePrice);

        layoutFreqOnce = findViewById(R.id.layoutFreqOnce);
        layoutFreqDaily = findViewById(R.id.layoutFreqDaily);
        layoutFreqWeekdays = findViewById(R.id.layoutFreqWeekdays);
        layoutFreqWeekends = findViewById(R.id.layoutFreqWeekends);
        layoutFreqCustom = findViewById(R.id.layoutFreqCustom);

        rbFreqOnce = findViewById(R.id.rbFreqOnce);
        rbFreqDaily = findViewById(R.id.rbFreqDaily);
        rbFreqWeekdays = findViewById(R.id.rbFreqWeekdays);
        rbFreqWeekends = findViewById(R.id.rbFreqWeekends);
        rbFreqCustom = findViewById(R.id.rbFreqCustom);

        tilEndDate = findViewById(R.id.tilEndDate);
        tilSingleTime = findViewById(R.id.tilSingleTime);
        tietSingleTime = findViewById(R.id.tietSingleTime);
        cardTimeSlots = findViewById(R.id.cardTimeSlots);

        cbSlot09 = findViewById(R.id.cbSlot09);
        cbSlot13 = findViewById(R.id.cbSlot13);
        cbSlot16 = findViewById(R.id.cbSlot16);
        cbSlot19 = findViewById(R.id.cbSlot19);
        cbSlot22 = findViewById(R.id.cbSlot22);

        layoutCustomDays = findViewById(R.id.layoutCustomDays);
        cbMon = findViewById(R.id.cbMon);
        cbTue = findViewById(R.id.cbTue);
        cbWed = findViewById(R.id.cbWed);
        cbThu = findViewById(R.id.cbThu);
        cbFri = findViewById(R.id.cbFri);
        cbSat = findViewById(R.id.cbSat);
        cbSun = findViewById(R.id.cbSun);

        tvPreviewTitle = findViewById(R.id.tvPreviewTitle);
        tvPreviewDescription = findViewById(R.id.tvPreviewDescription);

        actvMovie.setOnItemClickListener((parent, view, position, id) -> {
            selectedMovieId = moviesList.get(position).movieId;
            calculatePreview();
        });

        actvCinema.setOnItemClickListener((parent, view, position, id) -> {
            selectedCinemaId = cinemasList.get(position).cinemaId;
            selectedRoomId = null;
            actvRoom.setText("", false);
            loadRoomsForCinema(selectedCinemaId);
            calculatePreview();
        });

        actvRoom.setOnItemClickListener((parent, view, position, id) -> {
            selectedRoomId = roomsList.get(position).roomId;
            calculatePreview();
        });

        actvFormat.setOnItemClickListener((parent, view, position, id) -> calculatePreview());
        actvLanguage.setOnItemClickListener((parent, view, position, id) -> calculatePreview());

        tietSingleTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (singleHour != -1) {
                cal.set(Calendar.HOUR_OF_DAY, singleHour);
                cal.set(Calendar.MINUTE, singleMinute);
            }
            new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
                singleHour = hourOfDay;
                singleMinute = minute;
                tietSingleTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                calculatePreview();
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        setupFrequencySelection();

        View.OnClickListener checkboxListener = v -> calculatePreview();
        cbSlot09.setOnClickListener(checkboxListener);
        cbSlot13.setOnClickListener(checkboxListener);
        cbSlot16.setOnClickListener(checkboxListener);
        cbSlot19.setOnClickListener(checkboxListener);
        cbSlot22.setOnClickListener(checkboxListener);

        cbMon.setOnClickListener(checkboxListener);
        cbTue.setOnClickListener(checkboxListener);
        cbWed.setOnClickListener(checkboxListener);
        cbThu.setOnClickListener(checkboxListener);
        cbFri.setOnClickListener(checkboxListener);
        cbSat.setOnClickListener(checkboxListener);
        cbSun.setOnClickListener(checkboxListener);

        tietBasePrice.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                calculatePreview();
            }
        });
    }

    private void setupDateTimePickers() {
        tietStartDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (startCal != null) {
                cal = startCal;
            }
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                startCal = Calendar.getInstance();
                startCal.set(year, month, dayOfMonth, 0, 0, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                tietStartDate.setText(dateFormat.format(startCal.getTime()));
                calculatePreview();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        tietEndDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (endCal != null) {
                cal = endCal;
            }
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                endCal = Calendar.getInstance();
                endCal.set(year, month, dayOfMonth, 23, 59, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                tietEndDate.setText(dateFormat.format(endCal.getTime()));
                calculatePreview();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupStaticDropdowns() {
        String[] formats = {"2D", "3D", "IMAX", "4DX"};
        actvFormat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, formats));

        String[] languages = {"Phụ đề tiếng Việt", "Thuyết minh tiếng Việt", "Lồng tiếng tiếng Việt", "Original English"};
        actvLanguage.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, languages));
    }

    private void loadMetaData() {
        movieRepository.getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                moviesList.clear();
                moviesList.addAll(movies);
                List<String> movieTitles = new ArrayList<>();
                for (Movie m : movies) {
                    movieTitles.add(m.title);
                }
                actvMovie.setAdapter(new ArrayAdapter<>(AdminShowtimeScheduleActivity.this,
                        android.R.layout.simple_list_item_1, movieTitles));

                loadCinemas();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách phim: " + message);
            }
        });
    }

    private void loadCinemas() {
        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> cinemas) {
                cinemasList.clear();
                cinemasList.addAll(cinemas);
                List<String> cinemaNames = new ArrayList<>();
                for (Cinema c : cinemas) {
                    cinemaNames.add(c.name);
                }
                actvCinema.setAdapter(new ArrayAdapter<>(AdminShowtimeScheduleActivity.this,
                        android.R.layout.simple_list_item_1, cinemaNames));
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách rạp: " + message);
            }
        });
    }

    private void loadRoomsForCinema(String cinemaId) {
        roomRepository.getRoomsByCinemaId(cinemaId, new ResultCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                roomsList.clear();
                roomsList.addAll(rooms);
                List<String> roomNames = new ArrayList<>();
                for (Room r : rooms) {
                    roomNames.add(r.name);
                }
                actvRoom.setAdapter(new ArrayAdapter<>(AdminShowtimeScheduleActivity.this,
                        android.R.layout.simple_list_item_1, roomNames));
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách phòng: " + message);
            }
        });
    }

    private void calculatePreview() {
        if (selectedMovieId == null || selectedCinemaId == null || selectedRoomId == null) {
            tvPreviewTitle.setText("Dự kiến tạo: 0 suất chiếu");
            tvPreviewDescription.setText("Vui lòng chọn phim, rạp, phòng chiếu.");
            return;
        }

        if (selectedFrequencyIndex == 0) { // Chỉ một lần
            if (startCal == null || singleHour == -1) {
                tvPreviewTitle.setText("Dự kiến tạo: 0 suất chiếu");
                tvPreviewDescription.setText("Vui lòng chọn ngày chiếu và giờ chiếu.");
                return;
            }
            tvPreviewTitle.setText("Dự kiến tạo: 1 suất chiếu");
            displayPreviewDescription();
            return;
        }

        if (startCal == null || endCal == null) {
            tvPreviewTitle.setText("Dự kiến tạo: 0 suất chiếu");
            tvPreviewDescription.setText("Vui lòng chọn khoảng thời gian.");
            return;
        }

        List<Integer> slots = getSelectedTimeSlots();
        if (slots.isEmpty()) {
            tvPreviewTitle.setText("Dự kiến tạo: 0 suất chiếu");
            tvPreviewDescription.setText("Vui lòng tích chọn ít nhất một khung giờ phát sóng.");
            return;
        }

        int showtimesCount = 0;
        Calendar current = (Calendar) startCal.clone();

        while (current.before(endCal) || isSameDay(current, endCal)) {
            int dayOfWeek = current.get(Calendar.DAY_OF_WEEK);
            boolean matchesFreq = false;

            if (selectedFrequencyIndex == 1) { // Hàng ngày
                matchesFreq = true;
            } else if (selectedFrequencyIndex == 2) { // Trong tuần (Mon - Fri)
                matchesFreq = (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY);
            } else if (selectedFrequencyIndex == 3) { // Cuối tuần (Sat - Sun)
                matchesFreq = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
            } else if (selectedFrequencyIndex == 4) { // Tùy chỉnh
                if (dayOfWeek == Calendar.MONDAY && cbMon.isChecked()) matchesFreq = true;
                else if (dayOfWeek == Calendar.TUESDAY && cbTue.isChecked()) matchesFreq = true;
                else if (dayOfWeek == Calendar.WEDNESDAY && cbWed.isChecked()) matchesFreq = true;
                else if (dayOfWeek == Calendar.THURSDAY && cbThu.isChecked()) matchesFreq = true;
                else if (dayOfWeek == Calendar.FRIDAY && cbFri.isChecked()) matchesFreq = true;
                else if (dayOfWeek == Calendar.SATURDAY && cbSat.isChecked()) matchesFreq = true;
                else if (dayOfWeek == Calendar.SUNDAY && cbSun.isChecked()) matchesFreq = true;
            }

            if (matchesFreq) {
                showtimesCount += slots.size();
            }

            current.add(Calendar.DATE, 1);
        }

        tvPreviewTitle.setText("Dự kiến tạo: " + showtimesCount + " suất chiếu");
        displayPreviewDescription();
    }

    private void displayPreviewDescription() {
        String movieTitle = actvMovie.getText().toString();
        String cinemaRoom = actvCinema.getText().toString() + " - " + actvRoom.getText().toString();
        
        String dates = "";
        if (selectedFrequencyIndex == 0) {
            dates = startCal != null ? dateFormat.format(startCal.getTime()) + " lúc " + String.format(Locale.getDefault(), "%02d:%02d", singleHour, singleMinute) : "";
        } else {
            dates = dateFormat.format(startCal.getTime()) + " đến " + dateFormat.format(endCal.getTime());
        }
        
        String format = actvFormat.getText().toString();
        String lang = actvLanguage.getText().toString();
        String price = tietBasePrice.getText().toString();

        String desc = String.format("• Phim: %s\n• Cơ sở: %s\n• Ngày áp dụng: %s\n• Định dạng: %s | %s\n• Giá vé gốc: %s đ",
                movieTitle, cinemaRoom, dates,
                format.isEmpty() ? "2D" : format,
                lang.isEmpty() ? "Phụ đề" : lang,
                price.isEmpty() ? "0" : price);
        tvPreviewDescription.setText(desc);
    }

    private List<Integer> getSelectedTimeSlots() {
        List<Integer> list = new ArrayList<>();
        if (cbSlot09.isChecked()) list.add(9);
        if (cbSlot13.isChecked()) list.add(13);
        if (cbSlot16.isChecked()) list.add(16);
        if (cbSlot19.isChecked()) list.add(19);
        if (cbSlot22.isChecked()) list.add(22);
        return list;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void generateSchedule() {
        if (selectedMovieId == null || selectedCinemaId == null || selectedRoomId == null) {
            showToast("Vui lòng chọn Phim, Rạp và Phòng chiếu!");
            return;
        }

        if (selectedFrequencyIndex == 0) {
            if (startCal == null) {
                showToast("Vui lòng chọn ngày chiếu!");
                return;
            }
            if (singleHour == -1) {
                showToast("Vui lòng chọn giờ chiếu!");
                return;
            }
        } else {
            if (startCal == null || endCal == null) {
                showToast("Vui lòng chọn khoảng thời gian hợp lệ!");
                return;
            }
            if (endCal.before(startCal)) {
                showToast("Ngày kết thúc phải sau hoặc trùng ngày bắt đầu!");
                return;
            }
            List<Integer> slots = getSelectedTimeSlots();
            if (slots.isEmpty()) {
                showToast("Vui lòng chọn ít nhất một khung giờ chiếu!");
                return;
            }
        }

        String format = actvFormat.getText().toString();
        String language = actvLanguage.getText().toString();
        String priceStr = tietBasePrice.getText().toString();

        if (TextUtils.isEmpty(format) || TextUtils.isEmpty(language) || TextUtils.isEmpty(priceStr)) {
            showToast("Vui lòng chọn định dạng, ngôn ngữ và nhập giá vé!");
            return;
        }

        double basePrice;
        try {
            basePrice = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showToast("Giá vé gốc không hợp lệ!");
            return;
        }

        Movie movie = null;
        for (Movie m : moviesList) {
            if (m.movieId.equals(selectedMovieId)) {
                movie = m;
                break;
            }
        }
        if (movie == null || movie.durationMinutes <= 0) {
            showToast("Thời lượng phim không hợp lệ hoặc chưa được thiết lập.");
            return;
        }

        final int duration = movie.durationMinutes;

        // Fetch all showtimes once to check for overlaps
        showtimeRepository.getAllShowtimes(new ResultCallback<List<Showtime>>() {
            @Override
            public void onSuccess(List<Showtime> existingShowtimes) {
                showtimeRepository.getAllShowtimeSchedules(new ResultCallback<List<Showtime>>() {
                    @Override
                    public void onSuccess(List<Showtime> existingSchedules) {
                        List<Showtime> combinedList = new ArrayList<>(existingShowtimes);
                        if (existingSchedules != null) {
                            for (Showtime s : existingSchedules) {
                                if (!s.executed) {
                                    combinedList.add(s);
                                }
                            }
                        }

                        List<Showtime> toGenerate = new ArrayList<>();

                        if (selectedFrequencyIndex == 0) { // Chỉ một lần
                            Calendar start = Calendar.getInstance();
                            start.set(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DATE), singleHour, singleMinute, 0);
                            start.set(Calendar.MILLISECOND, 0);
                            long startAt = start.getTimeInMillis();
                            long endAt = startAt + (duration * 60L * 1000L);

                            // Check overlaps
                            boolean overlaps = false;
                            for (Showtime s : combinedList) {
                                if (s.roomId.equals(selectedRoomId) && !s.deleted) {
                                    if (startAt < s.endAt && endAt > s.startAt) {
                                        overlaps = true;
                                        break;
                                    }
                                }
                            }

                            if (!overlaps) {
                                Showtime showtime = new Showtime();
                                showtime.movieId = selectedMovieId;
                                showtime.cinemaId = selectedCinemaId;
                                showtime.roomId = selectedRoomId;
                                showtime.startAt = startAt;
                                showtime.endAt = endAt;
                                showtime.format = format;
                                showtime.language = language;
                                showtime.basePrice = basePrice;
                                showtime.status = "active";
                                showtime.bookedSeatsCount = 0;
                                showtime.isScheduled = true;
                                toGenerate.add(showtime);
                            }
                        } else {
                            List<Integer> slots = getSelectedTimeSlots();
                            Calendar current = (Calendar) startCal.clone();

                            while (current.before(endCal) || isSameDay(current, endCal)) {
                                int dayOfWeek = current.get(Calendar.DAY_OF_WEEK);
                                boolean matchesFreq = false;

                                if (selectedFrequencyIndex == 1) { // Hàng ngày
                                    matchesFreq = true;
                                } else if (selectedFrequencyIndex == 2) { // Trong tuần (Mon - Fri)
                                    matchesFreq = (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY);
                                } else if (selectedFrequencyIndex == 3) { // Cuối tuần (Sat - Sun)
                                    matchesFreq = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
                                } else if (selectedFrequencyIndex == 4) { // Tùy chỉnh
                                    if (dayOfWeek == Calendar.MONDAY && cbMon.isChecked()) matchesFreq = true;
                                    else if (dayOfWeek == Calendar.TUESDAY && cbTue.isChecked()) matchesFreq = true;
                                    else if (dayOfWeek == Calendar.WEDNESDAY && cbWed.isChecked()) matchesFreq = true;
                                    else if (dayOfWeek == Calendar.THURSDAY && cbThu.isChecked()) matchesFreq = true;
                                    else if (dayOfWeek == Calendar.FRIDAY && cbFri.isChecked()) matchesFreq = true;
                                    else if (dayOfWeek == Calendar.SATURDAY && cbSat.isChecked()) matchesFreq = true;
                                    else if (dayOfWeek == Calendar.SUNDAY && cbSun.isChecked()) matchesFreq = true;
                                }

                                if (matchesFreq) {
                                    for (int hour : slots) {
                                        Calendar start = Calendar.getInstance();
                                        start.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DATE), hour, 0, 0);
                                        start.set(Calendar.MILLISECOND, 0);
                                        long startAt = start.getTimeInMillis();
                                        long endAt = startAt + (duration * 60L * 1000L);

                                        // Check overlaps
                                        boolean overlaps = false;
                                        for (Showtime s : combinedList) {
                                            if (s.roomId.equals(selectedRoomId) && !s.deleted) {
                                                // Overlap condition: startAt < s.endAt && endAt > s.startAt
                                                if (startAt < s.endAt && endAt > s.startAt) {
                                                    overlaps = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!overlaps) {
                                            Showtime showtime = new Showtime();
                                            showtime.movieId = selectedMovieId;
                                            showtime.cinemaId = selectedCinemaId;
                                            showtime.roomId = selectedRoomId;
                                            showtime.startAt = startAt;
                                            showtime.endAt = endAt;
                                            showtime.format = format;
                                            showtime.language = language;
                                            showtime.basePrice = basePrice;
                                            showtime.status = "active";
                                            showtime.bookedSeatsCount = 0;
                                            showtime.isScheduled = true;
                                            toGenerate.add(showtime);
                                        }
                                    }
                                }

                                current.add(Calendar.DATE, 1);
                            }
                        }

                        if (toGenerate.isEmpty()) {
                            new AlertDialog.Builder(AdminShowtimeScheduleActivity.this)
                                    .setTitle("Không thể lên lịch")
                                    .setMessage("Tất cả các suất chiếu dự kiến đều bị trùng lịch với các suất chiếu hiện có hoặc lịch đã lên của phòng này!")
                                    .setPositiveButton("Đồng ý", null)
                                    .show();
                            return;
                        }

                        new AlertDialog.Builder(AdminShowtimeScheduleActivity.this)
                                .setTitle("Xác nhận lên lịch")
                                .setMessage("Hệ thống sẽ tạo tự động " + toGenerate.size() + " suất chiếu (đã tự lọc trùng lịch).\nBạn có đồng ý không?")
                                .setPositiveButton("Đồng ý", (dialog, which) -> {
                                    saveShowtimesSequentially(toGenerate, 0, 0);
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }

                    @Override
                    public void onError(String message) {
                        showToast("Lỗi tải lịch trình suất chiếu hiện có: " + message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải suất chiếu hiện có: " + message);
            }
        });
    }

    private void saveShowtimesSequentially(List<Showtime> list, int index, int successCount) {
        if (index >= list.size()) {
            showToast("Lên lịch thành công! Đã tạo " + successCount + " suất chiếu.");
            setResult(RESULT_OK);
            finish();
            return;
        }

        Showtime showtime = list.get(index);
        // Get total seats from seat template count or default 64
        seatRepository.getSeatTemplatesByRoomId(selectedRoomId, new ResultCallback<List<SeatTemplate>>() {
            @Override
            public void onSuccess(List<SeatTemplate> templates) {
                int totalSeats = templates.size();
                showtime.totalSeats = totalSeats > 0 ? totalSeats : 64;
                createShowtimeDocument(list, index, successCount, showtime);
            }

            @Override
            public void onError(String message) {
                showtime.totalSeats = 64;
                createShowtimeDocument(list, index, successCount, showtime);
            }
        });
    }

    private void createShowtimeDocument(List<Showtime> list, int index, int successCount, Showtime showtime) {
        showtimeRepository.createShowtimeSchedule(showtime, new ResultCallback<Showtime>() {
            @Override
            public void onSuccess(Showtime created) {
                saveShowtimesSequentially(list, index + 1, successCount + 1);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Failed to create showtime schedule " + index + ": " + message);
                saveShowtimesSequentially(list, index + 1, successCount);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupFrequencySelection() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.layoutFreqOnce) {
                selectFrequency(0);
            } else if (id == R.id.layoutFreqDaily) {
                selectFrequency(1);
            } else if (id == R.id.layoutFreqWeekdays) {
                selectFrequency(2);
            } else if (id == R.id.layoutFreqWeekends) {
                selectFrequency(3);
            } else if (id == R.id.layoutFreqCustom) {
                selectFrequency(4);
            }
        };

        layoutFreqOnce.setOnClickListener(listener);
        layoutFreqDaily.setOnClickListener(listener);
        layoutFreqWeekdays.setOnClickListener(listener);
        layoutFreqWeekends.setOnClickListener(listener);
        layoutFreqCustom.setOnClickListener(listener);

        selectFrequency(0); // Default selection
    }

    private void selectFrequency(int index) {
        selectedFrequencyIndex = index;

        rbFreqOnce.setChecked(index == 0);
        rbFreqDaily.setChecked(index == 1);
        rbFreqWeekdays.setChecked(index == 2);
        rbFreqWeekends.setChecked(index == 3);
        rbFreqCustom.setChecked(index == 4);

        updateFrequencyLayouts();
        calculatePreview();
    }

    private void updateFrequencyLayouts() {
        if (selectedFrequencyIndex == 0) { // Chỉ một lần
            tilEndDate.setVisibility(View.GONE);
            tietStartDate.setHint("Chọn ngày chiếu...");
            tilSingleTime.setVisibility(View.VISIBLE);
            cardTimeSlots.setVisibility(View.GONE);
            layoutCustomDays.setVisibility(View.GONE);
        } else {
            tilEndDate.setVisibility(View.VISIBLE);
            tietStartDate.setHint("Từ ngày (Ngày bắt đầu)...");
            tilSingleTime.setVisibility(View.GONE);
            cardTimeSlots.setVisibility(View.VISIBLE);
            if (selectedFrequencyIndex == 4) { // Tùy chỉnh
                layoutCustomDays.setVisibility(View.VISIBLE);
            } else {
                layoutCustomDays.setVisibility(View.GONE);
            }
        }
    }
}
