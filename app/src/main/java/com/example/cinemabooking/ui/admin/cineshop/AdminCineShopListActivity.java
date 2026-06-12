package com.example.cinemabooking.ui.admin.cineshop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.dto.CineShopItemDTO;
import com.example.cinemabooking.ui.admin.AdminBottomNavHelper;
import com.example.cinemabooking.ui.admin.log.AdminAuditLogger;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminCineShopListActivity extends BaseActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    private static final String TAG = "AdminCineShopList";

    private RecyclerView rvProducts;
    private TextView tvEmpty;
    private MaterialButton btnAddProduct;

    private AdminCineShopAdapter adapter;
    private final List<CineShopItemDTO> productList = new ArrayList<>();
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cineshop_list);



        initViews();
        setupList();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void initViews() {
        rvProducts = findViewById(R.id.rvProducts);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupList() {
        adapter = new AdminCineShopAdapter(new AdminCineShopAdapter.OnProductAction() {
            @Override
            public void onEdit(CineShopItemDTO item) {
                if (item == null) return;
                openForm(item.itemId);
            }

            @Override
            public void onDelete(CineShopItemDTO item) {
                if (item == null) return;
                confirmDelete(item);
            }
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setHasFixedSize(true);
        rvProducts.setAdapter(adapter);
    }

    private void bindActions() {
        btnAddProduct.setOnClickListener(v -> openForm(null));
    }

    private void loadProducts() {
        if (isLoading) return;
        isLoading = true;
        showLoading(true);

        FirebaseFirestore.getInstance().collection("cine_shop_items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    isLoading = false;
                    showLoading(false);

                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CineShopItemDTO item = doc.toObject(CineShopItemDTO.class);
                        productList.add(item);
                    }

                    // Sort by sortOrder ascending
                    productList.sort((i1, i2) -> Integer.compare(i1.sortOrder, i2.sortOrder));

                    adapter.submitList(new ArrayList<>(productList));
                    tvEmpty.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    isLoading = false;
                    showLoading(false);
                    Log.e(TAG, "Load products error: " + e.getMessage());
                    showToast("Lỗi tải sản phẩm: " + e.getMessage());
                });
    }

    private void openForm(String productId) {
        Intent intent = new Intent(this, AdminCineShopFormActivity.class);
        if (productId != null) {
            intent.putExtra(EXTRA_PRODUCT_ID, productId);
        }
        startActivity(intent);
    }

    private void confirmDelete(CineShopItemDTO item) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá sản phẩm")
                .setMessage("Bạn có chắc muốn xoá \"" + item.name + "\"?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    showLoading(true);

                    FirebaseFirestore.getInstance().collection("cine_shop_items")
                            .document(item.itemId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                showLoading(false);
                                showToast("Đã xoá sản phẩm");
                                AdminAuditLogger.log(
                                        "DELETE_CINESHOP_ITEM", "CINESHOP", item.itemId, "Đã xoá sản phẩm CineShop: " + item.name
                                );
                                loadProducts();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                showToast("Xoá thất bại: " + e.getMessage());
                            });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
