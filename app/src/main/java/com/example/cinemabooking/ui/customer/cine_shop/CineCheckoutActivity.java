package com.example.cinemabooking.ui.customer.cine_shop;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import android.content.Intent;
import com.example.cinemabooking.R;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.CineShopOrderRequestDTO;
import com.example.cinemabooking.data.dto.CineShopOrderResponseDTO;
import com.example.cinemabooking.data.remote.api.CineShopApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.example.cinemabooking.ui.customer.PaymentInstructionActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CineCheckoutActivity — Màn hình Thanh toán.
 *
 * Gồm:
 *   - Tóm tắt đơn hàng (lấy từ CineCartManager)
 *   - Chọn phương thức thanh toán (Zalopay / MoMo / OnePay)
 *   - Nhập mã voucher
 *   - Sử dụng điểm Star
 *   - Chọn tỉnh/thành + rạp nhận hàng
 *   - Bottom bar: tổng tiền + nút "Thanh Toán"
 */
public class CineCheckoutActivity extends FragmentActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private ImageView btnCheckoutBack;
    private LinearLayout layoutOrderSummary;
    private View radioZalopay, radioMomo, radioOnepay;
    private LinearLayout paymentZalopay, paymentMomo, paymentOnepay;
    private EditText etVoucherCode, etStarPoints;
    private TextView btnApplyVoucher, btnApplyStar;
    private TextView tvVoucherToggle, tvStarToggle;
    private LinearLayout layoutVoucherInput, layoutStarInput;
    private Spinner spinnerProvince, spinnerCinema;
    private TextView tvCheckoutTotal, btnCheckoutPay;

    // ── State ─────────────────────────────────────────────────────────────────
    private String selectedPayment = "zalopay"; // default
    private final DecimalFormat fmt = new DecimalFormat("#,###");

    // ── Province → Cinema data (mock) ─────────────────────────────────────────
    private static final String[] PROVINCES = {
            "Chọn tỉnh/thành", "Hà Nội", "TP. Hồ Chí Minh",
            "Đà Nẵng", "Cần Thơ", "Cà Mau", "Huế", "Nha Trang"
    };
    private static final String[][] CINEMAS = {
            {"Chọn rạp"},
            {"Galaxy Mê Linh", "Galaxy Nguyễn Du", "Galaxy Kinh Dương Vương"},
            {"Galaxy Tân Bình", "Galaxy Nguyễn Văn Quá", "Galaxy Co.opXtra"},
            {"Galaxy Đà Nẵng"},
            {"Galaxy Cần Thơ"},
            {"Galaxy Cà Mau"},
            {"Galaxy Huế"},
            {"Galaxy Nha Trang"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cine_checkout);

        bindViews();
        populateOrderSummary();
        setupPaymentMethods();
        setupPromoSection();
        setupSpinners();
        updateTotal();
    }

    // ── Bind ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        btnCheckoutBack    = findViewById(R.id.btnCheckoutBack);
        layoutOrderSummary = findViewById(R.id.layoutOrderSummary);

        radioZalopay  = findViewById(R.id.radioZalopay);
        radioMomo     = findViewById(R.id.radioMomo);
        radioOnepay   = findViewById(R.id.radioOnepay);
        paymentZalopay = findViewById(R.id.paymentZalopay);
        paymentMomo    = findViewById(R.id.paymentMomo);
        paymentOnepay  = findViewById(R.id.paymentOnepay);

        etVoucherCode     = findViewById(R.id.etVoucherCode);
        etStarPoints      = findViewById(R.id.etStarPoints);
        btnApplyVoucher   = findViewById(R.id.btnApplyVoucher);
        btnApplyStar      = findViewById(R.id.btnApplyStar);
        tvVoucherToggle   = findViewById(R.id.tvVoucherToggle);
        tvStarToggle      = findViewById(R.id.tvStarToggle);
        layoutVoucherInput = findViewById(R.id.layoutVoucherInput);
        layoutStarInput   = findViewById(R.id.layoutStarInput);

        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerCinema   = findViewById(R.id.spinnerCinema);
        tvCheckoutTotal = findViewById(R.id.tvCheckoutTotal);
        btnCheckoutPay  = findViewById(R.id.btnCheckoutPay);

        btnCheckoutBack.setOnClickListener(v -> finish());

        btnCheckoutPay.setOnClickListener(v -> {
            if (selectedPayment == null) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : null;
            if (currentUid == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            List<CineCartManager.CartItem> items = CineCartManager.getInstance().getItems();
            if (items.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create summary fields
            String primaryItemName = "";
            String primaryImageUrl = "";
            int totalQuantity = 0;
            double totalPrice = CineCartManager.getInstance().getTotalPrice();

            if (items.size() > 0) {
                CineCartManager.CartItem firstItem = items.get(0);
                primaryItemName = firstItem.snack.name;
                primaryImageUrl = firstItem.snack.imageUrl;
                if (items.size() > 1) {
                    primaryItemName += " và " + (items.size() - 1) + " sản phẩm khác";
                }
            }

            for (CineCartManager.CartItem item : items) {
                totalQuantity += item.quantity;
            }

            CineShopOrderRequestDTO request = new CineShopOrderRequestDTO(
                    primaryItemName,
                    primaryImageUrl,
                    totalQuantity,
                    totalPrice,
                    selectedPayment.toUpperCase()
            );

            btnCheckoutPay.setEnabled(false);

            CineShopApiService apiService = RetrofitClient.getInstance().create(CineShopApiService.class);
            apiService.createOrder(request).enqueue(new retrofit2.Callback<ApiResponse<CineShopOrderResponseDTO>>() {
                @Override
                public void onResponse(retrofit2.Call<ApiResponse<CineShopOrderResponseDTO>> call, retrofit2.Response<ApiResponse<CineShopOrderResponseDTO>> response) {
                    btnCheckoutPay.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        CineShopOrderResponseDTO orderResponse = response.body().getData();
                        if (orderResponse != null) {
                            Toast.makeText(CineCheckoutActivity.this, "Đặt hàng thành công! Vui lòng hoàn tất thanh toán. 🎉", Toast.LENGTH_LONG).show();
                            CineCartManager.getInstance().clear();

                            // Redirect to PaymentInstructionActivity
                            Intent intent = new Intent(CineCheckoutActivity.this, PaymentInstructionActivity.class);
                            intent.putExtra(PaymentInstructionActivity.EXTRA_BOOKING_ID, orderResponse.getOrderId());
                            intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_ID, orderResponse.getPaymentId());
                            intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_CODE, orderResponse.getPaymentCode());
                            intent.putExtra(PaymentInstructionActivity.EXTRA_AMOUNT, orderResponse.getTotalPrice());
                            intent.putExtra(PaymentInstructionActivity.EXTRA_PAYMENT_METHOD, orderResponse.getPaymentMethod());
                            intent.putExtra("createdAt", orderResponse.getCreatedAt());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CineCheckoutActivity.this, "Không nhận được thông tin đơn hàng.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(CineCheckoutActivity.this, "Lỗi tạo đơn hàng: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ApiResponse<CineShopOrderResponseDTO>> call, Throwable t) {
                    btnCheckoutPay.setEnabled(true);
                    Toast.makeText(CineCheckoutActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // ── Order summary ─────────────────────────────────────────────────────────

    private void populateOrderSummary() {
        layoutOrderSummary.removeAllViews();
        List<CineCartManager.CartItem> items = CineCartManager.getInstance().getItems();

        for (CineCartManager.CartItem item : items) {
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_cine_cart, layoutOrderSummary, false);

            // Hide quantity controls & delete in checkout view
            row.findViewById(R.id.btnCartMinus).setVisibility(View.GONE);
            row.findViewById(R.id.btnCartPlus).setVisibility(View.GONE);
            row.findViewById(R.id.tvCartQty).setVisibility(View.GONE);
            row.findViewById(R.id.btnCartDelete).setVisibility(View.GONE);

            ImageView imgCartItem = row.findViewById(R.id.imgCartItem);
            if (imgCartItem != null) {
                Glide.with(this)
                        .load(item.snack.imageUrl)
                        .placeholder(R.drawable.bg_banner_placeholder)
                        .error(R.drawable.bg_banner_placeholder)
                        .into(imgCartItem);
            }

            ((TextView) row.findViewById(R.id.tvCartItemName))
                    .setText(item.quantity + "x " + item.snack.name);
            ((TextView) row.findViewById(R.id.tvCartItemPrice))
                    .setText(fmt.format(item.subtotal()) + "đ");

            layoutOrderSummary.addView(row);
        }
    }

    // ── Payment methods ───────────────────────────────────────────────────────

    private void setupPaymentMethods() {
        selectPayment("zalopay"); // default

        paymentZalopay.setOnClickListener(v -> selectPayment("zalopay"));
        paymentMomo.setOnClickListener(v    -> selectPayment("momo"));
        paymentOnepay.setOnClickListener(v  -> selectPayment("onepay"));
    }

    private void selectPayment(String method) {
        selectedPayment = method;
        radioZalopay.setBackgroundResource(
                "zalopay".equals(method) ? R.drawable.bg_radio_selected_cine : R.drawable.bg_radio_unselected_cine);
        radioMomo.setBackgroundResource(
                "momo".equals(method) ? R.drawable.bg_radio_selected_cine : R.drawable.bg_radio_unselected_cine);
        radioOnepay.setBackgroundResource(
                "onepay".equals(method) ? R.drawable.bg_radio_selected_cine : R.drawable.bg_radio_unselected_cine);
    }

    // ── Promo section toggle ──────────────────────────────────────────────────

    private void setupPromoSection() {
        // Voucher toggle
        tvVoucherToggle.setOnClickListener(v -> {
            boolean visible = layoutVoucherInput.getVisibility() == View.VISIBLE;
            layoutVoucherInput.setVisibility(visible ? View.GONE : View.VISIBLE);
            tvVoucherToggle.setText(visible ? "▼" : "▲");
        });

        // Star toggle
        tvStarToggle.setOnClickListener(v -> {
            boolean visible = layoutStarInput.getVisibility() == View.VISIBLE;
            layoutStarInput.setVisibility(visible ? View.GONE : View.VISIBLE);
            tvStarToggle.setText(visible ? "▼" : "▲");
        });

        // Apply voucher
        btnApplyVoucher.setOnClickListener(v -> {
            String code = etVoucherCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: validate voucher code via API
            Toast.makeText(this, "Mã \"" + code + "\" không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
        });

        // Apply stars
        btnApplyStar.setOnClickListener(v -> {
            String pts = etStarPoints.getText().toString().trim();
            if (pts.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điểm Star", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: validate Star points via API
            Toast.makeText(this, "Bạn không đủ điểm Star để áp dụng", Toast.LENGTH_SHORT).show();
        });
    }

    // ── Spinners (Province + Cinema) ──────────────────────────────────────────

    private void setupSpinners() {
        // Province spinner
        SpinnerAdapter provinceAdapter = new SpinnerAdapter(this,
                Arrays.asList(PROVINCES));
        spinnerProvince.setAdapter(provinceAdapter);

        // Khi chọn tỉnh → update cinema spinner
        spinnerProvince.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent,
                                               View view, int position, long id) {
                        String[] cinemasForProvince = CINEMAS[position];
                        SpinnerAdapter cinemaAdapter = new SpinnerAdapter(
                                CineCheckoutActivity.this,
                                Arrays.asList(cinemasForProvince));
                        spinnerCinema.setAdapter(cinemaAdapter);
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });

        // Default to "Cà Mau" (index 5)
        spinnerProvince.setSelection(5);
    }

    // ── Total ─────────────────────────────────────────────────────────────────

    private void updateTotal() {
        double total = CineCartManager.getInstance().getTotalPrice();
        tvCheckoutTotal.setText(fmt.format(total) + "đ");
    }

    // ── Custom Spinner Adapter ────────────────────────────────────────────────

    /** Simple spinner adapter with custom text style */
    private static class SpinnerAdapter extends ArrayAdapter<String> {
        SpinnerAdapter(Context ctx, List<String> items) {
            super(ctx, android.R.layout.simple_spinner_item, items);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
    }
}
