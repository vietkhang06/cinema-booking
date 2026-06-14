package com.example.cinemabooking.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.ui.customer.model.HomeBannerItem;

import java.util.ArrayList;
import java.util.List;

public class HomeBannerAdapter extends RecyclerView.Adapter<HomeBannerAdapter.BannerViewHolder> {

    // 🔥 FIX: đổi từ Integer → HomeBannerItem
    private final List<HomeBannerItem> banners = new ArrayList<>();

    // 🔥 FIX: nhận HomeBannerItem
    public void setBanners(List<HomeBannerItem> newBanners) {
        banners.clear();
        if (newBanners != null) {
            banners.addAll(newBanners);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_banner, parent, false);
        return new BannerViewHolder(view);
    }

    public interface OnBannerClickListener {
        void onBannerClick(HomeBannerItem item);
    }

    private OnBannerClickListener onBannerClickListener;

    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.onBannerClickListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        HomeBannerItem item = banners.get(position);

        // 🔥 dùng Glide load URL
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.login_icon)
                .into(holder.imgBanner);

        holder.itemView.setOnClickListener(v -> {
            if (onBannerClickListener != null) {
                onBannerClickListener.onBannerClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return banners.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {

        ImageView imgBanner;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}