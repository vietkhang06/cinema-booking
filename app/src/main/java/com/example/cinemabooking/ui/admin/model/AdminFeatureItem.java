package com.example.cinemabooking.ui.admin.model;

public class AdminFeatureItem {
    public final String title;
    public final String subtitle;
    public final int iconRes;
    public final Class<?> targetActivity;

    public AdminFeatureItem(String title, String subtitle, int iconRes, Class<?> targetActivity) {
        this.title = title;
        this.subtitle = subtitle;
        this.iconRes = iconRes;
        this.targetActivity = targetActivity;
    }
}