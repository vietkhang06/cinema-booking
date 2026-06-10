package com.example.cinemabooking.ui.admin.report;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.BookingRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.domain.model.SnackOrder;
import com.example.cinemabooking.ui.admin.widget.AdminHorizontalBarChartView;
import com.example.cinemabooking.ui.admin.widget.AdminLineChartView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class AdminReportActivity extends AppCompatActivity {

    private static final String TAG = "AdminReportActivity";

    private TextView tvSelectedDateRange;
    private TextView tvTicketRevenue, tvSnackRevenue, tvTotalRevenue, tvTicketsSold;
    private AdminLineChartView lineChartRevenue;
    private AdminHorizontalBarChartView barChartMovies;
    private View btnSelectDateRange;

    private BookingRepositoryImpl bookingRepository;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private long startDateMillis;
    private long endDateMillis;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_report);

        bookingRepository = new BookingRepositoryImpl();

        initViews();
        setupDefaultDates();
        setupListeners();
        loadReportData();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvSelectedDateRange = findViewById(R.id.tvSelectedDateRange);
        tvTicketRevenue = findViewById(R.id.tvTicketRevenue);
        tvSnackRevenue = findViewById(R.id.tvSnackRevenue);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTicketsSold = findViewById(R.id.tvTicketsSold);
        lineChartRevenue = findViewById(R.id.lineChartRevenue);
        barChartMovies = findViewById(R.id.barChartMovies);
        btnSelectDateRange = findViewById(R.id.btnSelectDateRange);
    }

    private void setupDefaultDates() {
        Calendar cal = Calendar.getInstance();
        // End of today
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        endDateMillis = cal.getTimeInMillis();

        // 30 days ago at start of day
        cal.add(Calendar.DAY_OF_YEAR, -30);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startDateMillis = cal.getTimeInMillis();

        updateDateRangeLabel();
    }

    private void updateDateRangeLabel() {
        String startStr = dateFormat.format(new Date(startDateMillis));
        String endStr = dateFormat.format(new Date(endDateMillis));
        tvSelectedDateRange.setText(startStr + " - " + endStr);
    }

    private void setupListeners() {
        btnSelectDateRange.setOnClickListener(v -> {
            MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Chọn khoảng thời gian báo cáo")
                    .setSelection(new Pair<>(startDateMillis, endDateMillis))
                    .build();

            dateRangePicker.addOnPositiveButtonClickListener(selection -> {
                if (selection.first != null && selection.second != null) {
                    Calendar cal = Calendar.getInstance();
                    
                    cal.setTimeInMillis(selection.first);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    startDateMillis = cal.getTimeInMillis();

                    cal.setTimeInMillis(selection.second);
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    endDateMillis = cal.getTimeInMillis();

                    updateDateRangeLabel();
                    loadReportData();
                }
            });

            dateRangePicker.show(getSupportFragmentManager(), "REPORT_DATE_RANGE");
        });
    }

    private void loadReportData() {
        // Step 1: Load all bookings
        bookingRepository.getAllBookings(new ResultCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                // Step 2: Load all snack orders
                firestore.collection("snack_orders")
                        .whereEqualTo("deleted", false)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<SnackOrder> snackOrders = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                try {
                                    SnackOrder order = doc.toObject(SnackOrder.class);
                                    if (order != null) {
                                        snackOrders.add(order);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Lỗi phân tích đơn bắp nước: " + doc.getId(), e);
                                }
                            }
                            calculateAndRenderReport(bookings, snackOrders);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Lỗi tải đơn bắp nước: " + e.getMessage());
                            Toast.makeText(AdminReportActivity.this, "Lỗi tải báo cáo bắp nước", Toast.LENGTH_SHORT).show();
                            // Fallback: render with bookings only
                            calculateAndRenderReport(bookings, new ArrayList<>());
                        });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Lỗi tải vé đặt: " + message);
                Toast.makeText(AdminReportActivity.this, "Lỗi tải báo cáo đặt vé", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAndRenderReport(List<Booking> bookings, List<SnackOrder> snackOrders) {
        double totalTicketRevenueVal = 0;
        double totalSnackRevenueVal = 0;
        int totalTicketsSoldVal = 0;

        // Maps for Charts
        // For Daily Revenue: TreeMap automatically sorts keys (dates) chronologically
        Map<String, Double> dailyTicketRevenue = new TreeMap<>();
        Map<String, Integer> movieSales = new HashMap<>();

        // Helper Map to find snack order price by bookingId/snackOrderId
        Map<String, Double> snackPricesBySnackOrderId = new HashMap<>();
        for (SnackOrder so : snackOrders) {
            boolean isPaid = "paid".equalsIgnoreCase(so.status) || "completed".equalsIgnoreCase(so.status) || "success".equalsIgnoreCase(so.status);
            if (isPaid && so.createdAt >= startDateMillis && so.createdAt <= endDateMillis) {
                totalSnackRevenueVal += so.total;
            }
            if (so.snackOrderId != null) {
                snackPricesBySnackOrderId.put(so.snackOrderId, so.total);
            }
        }

        SimpleDateFormat dayKeyFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        // Process Bookings
        if (bookings != null) {
            for (Booking b : bookings) {
                if (b.deleted) continue;

                // Check dates
                if (b.createdAt < startDateMillis || b.createdAt > endDateMillis) continue;

                // Check payment status or booking status
                boolean isPaid = "confirmed".equalsIgnoreCase(b.bookingStatus) || "paid".equalsIgnoreCase(b.paymentStatus) || "completed".equalsIgnoreCase(b.paymentStatus);
                if (!isPaid) continue;

                // Tickets sold count
                int tickets = (b.seatCodes != null) ? b.seatCodes.size() : 0;
                totalTicketsSoldVal += tickets;

                // Calculate Ticket Revenue
                double bookingTotal = b.total;
                double snackCostInBooking = 0;
                if (b.snackOrderId != null && snackPricesBySnackOrderId.containsKey(b.snackOrderId)) {
                    snackCostInBooking = snackPricesBySnackOrderId.get(b.snackOrderId);
                }
                
                double ticketOnlyRevenue = bookingTotal - snackCostInBooking;
                if (ticketOnlyRevenue < 0) ticketOnlyRevenue = 0;

                totalTicketRevenueVal += ticketOnlyRevenue;

                // Group by day for line chart
                String dayKey = dayKeyFormat.format(new Date(b.createdAt));
                dailyTicketRevenue.put(dayKey, dailyTicketRevenue.getOrDefault(dayKey, 0.0) + ticketOnlyRevenue);

                // Group by movie for horizontal bar chart
                String movieTitle = (b.movieTitleSnapshot != null && !b.movieTitleSnapshot.isEmpty()) ? b.movieTitleSnapshot : "Khác";
                movieSales.put(movieTitle, movieSales.getOrDefault(movieTitle, 0) + tickets);
            }
        }

        // Render Metric Cards
        tvTicketRevenue.setText(currencyFormat.format(totalTicketRevenueVal) + " đ");
        tvSnackRevenue.setText(currencyFormat.format(totalSnackRevenueVal) + " đ");
        double totalRevenueVal = totalTicketRevenueVal + totalSnackRevenueVal;
        tvTotalRevenue.setText(currencyFormat.format(totalRevenueVal) + " đ");
        tvTicketsSold.setText(totalTicketsSoldVal + " vé");

        // 1. Render Line Chart (Revenue trend)
        List<Float> lineValues = new ArrayList<>();
        List<String> lineLabels = new ArrayList<>();

        // Generate all dates in range to show 0 for days without sales, keeping it clean
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTimeInMillis(startDateMillis);
        while (tempCal.getTimeInMillis() <= endDateMillis) {
            String dayKey = dayKeyFormat.format(tempCal.getTime());
            lineLabels.add(dayKey);
            double revenue = dailyTicketRevenue.getOrDefault(dayKey, 0.0);
            lineValues.add((float) revenue);
            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // If range is long, only display a subset of labels or limit data points to prevent crowding
        if (lineLabels.size() > 10) {
            // Keep actual data but sparse the labels in chart? Let's pass full list, the chart's custom implementation handles it.
        }

        if (lineChartRevenue != null && !lineValues.isEmpty()) {
            lineChartRevenue.setData(lineValues, lineLabels);
        }

        // 2. Render Bar Chart (Top movies)
        List<Map.Entry<String, Integer>> movieSalesList = new ArrayList<>(movieSales.entrySet());
        movieSalesList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // Descending

        List<String> barLabels = new ArrayList<>();
        List<Float> barValues = new ArrayList<>();

        for (int i = 0; i < Math.min(5, movieSalesList.size()); i++) {
            barLabels.add(movieSalesList.get(i).getKey());
            barValues.add((float) movieSalesList.get(i).getValue());
        }

        if (barChartMovies != null) {
            // If empty, set empty placeholder data
            if (barLabels.isEmpty()) {
                barLabels.add("Chưa có dữ liệu");
                barValues.add(0f);
            }
            barChartMovies.setData(barLabels, barValues);
        }
    }
}