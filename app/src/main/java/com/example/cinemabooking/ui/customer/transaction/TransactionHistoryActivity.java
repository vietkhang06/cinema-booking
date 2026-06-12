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
import com.google.android.material.tabs.TabLayout;


import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private BookingService bookingService;
    private final List<Booking> allTransactions = new ArrayList<>();
    private TabLayout tabLayout;

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

        // Bind TabLayout and add 2 tabs representing booking states
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Đã thanh toán"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã quét"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterTransactionsByTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

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
                            allTransactions.clear();
                            if (movieBookings != null) {
                                allTransactions.addAll(movieBookings);
                            }

                            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Booking orderBooking = new Booking();
                                orderBooking.bookingId = doc.getId();
                                orderBooking.userId = doc.getString("userId");
                                orderBooking.showtimeId = null; // CineShop indicator
                                orderBooking.movieTitleSnapshot = doc.getString("itemName");
                                orderBooking.movieImageUrlSnapshot = doc.getString("itemImageUrl");
                                
                                String paymentMethod = doc.getString("paymentMethod");
                                orderBooking.cinemaNameSnapshot = "CineShop - Nhận tại rạp (" + (paymentMethod != null ? paymentMethod : "ZALOPAY") + ")";
                                
                                Long qtyObj = doc.getLong("quantity");
                                int qty = qtyObj != null ? qtyObj.intValue() : 1;
                                orderBooking.roomNameSnapshot = "Số lượng: " + qty;
                                orderBooking.showtimeStartAtSnapshot = 0L;
                                
                                Double priceObj = doc.getDouble("totalPrice");
                                double price = priceObj != null ? priceObj : 0.0;
                                orderBooking.total = price;
                                orderBooking.subtotal = price;
                                
                                orderBooking.bookingStatus = doc.getString("status");
                                orderBooking.paymentStatus = doc.getString("status");
                                
                                Long checkInAtObj = doc.getLong("checkInAt");
                                orderBooking.checkInAt = checkInAtObj != null ? checkInAtObj : 0;
                                
                                Long createdAtObj = doc.getLong("createdAt");
                                orderBooking.createdAt = createdAtObj != null ? createdAtObj : 0;

                                allTransactions.add(orderBooking);
                            }

                            allTransactions.sort((t1, t2) -> Long.compare(t2.createdAt, t1.createdAt));

                            showLoading(false);
                            filterTransactionsByTab(tabLayout.getSelectedTabPosition());
                        })
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            allTransactions.clear();
                            if (movieBookings != null) {
                                allTransactions.addAll(movieBookings);
                            }
                            filterTransactionsByTab(tabLayout.getSelectedTabPosition());
                        });
            }

            @Override
            public void onError(String message) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("cine_shop_orders")
                        .whereEqualTo("userId", currentUid)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            allTransactions.clear();
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
                                orderBooking.showtimeStartAtSnapshot = 0L;
                                
                                Double priceObj = doc.getDouble("totalPrice");
                                double price = priceObj != null ? priceObj : 0.0;
                                orderBooking.total = price;
                                orderBooking.subtotal = price;
                                
                                orderBooking.bookingStatus = doc.getString("status");
                                orderBooking.paymentStatus = doc.getString("status");
                                
                                Long checkInAtObj = doc.getLong("checkInAt");
                                orderBooking.checkInAt = checkInAtObj != null ? checkInAtObj : 0;
                                
                                Long createdAtObj = doc.getLong("createdAt");
                                orderBooking.createdAt = createdAtObj != null ? createdAtObj : 0;

                                allTransactions.add(orderBooking);
                            }
                            
                            allTransactions.sort((t1, t2) -> Long.compare(t2.createdAt, t1.createdAt));
                            showLoading(false);
                            filterTransactionsByTab(tabLayout.getSelectedTabPosition());
                        })
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            Toast.makeText(TransactionHistoryActivity.this, "Lỗi: " + message, Toast.LENGTH_LONG).show();
                            layoutEmpty.setVisibility(View.VISIBLE);
                        });
            }
        });
    }

    private void filterTransactionsByTab(int tabPosition) {
        List<Booking> filteredList = new ArrayList<>();

        for (Booking booking : allTransactions) {
            String status = booking.bookingStatus != null ? booking.bookingStatus.toLowerCase() : "unknown";
            boolean isPaid = "confirmed".equals(status) || "success".equals(status);
            boolean isUsed = booking.checkInAt != null && booking.checkInAt > 0;

            if (tabPosition == 0) {
                // Tab "Đã thanh toán": đã thanh toán thành công và chưa check-in/quét
                if (isPaid && !isUsed) {
                    filteredList.add(booking);
                }
            } else if (tabPosition == 1) {
                // Tab "Đã quét": đã check-in
                if (isUsed) {
                    filteredList.add(booking);
                }
            }
        }

        if (filteredList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
            adapter.setBookings(filteredList);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            layoutEmpty.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.GONE);
        }
    }
}
