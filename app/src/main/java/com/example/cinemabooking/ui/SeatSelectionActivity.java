package com.example.cinemabookingapp.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.dto.SeatDTO;
import com.example.cinemabookingapp.ui.customer.adapter.SeatAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.LinearLayout;

public class SeatSelectionActivity extends AppCompatActivity {



    public static final String EXTRA_SHOWTIME_ID  = "showtimeId";
    public static final String EXTRA_MOVIE_TITLE  = "movieTitle";
    public static final String EXTRA_MOVIE_ID     = "movieId";
    public static final String EXTRA_POSTER_URL   = "posterUrl";
    public static final String EXTRA_BASE_PRICE   = "basePrice";
    public static final String EXTRA_SHOWTIME_START = "showtimeStart";
    public static final String EXTRA_CINEMA_NAME  = "cinemaName";

    private SeatAdapter adapter;
    private android.widget.LinearLayout llSelectedSeatChips;
    private android.widget.HorizontalScrollView scrollSelectedSeats;
    private android.view.View dividerBottom;
    private final List<SeatDTO> seatList = new ArrayList<>();

    private TextView tvMovieTitle, tvTotalPrice, tvSeatCount, tvShowtimeDate;
    private MaterialButton btnContinue;

    private String showtimeId, movieTitle, movieId, posterUrl, cinemaName;
    private double basePrice = 85000;
    private long showtimeStart;

    private com.google.firebase.firestore.ListenerRegistration seatListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        showtimeId   = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);
        movieTitle   = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        movieId      = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        posterUrl    = getIntent().getStringExtra(EXTRA_POSTER_URL);
        cinemaName   = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        basePrice    = getIntent().getDoubleExtra(EXTRA_BASE_PRICE, 85000);
        showtimeStart = getIntent().getLongExtra(EXTRA_SHOWTIME_START, 0);

        initViews();
        loadSeats();
    }

    private void initViews() {
        tvMovieTitle   = findViewById(R.id.tvMovieTitle);
        tvTotalPrice   = findViewById(R.id.tvTotalPrice);
        tvSeatCount    = findViewById(R.id.tvSeatCount);
        tvShowtimeDate = findViewById(R.id.tvShowtimeDate);
        btnContinue    = findViewById(R.id.btnContinue);

        llSelectedSeatChips = findViewById(R.id.llSelectedSeatChips);
        scrollSelectedSeats = findViewById(R.id.scrollSelectedSeats);
        dividerBottom = findViewById(R.id.dividerBottom);

        ImageButton btnBack = findViewById(R.id.btnBack);

        if (movieTitle != null) tvMovieTitle.setText(movieTitle);

        // Hiển thị ngày + giờ chiếu
        if (showtimeStart > 0) {
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd 'Th'M", new Locale("vi"));
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvShowtimeDate.setText(dateFmt.format(new Date(showtimeStart))
                    + " • " + timeFmt.format(new Date(showtimeStart)));
        }

        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvSeatMap = findViewById(R.id.rvSeatMap);
        rvSeatMap.setLayoutManager(new GridLayoutManager(this, 9));

        adapter = new SeatAdapter(seatList, (seat, position) -> {
            long now = System.currentTimeMillis();
            String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                    ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : "";
            
            boolean isBooked = "booked".equalsIgnoreCase(seat.status);
            boolean isHeldByOther = "held".equalsIgnoreCase(seat.status) 
                    && (seat.heldUntil > now) 
                    && !currentUserId.equals(seat.heldBy);
            
            if (isBooked) {
                Toast.makeText(this, "Ghế đã được đặt trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isHeldByOther) {
                Toast.makeText(this, "Ghế đang được người khác giữ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enforce maximum 5 seats selection
            if (!seat.isSelected) {
                if (getSelectedSeats().size() >= 5) {
                    Toast.makeText(this, "Bạn chỉ có thể chọn tối đa 5 ghế", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            seat.isSelected = !seat.isSelected;
            adapter.notifyItemChanged(position);
            updateBottomBar();
        });
        rvSeatMap.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> {
            List<SeatDTO> selected = getSelectedSeats();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ghế!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            btnContinue.setEnabled(false);
            Toast.makeText(this, "Đang kiểm tra trạng thái ghế...", Toast.LENGTH_SHORT).show();

            List<String> selectedSeatIds = new ArrayList<>();
            for (SeatDTO s : selected) {
                if (s.seatId != null) selectedSeatIds.add(s.seatId);
            }

            com.example.cinemabookingapp.data.dto.SeatLockRequestDTO lockRequest = 
                    new com.example.cinemabookingapp.data.dto.SeatLockRequestDTO(showtimeId, selectedSeatIds);

            com.example.cinemabookingapp.data.remote.api.SeatApiService seatApi = 
                    com.example.cinemabookingapp.data.remote.api.RetrofitClient.getInstance()
                    .create(com.example.cinemabookingapp.data.remote.api.SeatApiService.class);

            seatApi.lockSeats(lockRequest).enqueue(new retrofit2.Callback<com.example.cinemabookingapp.data.dto.ApiResponse<Void>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<Void>> call, retrofit2.Response<com.example.cinemabookingapp.data.dto.ApiResponse<Void>> response) {
                    btnContinue.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        goToBookingConfirm(selected);
                    } else {
                        String errMsg = "Ghế đã có người khác chọn hoặc hết hạn khóa ghế. Vui lòng chọn ghế khác!";
                        if (response.body() != null && response.body().getMessage() != null) {
                            errMsg = response.body().getMessage();
                        } else if (response.code() == 409) {
                            errMsg = "Xung đột: Ghế đã có người giữ hoặc đã được đặt!";
                        } else if (response.code() == 401 || response.code() == 403) {
                            errMsg = "Lỗi xác thực: Vui lòng đăng nhập lại!";
                        }
                        Toast.makeText(SeatSelectionActivity.this, errMsg, Toast.LENGTH_LONG).show();
                        loadSeats(); // Refresh seat map
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<Void>> call, Throwable t) {
                    btnContinue.setEnabled(true);
                    Toast.makeText(SeatSelectionActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });


    }

    private void loadSeats() {
        if (showtimeId == null) { loadDummySeats(); return; }

        com.example.cinemabookingapp.data.remote.api.SeatApiService seatApi = 
                com.example.cinemabookingapp.data.remote.api.RetrofitClient.getInstance()
                .create(com.example.cinemabookingapp.data.remote.api.SeatApiService.class);

        seatApi.getSeatsByShowtimeId(showtimeId).enqueue(new retrofit2.Callback<com.example.cinemabookingapp.data.dto.ApiResponse<List<SeatDTO>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<List<SeatDTO>>> call, retrofit2.Response<com.example.cinemabookingapp.data.dto.ApiResponse<List<SeatDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Initial load successful. Now start realtime Firestore sync!
                    startRealtimeSeatSync();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi server (" + response.code() + ")";
                    Toast.makeText(SeatSelectionActivity.this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.cinemabookingapp.data.dto.ApiResponse<List<SeatDTO>>> call, Throwable t) {
                Toast.makeText(SeatSelectionActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void startRealtimeSeatSync() {
        if (showtimeId == null) return;
        
        if (seatListenerRegistration != null) {
            seatListenerRegistration.remove();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        seatListenerRegistration = db.collection("seats")
                .whereEqualTo("showtimeId", showtimeId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        android.util.Log.e("SEAT_SYNC", "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        // 1. Keep track of currently selected seat IDs
                        java.util.Set<String> currentlySelectedIds = new java.util.HashSet<>();
                        for (SeatDTO s : seatList) {
                            if (s.isSelected) {
                                currentlySelectedIds.add(s.seatId);
                            }
                        }

                        // 2. Parse new seats from Firestore snapshot
                        List<SeatDTO> newSeats = new ArrayList<>();
                        int maxCol = 1;
                        boolean seatStolen = false;
                        String stolenSeatCode = "";
                        
                        long now = System.currentTimeMillis();
                        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                                : "";

                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            SeatDTO seat = doc.toObject(SeatDTO.class);
                            if (seat != null) {
                                seat.seatId = doc.getId();
                                
                                boolean isAvailable = "available".equalsIgnoreCase(seat.status) 
                                        || ("held".equalsIgnoreCase(seat.status) && seat.heldUntil < now);
                                
                                boolean isHeldByMe = "held".equalsIgnoreCase(seat.status) 
                                        && (seat.heldUntil >= now) 
                                        && currentUserId.equals(seat.heldBy);

                                // Check if this seat was selected by me previously
                                if (currentlySelectedIds.contains(seat.seatId)) {
                                    if (isAvailable || isHeldByMe) {
                                        seat.isSelected = true;
                                    } else {
                                        // Stolen! The status has changed to booked or held by someone else!
                                        seat.isSelected = false;
                                        seatStolen = true;
                                        stolenSeatCode = seat.seatCode;
                                    }
                                } else {
                                    seat.isSelected = false;
                                }
                                newSeats.add(seat);
                                if (seat.columnNo > maxCol) {
                                    maxCol = seat.columnNo;
                                }
                            }
                        }

                        if (seatStolen) {
                            Toast.makeText(SeatSelectionActivity.this, 
                                    "Ghế " + stolenSeatCode + " đã được người khác giữ hoặc đặt trước!", 
                                    Toast.LENGTH_LONG).show();
                        }

                        // 3. Sort A1 -> F8
                        newSeats.sort((a, b) -> {
                            if (a.rowName == null || b.rowName == null) return 0;
                            int r = a.rowName.compareTo(b.rowName);
                            return r != 0 ? r : Integer.compare(a.columnNo, b.columnNo);
                        });

                        // 4. Update the seatList and UI
                        seatList.clear();
                        seatList.addAll(newSeats);

                        RecyclerView rvSeatMap = findViewById(R.id.rvSeatMap);
                        if (rvSeatMap != null && rvSeatMap.getLayoutManager() instanceof GridLayoutManager) {
                            ((GridLayoutManager) rvSeatMap.getLayoutManager()).setSpanCount(maxCol + 1);
                        }

                        adapter.setSeats(seatList);
                        updateBottomBar();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (seatListenerRegistration != null) {
            seatListenerRegistration.remove();
            seatListenerRegistration = null;
        }
    }

    private void loadDummySeats() {
        String[] rows = {"A","B","C","D","E","F"};
        for (String row : rows) {
            for (int col = 1; col <= 8; col++) {
                SeatDTO s = new SeatDTO();
                s.seatCode = row + String.format(Locale.getDefault(), "%02d", col);
                s.rowName = row;
                s.columnNo = col;
                s.seatType = (row.equals("C") || row.equals("D")) ? "VIP" : "STANDARD";
                s.status = (col == 4) ? "booked" : "available";
                s.isSelected = false;
                seatList.add(s);
            }
        }
        RecyclerView rvSeatMap = findViewById(R.id.rvSeatMap);
        if (rvSeatMap != null && rvSeatMap.getLayoutManager() instanceof GridLayoutManager) {
            ((GridLayoutManager) rvSeatMap.getLayoutManager()).setSpanCount(9);
        }
        adapter.setSeats(seatList);
    }

    private void updateBottomBar() {
        List<SeatDTO> selected = getSelectedSeats();
        double total = 0;

        for (SeatDTO s : selected) {
            total += (s.priceOverride > 0) ? s.priceOverride : basePrice;
        }

        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
        tvSeatCount.setText(selected.size() + " Ghế");

        // Cập nhật chips ghế đã chọn
        llSelectedSeatChips.removeAllViews();

        if (selected.isEmpty()) {
            scrollSelectedSeats.setVisibility(android.view.View.GONE);
            dividerBottom.setVisibility(android.view.View.GONE);
        } else {
            scrollSelectedSeats.setVisibility(android.view.View.VISIBLE);
            dividerBottom.setVisibility(android.view.View.VISIBLE);

            for (SeatDTO s : selected) {
                TextView chip = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMarginEnd(8);
                chip.setLayoutParams(params);
                chip.setText(s.seatCode);
                chip.setTextColor(0xFFFFFFFF);
                chip.setTextSize(12f);
                chip.setPadding(24, 8, 24, 8);

                if ("VIP".equalsIgnoreCase(s.seatType)) {
                    chip.setBackgroundTintList(null);
                    chip.setBackground(getDrawable(R.drawable.bg_chip_vip));
                } else {
                    chip.setBackground(getDrawable(R.drawable.bg_chip_selected));
                }
                llSelectedSeatChips.addView(chip);
            }
        }
    }

    private void goToBookingConfirm(List<SeatDTO> selected) {
        ArrayList<String> seatCodes = new ArrayList<>();
        ArrayList<String> seatIds = new ArrayList<>();
        double total = 0;

        for (SeatDTO s : selected) {
            seatCodes.add(s.seatCode);
            if (s.seatId != null) seatIds.add(s.seatId);
            total += (s.priceOverride > 0) ? s.priceOverride : basePrice;
        }

        Intent intent = new Intent(this, BookingConfirmActivity.class);
        intent.putExtra(BookingConfirmActivity.EXTRA_SHOWTIME_ID, showtimeId);
        intent.putExtra(BookingConfirmActivity.EXTRA_MOVIE_TITLE, movieTitle);
        intent.putExtra(BookingConfirmActivity.EXTRA_MOVIE_ID, movieId);
        intent.putExtra(BookingConfirmActivity.EXTRA_POSTER_URL, posterUrl);
        intent.putExtra(BookingConfirmActivity.EXTRA_CINEMA_NAME, cinemaName);
        intent.putExtra(BookingConfirmActivity.EXTRA_SHOWTIME_START, showtimeStart);
        intent.putExtra(BookingConfirmActivity.EXTRA_TOTAL, total);
        intent.putStringArrayListExtra(BookingConfirmActivity.EXTRA_SEAT_CODES, seatCodes);
        intent.putStringArrayListExtra(BookingConfirmActivity.EXTRA_SEAT_IDS, seatIds);
        startActivity(intent);
    }

    private List<SeatDTO> getSelectedSeats() {
        List<SeatDTO> result = new ArrayList<>();
        for (SeatDTO s : seatList) if (s.isSelected) result.add(s);
        return result;
    }
}