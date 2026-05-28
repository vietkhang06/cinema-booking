package com.example.cinemabooking.ui.customer.transaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.navigation.AppNavigator;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.service.BookingService;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private BookingService bookingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        bookingService = ServiceProvider.getInstance().getBookingService();

        initViews();
        loadData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvTransactions = findViewById(R.id.rv_transactions);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);

        adapter = new TransactionAdapter(booking -> {
            boolean isPaid = "paid".equalsIgnoreCase(booking.paymentStatus)
                    || "confirmed".equalsIgnoreCase(booking.bookingStatus)
                    || "success".equalsIgnoreCase(booking.bookingStatus);
            if (isPaid) {
                AppNavigator.goToTicketDetail(this, booking.bookingId);
            } else {
                Toast.makeText(this, "Chỉ vé đã thanh toán mới xem được chi tiết và mã QR!", Toast.LENGTH_LONG).show();
            }
        });

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void loadData() {
        showLoading(true);
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUid == null) {
            showLoading(false);
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        bookingService.getMyBookings(new ResultCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> movieBookings) {
                // Fetch CineShop orders
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("cine_shop_orders")
                        .whereEqualTo("userId", currentUid)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<Booking> allTransactions = new ArrayList<>();
                            if (movieBookings != null) {
                                allTransactions.addAll(movieBookings);
                            }

                            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                // Map fields programmatically to temporary Booking object
                                Booking orderBooking = new Booking();
                                orderBooking.bookingId = doc.getId();
                                orderBooking.userId = doc.getString("userId");
                                orderBooking.showtimeId = null; // Mark as CineShop order
                                orderBooking.movieTitleSnapshot = doc.getString("itemName");
                                orderBooking.movieImageUrlSnapshot = doc.getString("itemImageUrl");
                                
                                String paymentMethod = doc.getString("paymentMethod");
                                orderBooking.cinemaNameSnapshot = "CineShop - Nhận tại rạp (" + (paymentMethod != null ? paymentMethod : "ZALOPAY") + ")";
                                
                                Long qtyObj = doc.getLong("quantity");
                                int qty = qtyObj != null ? qtyObj.intValue() : 1;
                                orderBooking.roomNameSnapshot = "Số lượng: " + qty;
                                orderBooking.showtimeStartAtSnapshot = 0;
                                
                                Double priceObj = doc.getDouble("totalPrice");
                                double price = priceObj != null ? priceObj : 0.0;
                                orderBooking.total = price;
                                orderBooking.subtotal = price;
                                
                                orderBooking.bookingStatus = doc.getString("status");
                                orderBooking.paymentStatus = doc.getString("status");
                                
                                Long createdAtObj = doc.getLong("createdAt");
                                orderBooking.createdAt = createdAtObj != null ? createdAtObj : 0;

                                allTransactions.add(orderBooking);
                            }

                            // Sort combined list by createdAt descending
                            allTransactions.sort((t1, t2) -> Long.compare(t2.createdAt, t1.createdAt));

                            showLoading(false);
                            if (allTransactions.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rvTransactions.setVisibility(View.GONE);
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                rvTransactions.setVisibility(View.VISIBLE);
                                adapter.setBookings(allTransactions);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // If CineShop fetch fails, fallback to show only movie bookings
                            showLoading(false);
                            List<Booking> fallbackList = movieBookings != null ? movieBookings : new ArrayList<>();
                            if (fallbackList.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rvTransactions.setVisibility(View.GONE);
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                rvTransactions.setVisibility(View.VISIBLE);
                                adapter.setBookings(fallbackList);
                            }
                        });
            }

            @Override
            public void onError(String message) {
                // If movie bookings fetch fails, try loading CineShop orders as fallback
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("cine_shop_orders")
                        .whereEqualTo("userId", currentUid)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<Booking> allTransactions = new ArrayList<>();
                            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Booking orderBooking = new Booking();
                                orderBooking.bookingId = doc.getId();
                                orderBooking.userId = doc.getString("userId");
                                orderBooking.showtimeId = null;
                                orderBooking.movieTitleSnapshot = doc.getString("itemName");
                                orderBooking.movieImageUrlSnapshot = doc.getString("itemImageUrl");
                                
                                String paymentMethod = doc.getString("paymentMethod");
                                orderBooking.cinemaNameSnapshot = "CineShop - Nhận tại rạp (" + (paymentMethod != null ? paymentMethod : "ZALOPAY") + ")";
                                
                                Long qtyObj = doc.getLong("quantity");
                                int qty = qtyObj != null ? qtyObj.intValue() : 1;
                                orderBooking.roomNameSnapshot = "Số lượng: " + qty;
                                orderBooking.showtimeStartAtSnapshot = 0;
                                
                                Double priceObj = doc.getDouble("totalPrice");
                                double price = priceObj != null ? priceObj : 0.0;
                                orderBooking.total = price;
                                orderBooking.subtotal = price;
                                
                                orderBooking.bookingStatus = doc.getString("status");
                                orderBooking.paymentStatus = doc.getString("status");
                                
                                Long createdAtObj = doc.getLong("createdAt");
                                orderBooking.createdAt = createdAtObj != null ? createdAtObj : 0;

                                allTransactions.add(orderBooking);
                            }
                            
                            allTransactions.sort((t1, t2) -> Long.compare(t2.createdAt, t1.createdAt));
                            showLoading(false);
                            if (allTransactions.isEmpty()) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rvTransactions.setVisibility(View.GONE);
                            } else {
                                layoutEmpty.setVisibility(View.GONE);
                                rvTransactions.setVisibility(View.VISIBLE);
                                adapter.setBookings(allTransactions);
                            }
                        })
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            Toast.makeText(TransactionHistoryActivity.this, "Lỗi: " + message, Toast.LENGTH_LONG).show();
                            layoutEmpty.setVisibility(View.VISIBLE);
                        });
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            layoutEmpty.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.GONE);
        }
    }
}
