package com.example.cinemabooking.ui.customer.model;

public class HomeBannerItem {

    private String bannerId;
    private String imageUrl;

    public HomeBannerItem() {}

    public HomeBannerItem(String bannerId, String imageUrl) {
        this.bannerId = bannerId;
        this.imageUrl = imageUrl;
    }

    public String getBannerId() {
        return bannerId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}