package com.example.cinemabooking.ui.customer.profile;

import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.data.repository.PromotionRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Promotion;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.service.ProfileService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyPromotionListActivity extends AppCompatActivity {

    private RecyclerView rvMyPromotions;
    private View layoutPromoEmptyState;

    private PromotionRepositoryImpl promotionRepository;
    private ProfileService profileService;
    private final List<Promotion> activePromoList = new ArrayList<>();
    private MyPromoAdapter adapter;
    private String userLevel = "standard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_promotion_list);

        promotionRepository = new PromotionRepositoryImpl();
        profileService = ServiceProvider.getInstance().getProfileService();

        initViews();
        loadUserProfileAndPromotions();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvMyPromotions = findViewById(R.id.rvMyPromotions);
        layoutPromoEmptyState = findViewById(R.id.layoutPromoEmptyState);

        rvMyPromotions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyPromoAdapter(activePromoList);
        rvMyPromotions.setAdapter(adapter);
    }

    private void loadUserProfileAndPromotions() {
        // Fetch user profile first to get the member level
        profileService.getUserProfile(new ResultCallback<User>() {
            @Override
            public void onSuccess(User profileData) {
                if (profileData != null && profileData.memberLevel != null) {
                    userLevel = profileData.memberLevel.trim().toLowerCase();
                } else {
                    userLevel = "standard";
                }
                loadActivePromotions();
            }

            @Override
            public void onError(String message) {
                userLevel = "standard";
                loadActivePromotions();
            }
        });
    }

    private void loadActivePromotions() {
        long now = System.currentTimeMillis();
        promotionRepository.getAllPromotions(new ResultCallback<List<Promotion>>() {
            @Override
            public void onSuccess(List<Promotion> promotions) {
                activePromoList.clear();
                if (promotions != null) {
                    for (Promotion p : promotions) {
                        if (p.deleted == null || !p.deleted) {
                            // Check active status, time validity, and usage limit
                            boolean isActive = "active".equalsIgnoreCase(p.status)
                                    && (p.validFrom == null || p.validFrom <= now)
                                    && (p.validTo == null || p.validTo >= now)
                                    && (p.usageLimit == null || p.usageLimit <= 0 || (p.usedCount != null && p.usedCount < p.usageLimit));

                            if (isActive) {
                                // Check role applicability
                                boolean isRoleMatch = false;
                                if (p.targetRole == null || p.targetRole.trim().isEmpty() || p.targetRole.equalsIgnoreCase("all")) {
                                    isRoleMatch = true;
                                } else if (userLevel.contains(p.targetRole.trim().toLowerCase())) {
                                    isRoleMatch = true;
                                } else if (p.targetRole.trim().toLowerCase().contains(userLevel)) {
                                    isRoleMatch = true;
                                }

                                if (isRoleMatch) {
                                    // Check if it's a global promo (userId == null) or belongs to current user
                                    String currentUid = null;
                                    if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
                                        currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    }

                                    if (p.userId == null || p.userId.trim().isEmpty() || p.userId.equals(currentUid)) {
                                        activePromoList.add(p);
                                    }
                                }
                            }
                        }
                    }
                }
                updateUI();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MyPromotionListActivity.this, "Lỗi tải khuyến mãi: " + message, Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        if (activePromoList.isEmpty()) {
            layoutPromoEmptyState.setVisibility(View.VISIBLE);
            rvMyPromotions.setVisibility(View.GONE);
        } else {
            layoutPromoEmptyState.setVisibility(View.GONE);
            rvMyPromotions.setVisibility(View.VISIBLE);
        }
    }

    private void copyToClipboard(String code) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("PromoCode", code);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép mã: " + code, Toast.LENGTH_SHORT).show();
        }
    }

    private class MyPromoAdapter extends RecyclerView.Adapter<MyPromoAdapter.ViewHolder> {
        private final List<Promotion> items;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public MyPromoAdapter(List<Promotion> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_promotion, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Promotion p = items.get(position);

            holder.tvCode.setText(p.code != null ? p.code.toUpperCase(Locale.getDefault()) : "");
            holder.tvTitle.setText(p.title != null ? p.title : "");
            holder.tvDesc.setText(p.description != null ? p.description : "");

            // Expiry Date
            String dateEnd = (p.validTo != null && p.validTo > 0) ? dateFormat.format(new Date(p.validTo)) : "Không thời hạn";
            holder.tvValidity.setText("HSD: " + dateEnd);

            // Target Role badge
            if (p.targetRole != null && !p.targetRole.trim().isEmpty() && !p.targetRole.equalsIgnoreCase("all")) {
                holder.tvTargetRole.setText(p.targetRole.toUpperCase(Locale.getDefault()));
                holder.tvTargetRole.setVisibility(View.VISIBLE);
            } else {
                holder.tvTargetRole.setVisibility(View.GONE);
            }

            // Copy button click listener
            holder.btnCopyCode.setOnClickListener(v -> {
                if (p.code != null) {
                    copyToClipboard(p.code.toUpperCase(Locale.getDefault()));
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCode, tvTitle, tvDesc, tvValidity, tvTargetRole;
            View btnCopyCode;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCode = itemView.findViewById(R.id.tvPromoCode);
                tvTitle = itemView.findViewById(R.id.tvPromoTitle);
                tvDesc = itemView.findViewById(R.id.tvPromoDesc);
                tvValidity = itemView.findViewById(R.id.tvPromoValidity);
                tvTargetRole = itemView.findViewById(R.id.tvPromoTargetRole);
                btnCopyCode = itemView.findViewById(R.id.btnCopyCode);
            }
        }
    }
}
