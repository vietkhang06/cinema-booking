package com.example.cinemabooking.ui.admin.promotion;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.PromotionRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Promotion;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminPromotionListActivity extends AppCompatActivity {

    private ChipGroup chipGroupPromoFilters;
    private RecyclerView rvPromotions;
    private View layoutPromoEmptyState;
    private FloatingActionButton fabAddPromotion;

    private PromotionRepositoryImpl promotionRepository;
    private final List<Promotion> fullPromoList = new ArrayList<>();
    private final List<Promotion> filteredPromoList = new ArrayList<>();
    private PromoAdapter adapter;
    private int currentFilterId = R.id.chipPromoAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_promotion_list);

        promotionRepository = new PromotionRepositoryImpl();

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPromotions();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        chipGroupPromoFilters = findViewById(R.id.chipGroupPromoFilters);
        rvPromotions = findViewById(R.id.rvPromotions);
        layoutPromoEmptyState = findViewById(R.id.layoutPromoEmptyState);
        fabAddPromotion = findViewById(R.id.fabAddPromotion);

        rvPromotions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PromoAdapter(filteredPromoList);
        rvPromotions.setAdapter(adapter);
    }

    private void setupListeners() {
        chipGroupPromoFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                chipGroupPromoFilters.check(R.id.chipPromoAll);
                currentFilterId = R.id.chipPromoAll;
            } else {
                currentFilterId = checkedId;
            }
            applyFilters();
        });

        fabAddPromotion.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPromotionAddEditActivity.class);
            startActivity(intent);
        });
    }

    private void loadPromotions() {
        promotionRepository.getAllPromotions(new ResultCallback<List<Promotion>>() {
            @Override
            public void onSuccess(List<Promotion> promotions) {
                fullPromoList.clear();
                if (promotions != null) {
                    for (Promotion p : promotions) {
                        if (!p.deleted) {
                            fullPromoList.add(p);
                        }
                    }
                }
                applyFilters();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminPromotionListActivity.this, "Lỗi tải khuyến mãi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        filteredPromoList.clear();
        long now = System.currentTimeMillis();

        for (Promotion p : fullPromoList) {
            boolean matches = false;

            boolean isActive = "active".equalsIgnoreCase(p.status) && p.validFrom <= now && p.validTo >= now && p.usedCount < p.usageLimit;
            boolean isScheduled = "active".equalsIgnoreCase(p.status) && p.validFrom > now;
            boolean isExpiredOrDisabled = "inactive".equalsIgnoreCase(p.status) || p.validTo < now || p.usedCount >= p.usageLimit;

            if (currentFilterId == R.id.chipPromoAll) {
                matches = true;
            } else if (currentFilterId == R.id.chipPromoActive) {
                matches = isActive;
            } else if (currentFilterId == R.id.chipPromoScheduled) {
                matches = isScheduled;
            } else if (currentFilterId == R.id.chipPromoExpired) {
                matches = isExpiredOrDisabled;
            }

            if (matches) {
                filteredPromoList.add(p);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredPromoList.isEmpty()) {
            layoutPromoEmptyState.setVisibility(View.VISIBLE);
            rvPromotions.setVisibility(View.GONE);
        } else {
            layoutPromoEmptyState.setVisibility(View.GONE);
            rvPromotions.setVisibility(View.VISIBLE);
        }
    }

    private class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.ViewHolder> {
        private final List<Promotion> items;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public PromoAdapter(List<Promotion> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_promotion, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Promotion p = items.get(position);

            holder.tvCode.setText(p.code != null ? p.code.toUpperCase(Locale.getDefault()) : "");
            holder.tvTitle.setText(p.title != null ? p.title : "");
            holder.tvDesc.setText(p.description != null ? p.description : "");

            // Usage progress
            int usageLimit = p.usageLimit > 0 ? p.usageLimit : 1;
            holder.tvUsage.setText(p.usedCount + " / " + p.usageLimit);
            holder.pbUsage.setMax(usageLimit);
            holder.pbUsage.setProgress(p.usedCount);

            // Validity period
            String dateStart = p.validFrom > 0 ? dateFormat.format(new Date(p.validFrom)) : "";
            String dateEnd = p.validTo > 0 ? dateFormat.format(new Date(p.validTo)) : "";
            holder.tvValidity.setText(dateStart + " - " + dateEnd);

            // Status Badge
            long now = System.currentTimeMillis();
            boolean isActive = "active".equalsIgnoreCase(p.status) && p.validFrom <= now && p.validTo >= now && p.usedCount < p.usageLimit;
            boolean isScheduled = "active".equalsIgnoreCase(p.status) && p.validFrom > now;
            
            if (isActive) {
                holder.tvStatus.setText("ĐANG CHẠY");
                holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1E7DD"))); // Light Green
                holder.tvStatus.setTextColor(Color.parseColor("#0F5132"));
            } else if (isScheduled) {
                holder.tvStatus.setText("SẮP DIỄN RA");
                holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CFF4FC"))); // Light Blue
                holder.tvStatus.setTextColor(Color.parseColor("#087990"));
            } else {
                holder.tvStatus.setText("HẾT HẠN / TẮT");
                holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F8D7DA"))); // Light Red
                holder.tvStatus.setTextColor(Color.parseColor("#842029"));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AdminPromotionListActivity.this, AdminPromotionAddEditActivity.class);
                intent.putExtra("PROMO_ID", p.promoId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCode, tvStatus, tvTitle, tvDesc, tvUsage, tvValidity;
            ProgressBar pbUsage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCode = itemView.findViewById(R.id.tvPromoCode);
                tvStatus = itemView.findViewById(R.id.tvPromoStatus);
                tvTitle = itemView.findViewById(R.id.tvPromoTitle);
                tvDesc = itemView.findViewById(R.id.tvPromoDesc);
                tvUsage = itemView.findViewById(R.id.tvPromoUsageCount);
                tvValidity = itemView.findViewById(R.id.tvPromoValidity);
                pbUsage = itemView.findViewById(R.id.pbPromoUsage);
            }
        }
    }
}