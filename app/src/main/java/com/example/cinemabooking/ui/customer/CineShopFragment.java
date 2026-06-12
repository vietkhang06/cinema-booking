package com.example.cinemabooking.ui.customer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cinemabooking.R;
import com.example.cinemabooking.domain.model.Snack;
import com.example.cinemabooking.ui.customer.adapter.CineShopAdapter;
import com.example.cinemabooking.ui.customer.adapter.CineShopBannerAdapter;
import com.example.cinemabooking.ui.customer.cine_shop.CineCartActivity;
import com.example.cinemabooking.core.navigation.AppNavigator;
import com.google.firebase.auth.FirebaseAuth;

//Zikenic was here
import com.example.cinemabooking.data.dto.CineShopBannerDTO;
import com.example.cinemabooking.data.dto.CineShopItemDTO;
import com.example.cinemabooking.utils.CineShopSeeder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CineShopFragment — màn hình "Trang sản phẩm" (Star Shop / Cine Shop).
 *
 * Giao diện theo phong cách Galaxy Cinema:
 *   • Banner carousel tự động chạy mỗi 3 giây
 *   • 2 tab: SEASONAL / MOVIE
 *   • Danh sách sản phẩm dạng 1 cột (full-width)
 *   • Mỗi item có 2 nút: "MUA NGAY" và "THÊM VÀO GIỎ HÀNG"
 *
 * Dữ liệu hiện dùng mock. Khi Firebase sẵn sàng, gọi SnackRepository qua UseCase.
 */
public class CineShopFragment extends Fragment {

    // ── Views ────────────────────────────────────────────────────────────────
    private ViewPager2 bannerPager;
    private LinearLayout bannerDots;
    private TextView tabSeasonal, tabMovie;
    private RecyclerView rvProducts;
    private ImageView btnCart;
    private android.widget.ProgressBar loadingProgress;
    private TextView tvEmptyState;
    private LinearLayout layoutLoginRequired;
    private com.google.android.material.button.MaterialButton btnLoginRequired;
    private androidx.core.widget.NestedScrollView scrollContent;

    // ── Adapters ─────────────────────────────────────────────────────────────
    private CineShopAdapter productAdapter;
    private CineShopBannerAdapter bannerAdapter;

    // ── Data ─────────────────────────────────────────────────────────────────
    private final List<Snack> allProducts = new ArrayList<>();
    private final List<View> dotViews     = new ArrayList<>();

    // ── Banner auto-scroll ────────────────────────────────────────────────────
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;

    // ── Tab state ─────────────────────────────────────────────────────────────
    /** SEASONAL → {"CAT_COMBO", "CAT_POPCORN"}; MOVIE → {"CAT_DRINK", "CAT_SNACK"} */
    private static final String TAB_SEASONAL = "seasonal";
    private static final String TAB_MOVIE    = "movie";
    private String currentTab = TAB_SEASONAL;

    // ── Cart total (hiển thị icon giỏ hàng) ──────────────────────────────────
    private int cartItemCount = 0;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cine_shop, container, false);

        // Kích hoạt Seeder khởi tạo dữ liệu mẫu nếu Firestore trống
        CineShopSeeder.seedIfNeeded();

        bindViews(view);
        setupBanner();
        setupTabs();
        setupRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginState();
    }

    private void checkLoginState() {
        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (isLoggedIn) {
            layoutLoginRequired.setVisibility(View.GONE);
            scrollContent.setVisibility(View.VISIBLE);
            
            // Load data if empty
            if (bannerAdapter != null && bannerAdapter.getItemCount() == 0) {
                fetchBannersFromDatabase();
            }
            if (allProducts.isEmpty()) {
                loadCineShopItems();
            }
            startBannerAutoScroll();
        } else {
            layoutLoginRequired.setVisibility(View.VISIBLE);
            scrollContent.setVisibility(View.GONE);
            stopBannerAutoScroll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBannerAutoScroll();
    }

    // ── Bind views ───────────────────────────────────────────────────────────

    private void bindViews(View view) {
        bannerPager  = view.findViewById(R.id.bannerPager);
        bannerDots   = view.findViewById(R.id.bannerDots);
        tabSeasonal  = view.findViewById(R.id.tabSeasonal);
        tabMovie     = view.findViewById(R.id.tabMovie);
        rvProducts   = view.findViewById(R.id.rvProducts);
        btnCart      = view.findViewById(R.id.btnCart);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        tvEmptyState    = view.findViewById(R.id.tvEmptyState);
        layoutLoginRequired = view.findViewById(R.id.layoutLoginRequired);
        btnLoginRequired = view.findViewById(R.id.btnLoginRequired);
        scrollContent   = view.findViewById(R.id.scrollContent);

        btnCart.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(requireContext(), CineCartActivity.class));
            } else {
                AppNavigator.goToLoginForBooking(requireActivity());
            }
        });

        if (btnLoginRequired != null) {
            btnLoginRequired.setOnClickListener(v -> 
                    AppNavigator.goToLoginForBooking(requireActivity())
            );
        }
    }

    // ── Banner ───────────────────────────────────────────────────────────────
    //Zikenic was here
    private void setupBanner() {
        bannerAdapter = new CineShopBannerAdapter();
        bannerPager.setAdapter(bannerAdapter);

        bannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void fetchBannersFromDatabase() {
        FirebaseFirestore.getInstance().collection("cine_shop_banners")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CineShopBannerDTO> activeBanners = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CineShopBannerDTO banner = doc.toObject(CineShopBannerDTO.class);
                        if (banner.isActive) {
                            activeBanners.add(banner);
                        }
                    }

                    // Sắp xếp banner theo sortOrder tăng dần trên client
                    activeBanners.sort((b1, b2) -> Integer.compare(b1.sortOrder, b2.sortOrder));

                    List<String> bannerUrls = new ArrayList<>();
                    for (CineShopBannerDTO banner : activeBanners) {
                        if (banner.imageUrl != null) {
                            bannerUrls.add(banner.imageUrl);
                        }
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            bannerAdapter.setBanners(bannerUrls);
                            setupBannerDots(bannerUrls.size());

                            // Restart auto-scroll with new data
                            stopBannerAutoScroll();
                            startBannerAutoScroll();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CineShopFragment", "Failed to fetch banners from Firestore: " + e.getMessage());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            bannerAdapter.setBanners(new ArrayList<>());
                            setupBannerDots(0);
                            stopBannerAutoScroll();
                        });
                    }
                });
    }

    private void setupBannerDots(int count) {
        bannerDots.removeAllViews();
        dotViews.clear();

        for (int i = 0; i < count; i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(6), dpToPx(6));
            params.setMarginStart(dpToPx(3));
            params.setMarginEnd(dpToPx(3));
            dot.setLayoutParams(params);
            dot.setBackgroundColor(i == 0
                    ? Color.parseColor("#1E1A23")
                    : Color.parseColor("#CCCCCC"));
            bannerDots.addView(dot);
            dotViews.add(dot);
        }
    }

    private void updateDots(int selected) {
        for (int i = 0; i < dotViews.size(); i++) {
            dotViews.get(i).setBackgroundColor(
                    i == selected
                            ? Color.parseColor("#1E1A23")
                            : Color.parseColor("#CCCCCC"));
        }
    }

    private void startBannerAutoScroll() {
        int count = bannerAdapter.getItemCount();
        if (count <= 1) return;

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int next = (bannerPager.getCurrentItem() + 1) % count;
                bannerPager.setCurrentItem(next, true);
                bannerHandler.postDelayed(this, 3000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    private void stopBannerAutoScroll() {
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────

    private void setupTabs() {
        tabSeasonal.setOnClickListener(v -> selectTab(TAB_SEASONAL));
        tabMovie.setOnClickListener(v    -> selectTab(TAB_MOVIE));
        applyTabStyle(TAB_SEASONAL); // default
    }

    private void selectTab(String tab) {
        currentTab = tab;
        applyTabStyle(tab);
        filterProducts(tab);
    }

    private void applyTabStyle(String tab) {
        boolean isSeasonal = TAB_SEASONAL.equals(tab);

        // SEASONAL tab
        tabSeasonal.setBackgroundResource(isSeasonal
                ? R.drawable.bg_tab_cine_selected
                : android.R.color.transparent);
        tabSeasonal.setTextColor(isSeasonal
                ? Color.WHITE
                : Color.parseColor("#555555"));

        // MOVIE tab
        tabMovie.setBackgroundResource(isSeasonal
                ? android.R.color.transparent
                : R.drawable.bg_tab_cine_selected);
        tabMovie.setTextColor(isSeasonal
                ? Color.parseColor("#555555")
                : Color.WHITE);
    }

    // ── RecyclerView ─────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        productAdapter = new CineShopAdapter();
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(productAdapter);
        rvProducts.setNestedScrollingEnabled(false);

        productAdapter.setOnCartChangedListener(
                totalCount -> {
                    cartItemCount = totalCount;
                    // TODO: hiển thị badge số lượng trên icon giỏ hàng nếu cần
                });
    }

    // cart badge count

    // ── Data ─────────────────────────────────────────────────────────────────

    private void loadCineShopItems() {
        if (loadingProgress != null) {
            loadingProgress.setVisibility(View.VISIBLE);
        }
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }

        FirebaseFirestore.getInstance().collection("cine_shop_items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (loadingProgress != null) {
                        loadingProgress.setVisibility(View.GONE);
                    }

                    List<CineShopItemDTO> activeItems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CineShopItemDTO dto = doc.toObject(CineShopItemDTO.class);
                        if (dto.isActive) {
                            activeItems.add(dto);
                        }
                    }

                    // Sắp xếp sản phẩm theo sortOrder tăng dần trên client
                    activeItems.sort((i1, i2) -> Integer.compare(i1.sortOrder, i2.sortOrder));

                    allProducts.clear();
                    android.util.Log.d("CineShopFragment", "Successfully fetched and client-sorted " + activeItems.size() + " active CineShop products.");
                    for (CineShopItemDTO dto : activeItems) {
                        Snack product = new Snack();
                        product.snackId = dto.itemId;
                        product.categoryId = dto.categoryId;
                        product.name = dto.name;
                        product.description = dto.description;
                        product.price = dto.price;
                        product.imageUrl = dto.imageUrl;
                        product.isAvailable = dto.isActive && "available".equals(dto.status);
                        product.status = dto.status;
                        product.createdAt = System.currentTimeMillis();
                        product.updatedAt = System.currentTimeMillis();
                        product.deleted = false;
                        allProducts.add(product);
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> filterProducts(currentTab));
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CineShopFragment", "Failed to fetch CineShop items: " + e.getMessage());
                    if (loadingProgress != null) {
                        loadingProgress.setVisibility(View.GONE);
                    }
                    if (tvEmptyState != null) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Lỗi tải sản phẩm: " + e.getMessage());
                    }
                });
    }

    private void filterProducts(String tab) {
        List<Snack> filtered = new ArrayList<>();
        String targetCategory = TAB_SEASONAL.equals(tab) ? "CAT_SEASONAL" : "CAT_MOVIE";

        for (Snack product : allProducts) {
            if (targetCategory.equals(product.categoryId)) {
                filtered.add(product);
            }
        }
        productAdapter.setProducts(filtered);

        if (tvEmptyState != null) {
            if (filtered.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Không có sản phẩm nào khả dụng.");
            } else {
                tvEmptyState.setVisibility(View.GONE);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
