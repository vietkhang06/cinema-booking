package com.example.cinemabooking.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.ui.admin.model.AdminFeatureItem;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AdminFeatureAdapter extends RecyclerView.Adapter<AdminFeatureAdapter.FeatureVH> {

    private final Context context;
    private final List<AdminFeatureItem> items;

    public AdminFeatureAdapter(Context context, List<AdminFeatureItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public FeatureVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_feature, parent, false);
        return new FeatureVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureVH holder, int position) {
        AdminFeatureItem item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvSubtitle.setText(item.subtitle);
        holder.ivIcon.setImageResource(item.iconRes);

        holder.card.setOnClickListener(v -> {
            if (item.targetActivity != null) {
                Intent intent = new Intent(context, item.targetActivity);
                if ("Ghế mẫu".equalsIgnoreCase(item.title)) {
                    intent.putExtra("extra_from_seat_menu", true);
                }
                context.startActivity(intent);
            } else {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                        .setTitle("Tính năng đang phát triển")
                        .setMessage("Chức năng quản trị " + item.title + " đang được thiết lập giao diện và chuẩn bị ra mắt trong giai đoạn tiếp theo của hệ thống.")
                        .setPositiveButton("Đồng ý", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FeatureVH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvSubtitle;

        public FeatureVH(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardAdminFeature);
            ivIcon = itemView.findViewById(R.id.ivAdminFeatureIcon);
            tvTitle = itemView.findViewById(R.id.tvAdminFeatureTitle);
            tvSubtitle = itemView.findViewById(R.id.tvAdminFeatureSubtitle);
        }
    }
}