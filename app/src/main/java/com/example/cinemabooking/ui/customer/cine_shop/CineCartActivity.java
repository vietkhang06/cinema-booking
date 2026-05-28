package com.example.cinemabooking.ui.customer.cine_shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * CineCartActivity — Màn hình Giỏ hàng.
 *
 * Luồng:
 *   CineShopFragment (icon cart) → CineCartActivity → CineCheckoutActivity
 */
public class CineCartActivity extends FragmentActivity {

    private RecyclerView rvCartItems;
    private LinearLayout layoutEmptyCart;
    private TextView tvCartTotal, btnCartCheckout;
    private ImageView btnCartBack;

    private CineCartAdapter adapter;
    private final DecimalFormat fmt = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cine_cart);

        bindViews();
        setupRecyclerView();
        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh khi quay lại từ Checkout
        refreshUI();
    }

    private void bindViews() {
        btnCartBack     = findViewById(R.id.btnCartBack);
        rvCartItems     = findViewById(R.id.rvCartItems);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        tvCartTotal     = findViewById(R.id.tvCartTotal);
        btnCartCheckout = findViewById(R.id.btnCartCheckout);

        btnCartBack.setOnClickListener(v -> finish());

        btnCartCheckout.setOnClickListener(v -> {
            if (CineCartManager.getInstance().isEmpty()) return;
            startActivity(new Intent(this, CineCheckoutActivity.class));
        });
    }

    private void setupRecyclerView() {
        List<CineCartManager.CartItem> items = CineCartManager.getInstance().getItems();
        adapter = new CineCartAdapter(items);
        adapter.setOnCartChangeListener(this::refreshUI);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(adapter);
    }

    private void refreshUI() {
        boolean empty = CineCartManager.getInstance().isEmpty();
        rvCartItems.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmptyCart.setVisibility(empty ? View.VISIBLE : View.GONE);

        double total = CineCartManager.getInstance().getTotalPrice();
        tvCartTotal.setText(fmt.format(total) + "đ");
    }
}
