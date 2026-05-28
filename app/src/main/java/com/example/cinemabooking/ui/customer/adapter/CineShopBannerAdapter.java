package com.example.cinemabooking.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.cinemabooking.R;

import java.util.ArrayList;
import java.util.List;

public class CineShopBannerAdapter extends RecyclerView.Adapter<CineShopBannerAdapter.BannerVH> {

    private List<String> bannerUrls = new ArrayList<>();

    public void setBanners(List<String> urls) {
        this.bannerUrls = urls != null ? urls : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BannerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cine_banner, parent, false);
        return new BannerVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerVH holder, int position) {
        String url = bannerUrls.get(position);

        // Use Glide to load the image URL into the ImageView
        Glide.with(holder.itemView.getContext())
                .load(url)
                .placeholder(R.drawable.bg_banner_placeholder) // Show orange while loading
                .error(R.drawable.bg_banner_placeholder)      // Show orange if URL fails
                .transition(DrawableTransitionOptions.withCrossFade()) // Smooth fade-in effect
                .into(holder.imgBanner);
    }

    @Override
    public int getItemCount() {
        return bannerUrls.size();
    }

    static class BannerVH extends RecyclerView.ViewHolder {
        ImageView imgBanner;

        BannerVH(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}