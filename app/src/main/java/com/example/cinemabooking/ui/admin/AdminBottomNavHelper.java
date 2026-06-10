package com.example.cinemabooking.ui.admin;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;
import com.example.cinemabooking.R;
import com.google.android.material.card.MaterialCardView;
import com.example.cinemabooking.ui.admin.cinema.AdminCinemaListActivity;
import com.example.cinemabooking.ui.admin.movie.AdminMovieListActivity;
import com.example.cinemabooking.ui.admin.log.AdminAuditLogActivity;
import com.example.cinemabooking.ui.staff.StaffProfileActivity;

public class AdminBottomNavHelper {

    public static void setupAdminBottomNavigation(Activity activity, int selectedIndex) {
        LinearLayout bottomNavContainer = activity.findViewById(R.id.bottomNavContainer);
        if (bottomNavContainer == null) return;

        MaterialCardView navAdminHomeCard = activity.findViewById(R.id.navAdminHomeCard);
        MaterialCardView navAdminCinemaCard = activity.findViewById(R.id.navAdminCinemaCard);
        MaterialCardView navAdminMovieCard = activity.findViewById(R.id.navAdminMovieCard);
        MaterialCardView navAdminLogCard = activity.findViewById(R.id.navAdminLogCard);
        MaterialCardView navAdminProfileCard = activity.findViewById(R.id.navAdminProfileCard);

        TextView navAdminHomeLabel = activity.findViewById(R.id.navAdminHomeLabel);
        TextView navAdminCinemaLabel = activity.findViewById(R.id.navAdminCinemaLabel);
        TextView navAdminMovieLabel = activity.findViewById(R.id.navAdminMovieLabel);
        TextView navAdminLogLabel = activity.findViewById(R.id.navAdminLogLabel);
        TextView navAdminProfileLabel = activity.findViewById(R.id.navAdminProfileLabel);

        ImageView navAdminHomeIcon = activity.findViewById(R.id.navAdminHomeIcon);
        ImageView navAdminCinemaIcon = activity.findViewById(R.id.navAdminCinemaIcon);
        ImageView navAdminMovieIcon = activity.findViewById(R.id.navAdminMovieIcon);
        ImageView navAdminLogIcon = activity.findViewById(R.id.navAdminLogIcon);
        ImageView navAdminProfileIcon = activity.findViewById(R.id.navAdminProfileIcon);

        // Click Listeners
        navAdminHomeCard.setOnClickListener(v -> {
            if (selectedIndex != 0) {
                Intent intent = new Intent(activity, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                if (selectedIndex != 0) activity.finish();
            }
        });

        navAdminCinemaCard.setOnClickListener(v -> {
            if (selectedIndex != 1) {
                Intent intent = new Intent(activity, AdminCinemaListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                if (selectedIndex != 0) activity.finish();
            }
        });

        navAdminMovieCard.setOnClickListener(v -> {
            if (selectedIndex != 2) {
                Intent intent = new Intent(activity, AdminMovieListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                if (selectedIndex != 0) activity.finish();
            }
        });

        navAdminLogCard.setOnClickListener(v -> {
            if (selectedIndex != 3) {
                Intent intent = new Intent(activity, AdminAuditLogActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                if (selectedIndex != 0) activity.finish();
            }
        });

        navAdminProfileCard.setOnClickListener(v -> {
            if (selectedIndex != 4) {
                Intent intent = new Intent(activity, StaffProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                if (selectedIndex != 0) activity.finish();
            }
        });

        // Apply visual state
        float density = activity.getResources().getDisplayMetrics().density;
        applyAdminBottomState(activity, density, navAdminHomeCard, navAdminHomeLabel, navAdminHomeIcon, selectedIndex == 0, "Tổng quan");
        applyAdminBottomState(activity, density, navAdminCinemaCard, navAdminCinemaLabel, navAdminCinemaIcon, selectedIndex == 1, "Rạp chiếu");
        applyAdminBottomState(activity, density, navAdminMovieCard, navAdminMovieLabel, navAdminMovieIcon, selectedIndex == 2, "Điện ảnh");
        applyAdminBottomState(activity, density, navAdminLogCard, navAdminLogLabel, navAdminLogIcon, selectedIndex == 3, "Nhật ký");
        applyAdminBottomState(activity, density, navAdminProfileCard, navAdminProfileLabel, navAdminProfileIcon, selectedIndex == 4, "Hồ sơ");

        bottomNavContainer.requestLayout();
    }

    private static void applyAdminBottomState(Activity activity, float density, MaterialCardView card, TextView label, ImageView icon, boolean selected, String text) {
        int dp48 = (int) (48 * density);
        int dp2 = (int) (2 * density);
        int dp1 = (int) (1 * density);

        LinearLayout.LayoutParams params;
        if (selected) {
            params = new LinearLayout.LayoutParams(0, dp48, 1.2f);
            params.setMarginStart(dp2);
            params.setMarginEnd(dp2);
            params.leftMargin = dp2;
            params.rightMargin = dp2;
            card.setCardBackgroundColor(Color.parseColor("#121212")); // Black theme color matching customer style
            card.setStrokeWidth(0);
            label.setText(text);
            label.setVisibility(View.VISIBLE);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(Color.WHITE));
            label.setTextColor(Color.WHITE);
            card.animate().scaleX(1.03f).scaleY(1.03f).setDuration(150).start();
        } else {
            params = new LinearLayout.LayoutParams(dp48, dp48);
            params.setMarginStart(dp2);
            params.setMarginEnd(dp2);
            params.leftMargin = dp2;
            params.rightMargin = dp2;
            card.setCardBackgroundColor(Color.WHITE);
            card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#D3CAD7")));
            card.setStrokeWidth(dp1);
            label.setVisibility(View.GONE);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(Color.parseColor("#4A4650")));
            card.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
        }
        card.setLayoutParams(params);
    }
}
