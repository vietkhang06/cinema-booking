package com.example.cinemabooking.ui.admin.cineshop;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.data.dto.CineShopItemDTO;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCineShopAdapter extends RecyclerView.Adapter<AdminCineShopAdapter.ProductViewHolder> {

    public interface OnProductAction {
        void onEdit(CineShopItemDTO item);
        void onDelete(CineShopItemDTO item);
    }

    private final List<CineShopItemDTO> items = new ArrayList<>();
    private final OnProductAction actionListener;

    public AdminCineShopAdapter(OnProductAction actionListener) {
        this.actionListener = actionListener;
    }

    public void submitList(List<CineShopItemDTO> newList) {
        items.clear();
        if (newList != null) {
            items.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_cineshop, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        CineShopItemDTO item = items.get(position);
        holder.bind(item, actionListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvCategory, tvPrice, tvStatus;
        ImageButton btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(CineShopItemDTO item, OnProductAction actionListener) {
            tvName.setText(item.name);

            // Category tag
            if ("CAT_SEASONAL".equalsIgnoreCase(item.categoryId)) {
                tvCategory.setText("Danh mục: Seasonal (Combo nước + bắp)");
            } else if ("CAT_MOVIE".equalsIgnoreCase(item.categoryId)) {
                tvCategory.setText("Danh mục: Movie Snack");
            } else {
                tvCategory.setText("Danh mục: " + item.categoryId);
            }

            // Format price to VND
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvPrice.setText("Giá: " + formatter.format(item.price));

            // Status label
            if (item.isActive && "available".equalsIgnoreCase(item.status)) {
                tvStatus.setText("Trạng thái: Có sẵn (Hoạt động)");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setText("Trạng thái: Không hoạt động / Hết hàng");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }

            // Image URL load using Glide
            if (!TextUtils.isEmpty(item.imageUrl)) {
                Glide.with(itemView.getContext())
                        .load(item.imageUrl)
                        .placeholder(R.drawable.login_icon)
                        .error(R.drawable.login_icon)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.login_icon);
            }

            btnEdit.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onEdit(item);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onDelete(item);
                }
            });
        }
    }
}
