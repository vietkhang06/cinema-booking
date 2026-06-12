package com.example.cinemabooking.ui.admin.cineshop;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.dto.CineShopItemDTO;
import com.example.cinemabooking.ui.admin.log.AdminAuditLogger;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class AdminCineShopFormActivity extends BaseActivity {

    private TextView tvFormTitle;
    private TextInputLayout tilName, tilDescription, tilPrice, tilImageUrl, tilStock, tilSortOrder, tilCategory, tilStatus;
    private TextInputEditText edtName, edtDescription, edtPrice, edtImageUrl, edtStock, edtSortOrder;
    private MaterialAutoCompleteTextView actvCategory, actvStatus;
    private SwitchMaterial swActive;
    private MaterialButton btnSave;

    private String productId;
    private boolean isEditMode = false;
    private CineShopItemDTO currentItem;

    private static final String[] CATEGORY_LABELS = {"Seasonal (Bắp + Nước)", "Movie Snack (Đồ ăn nhẹ)"};
    private static final String[] CATEGORY_VALUES = {"CAT_SEASONAL", "CAT_MOVIE"};

    private static final String[] STATUS_LABELS = {"Có sẵn (Available)", "Tạm hết hàng (Unavailable)"};
    private static final String[] STATUS_VALUES = {"available", "unavailable"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cineshop_form);

        productId = getIntent().getStringExtra(AdminCineShopListActivity.EXTRA_PRODUCT_ID);
        isEditMode = !TextUtils.isEmpty(productId);

        initViews();
        setupDropdowns();
        bindActions();

        if (isEditMode) {
            tvFormTitle.setText("Sửa sản phẩm");
            btnSave.setText("Cập nhật sản phẩm");
            loadProduct(productId);
        } else {
            tvFormTitle.setText("Thêm sản phẩm");
            btnSave.setText("Thêm sản phẩm");
        }
    }

    private void initViews() {
        tvFormTitle = findViewById(R.id.tvFormTitle);

        tilName = findViewById(R.id.tilName);
        tilDescription = findViewById(R.id.tilDescription);
        tilPrice = findViewById(R.id.tilPrice);
        tilImageUrl = findViewById(R.id.tilImageUrl);
        tilStock = findViewById(R.id.tilStock);
        tilSortOrder = findViewById(R.id.tilSortOrder);
        tilCategory = findViewById(R.id.tilCategory);
        tilStatus = findViewById(R.id.tilStatus);

        edtName = findViewById(R.id.edtName);
        edtDescription = findViewById(R.id.edtDescription);
        edtPrice = findViewById(R.id.edtPrice);
        edtImageUrl = findViewById(R.id.edtImageUrl);
        edtStock = findViewById(R.id.edtStock);
        edtSortOrder = findViewById(R.id.edtSortOrder);

        actvCategory = findViewById(R.id.actvCategory);
        actvStatus = findViewById(R.id.actvStatus);
        swActive = findViewById(R.id.swActive);
        btnSave = findViewById(R.id.btnSave);

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupDropdowns() {
        actvCategory.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                Arrays.asList(CATEGORY_LABELS)
        ));

        actvStatus.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                Arrays.asList(STATUS_LABELS)
        ));
    }

    private void bindActions() {
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void loadProduct(String id) {
        showLoading(true);
        FirebaseFirestore.getInstance().collection("cine_shop_items")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    if (documentSnapshot.exists()) {
                        currentItem = documentSnapshot.toObject(CineShopItemDTO.class);
                        if (currentItem != null) {
                            bindProduct(currentItem);
                        }
                    } else {
                        showToast("Không tìm thấy sản phẩm");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showToast("Lỗi tải thông tin: " + e.getMessage());
                    finish();
                });
    }

    private void bindProduct(CineShopItemDTO item) {
        edtName.setText(item.name);
        edtDescription.setText(item.description);
        edtPrice.setText(String.valueOf(item.price));
        edtImageUrl.setText(item.imageUrl);
        edtStock.setText(String.valueOf(item.stock));
        edtSortOrder.setText(String.valueOf(item.sortOrder));
        swActive.setChecked(item.isActive);

        // Select Category
        int catIndex = Arrays.asList(CATEGORY_VALUES).indexOf(item.categoryId);
        if (catIndex >= 0) {
            actvCategory.setText(CATEGORY_LABELS[catIndex], false);
        } else {
            actvCategory.setText(item.categoryId, false);
        }

        // Select Status
        int statusIndex = Arrays.asList(STATUS_VALUES).indexOf(item.status);
        if (statusIndex >= 0) {
            actvStatus.setText(STATUS_LABELS[statusIndex], false);
        } else {
            actvStatus.setText(item.status, false);
        }
    }

    private void saveProduct() {
        clearErrors();

        String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
        String description = edtDescription.getText() != null ? edtDescription.getText().toString().trim() : "";
        String priceText = edtPrice.getText() != null ? edtPrice.getText().toString().trim() : "";
        String imageUrl = edtImageUrl.getText() != null ? edtImageUrl.getText().toString().trim() : "";
        String stockText = edtStock.getText() != null ? edtStock.getText().toString().trim() : "";
        String sortOrderText = edtSortOrder.getText() != null ? edtSortOrder.getText().toString().trim() : "";
        String categoryLabel = actvCategory.getText().toString();
        String statusLabel = actvStatus.getText().toString();

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Nhập tên sản phẩm");
            return;
        }

        if (TextUtils.isEmpty(priceText)) {
            tilPrice.setError("Nhập giá sản phẩm");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            tilPrice.setError("Giá sản phẩm không hợp lệ");
            return;
        }

        int stock = 99;
        if (!TextUtils.isEmpty(stockText)) {
            try {
                stock = Integer.parseInt(stockText);
            } catch (NumberFormatException e) {
                tilStock.setError("Số lượng không hợp lệ");
                return;
            }
        }

        int sortOrder = 0;
        if (!TextUtils.isEmpty(sortOrderText)) {
            try {
                sortOrder = Integer.parseInt(sortOrderText);
            } catch (NumberFormatException e) {
                tilSortOrder.setError("Thứ tự không hợp lệ");
                return;
            }
        }

        int catIndex = Arrays.asList(CATEGORY_LABELS).indexOf(categoryLabel);
        String categoryId = catIndex >= 0 ? CATEGORY_VALUES[catIndex] : "CAT_SEASONAL";

        int statusIndex = Arrays.asList(STATUS_LABELS).indexOf(statusLabel);
        String status = statusIndex >= 0 ? STATUS_VALUES[statusIndex] : "available";

        boolean isActive = swActive.isChecked();

        showLoading(true);
        btnSave.setEnabled(false);

        CineShopItemDTO item = currentItem != null ? currentItem : new CineShopItemDTO();
        item.name = name;
        item.description = description;
        item.price = price;
        item.imageUrl = imageUrl;
        item.stock = stock;
        item.sortOrder = sortOrder;
        item.categoryId = categoryId;
        item.status = status;
        item.isActive = isActive;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef;

        if (isEditMode) {
            docRef = db.collection("cine_shop_items").document(productId);
            item.itemId = productId;
        } else {
            docRef = db.collection("cine_shop_items").document();
            item.itemId = docRef.getId();
        }

        docRef.set(item)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    showToast(isEditMode ? "Đã cập nhật sản phẩm" : "Đã thêm sản phẩm");
                    AdminAuditLogger.log(
                            isEditMode ? "UPDATE_CINESHOP_ITEM" : "CREATE_CINESHOP_ITEM",
                            "CINESHOP",
                            item.itemId,
                            (isEditMode ? "Đã cập nhật sản phẩm CineShop: " : "Đã thêm sản phẩm CineShop mới: ") + item.name
                    );
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    btnSave.setEnabled(true);
                    showToast("Lưu thất bại: " + e.getMessage());
                });
    }

    private void clearErrors() {
        tilName.setError(null);
        tilPrice.setError(null);
        tilStock.setError(null);
        tilSortOrder.setError(null);
    }
}
