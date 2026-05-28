package com.example.cinemabooking.ui.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.service.InvoiceService;
import com.example.cinemabooking.ui.staff.adapter.OrderItemAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Executors;

public class StaffCheckOrder extends AppCompatActivity {

    private TextView totalSnackPriceTV, amountSnackTV;
    private RecyclerView snackContainerView;
    private View backBtn;
    private LinearLayout snackLayout;
    private TextView noOrderFoundTV;
    private String invoiceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff_check_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        invoiceId = getIntent().getStringExtra("invoiceId");
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            Toast.makeText(this, "Hóa đơn không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindActions();
        loadSnackOrder();
    }

    private void initViews() {
        totalSnackPriceTV = findViewById(R.id.snack_total_price);
        amountSnackTV = findViewById(R.id.snack_amount);
        snackContainerView = findViewById(R.id.snack_container);
        snackLayout = findViewById(R.id.customer_order_layout);
        noOrderFoundTV = findViewById(R.id.no_order_found);
        backBtn = findViewById(R.id.back_btn);

        snackContainerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadSnackOrder() {
        Executors.newSingleThreadExecutor().execute(() -> {
            InvoiceService invoiceService = ServiceProvider.getInstance().getInvoiceService();
            InvoiceService.InvoiceDetail detail = invoiceService.getInvoiceDetail(invoiceId);

            runOnUiThread(() -> {
                if (detail == null || detail.snackOrder == null || detail.snackOrder.items == null || detail.snackOrder.items.isEmpty()) {
                    noOrderFoundTV.setVisibility(View.VISIBLE);
                    snackLayout.setVisibility(View.GONE);
                } else {
                    noOrderFoundTV.setVisibility(View.GONE);
                    snackLayout.setVisibility(View.VISIBLE);

                    totalSnackPriceTV.setText(String.format("Tổng giá: %,.0f vnd", detail.snackOrder.total));
                    int totalQty = detail.snackOrder.items.stream().mapToInt(item -> item.quantity).sum();
                    amountSnackTV.setText("Số lượng: " + totalQty);

                    OrderItemAdapter adapter = new OrderItemAdapter(detail.snackItems);
                    snackContainerView.setAdapter(adapter);
                }
            });
        });
    }
}