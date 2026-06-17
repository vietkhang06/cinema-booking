package com.example.cinemabooking.ui.customer.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.core.navigation.AppNavigator;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.service.AuthenticationService;
import com.example.cinemabooking.service.ProfileService;
import com.example.cinemabooking.ui.component.AchievementProgressBar;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment {
    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    // ── Views ─────────────────────────────────────────────────────────────────
    TextView userNameTV, totalSpendingTV, tvMemberLevel, tvStarCount;
    ImageView profileAvatar, badgeImage;
    MaterialCardView editProfileBtn, viewTransactionBtn, viewNotificationBtn;
    MaterialCardView logOutBtn;
    TextView tvLogoutBtnLabel, notificationBadge;
    LinearLayout btnMemberCard;
    ImageView btnSettings;
    AchievementProgressBar achievementBar;

    // Menu items
    LinearLayout menuHotline, menuEmail, menuCompanyInfo,
            menuTerms, menuPaymentPolicy, menuPrivacyPolicy, menuFaq;
    LinearLayout btnMyTickets, btnMyRewards;

    private com.google.firebase.firestore.ListenerRegistration notificationListener;
    private User currentUserProfile;

    // ── Services ──────────────────────────────────────────────────────────────
    AuthenticationService authService;
    ProfileService profileService;

    // ── Spending milestones ───────────────────────────────────────────────────
    int maxSpendingMilestone = 4100000;
    int totalSpending = 0;
    List<Integer> spendingMilestones = Arrays.asList(0, 2_000_000, 4_000_000);
    List<Integer> milestoneIcons = Arrays.asList(
            R.drawable.square_solid_full,
            R.drawable.pentagon_solid_full,
            R.drawable.hexagon_solid_full
    );

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authService   = ServiceProvider.getInstance().getAuthenticationService();
        profileService = ServiceProvider.getInstance().getProfileService();

        initViews(view);
        bindActions();
        setMenuText(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateAuthButton();
        loadUserProfile();
        loadUserSpendingMilestone();
        listenToNotifications();
    }

    // ZELIOUS TASK: Lắng nghe số lượng thông báo chưa đọc.
    // Kết nối với Firestore để realtime-update cái huy hiệu (badge) màu đỏ báo số "99+".
    private void listenToNotifications() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        com.example.cinemabooking.domain.repository.NotificationRepository repo = new com.example.cinemabooking.data.repository.NotificationRepositoryImpl();
        notificationListener = repo.listenToUserNotifications(user.getUid(), new ResultCallback<List<com.example.cinemabooking.domain.model.Notification>>() {
            @Override
            public void onSuccess(List<com.example.cinemabooking.domain.model.Notification> result) {
                if (!isAdded()) return;
                int unreadCount = 0;
                if (result != null) {
                    for (com.example.cinemabooking.domain.model.Notification notif : result) {
                        if (!notif.isRead) {
                            unreadCount++;
                        }
                    }
                }

                if (notificationBadge != null) {
                    if (unreadCount > 0) {
                        notificationBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
                        notificationBadge.setVisibility(View.VISIBLE);
                    } else {
                        notificationBadge.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Do nothing
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }

    // ── Load data ─────────────────────────────────────────────────────────────

    // ZELIOUS TASK: Tải thông tin Profile của User từ Firestore thông qua ProfileService.
    // Lấy Avatar định dạng Base64 và parse ngược lại thành byte[] để Glide hiển thị được hình ảnh.
    private void loadUserProfile() {
        profileService.getUserProfile(new ResultCallback<User>() {
            @Override
            public void onSuccess(User profileData) {
                if (profileData == null || !isAdded()) return;
                currentUserProfile = profileData;

                String displayName = (profileData.name == null || profileData.name.isBlank())
                        ? profileData.email : profileData.name;
                userNameTV.setText(displayName);
                
                if (tvMemberLevel != null) {
                    tvMemberLevel.setText(profileData.memberLevel != null ? profileData.memberLevel : "Basic");
                }

                if (tvStarCount != null) {
                    tvStarCount.setText(((profileData.points != null) ? profileData.points : 0) + " Stars");
                }

                if (profileData.avatarUrl != null && profileData.avatarUrl.startsWith("data:image")) {
                    String base64Content = profileData.avatarUrl.substring(profileData.avatarUrl.indexOf(",") + 1);
                    byte[] imageBytes = android.util.Base64.decode(base64Content, android.util.Base64.DEFAULT);
                    Glide.with(ProfileFragment.this)
                            .load(imageBytes)
                            .circleCrop()
                            .placeholder(R.drawable.user_solid_full)
                            .into(profileAvatar);
                } else {
                    Glide.with(ProfileFragment.this)
                            .load(profileData.avatarUrl != null ? profileData.avatarUrl : R.drawable.user_solid_full)
                            .circleCrop()
                            .placeholder(R.drawable.user_solid_full)
                            .into(profileAvatar);
                }


                loadUserSpendingMilestone();
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải thông tin: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ZELIOUS TASK: Tính tổng tiền chi tiêu để thăng hạng.
    // Gọi profileService kéo tổng tiền. Sau đó gọi updateSpendingUI() để render thanh ProgressBar.
    private void loadUserSpendingMilestone() {
        profileService.getUserTotalSpending(new ResultCallback<Double>() {
            @Override
            public void onSuccess(Double total) {
                totalSpending = total.intValue();
                updateSpendingUI();
            }

            @Override
            public void onError(String message) {
                totalSpending = 0;
                updateSpendingUI();
            }
        });
    }

    // ZELIOUS TASK: Cập nhật UI của thẻ Thành viên (Màu sắc, Huy hiệu).
    // Dựa vào tổng tiền so sánh với mốc 2 triệu, 4 triệu để quyết định là hạng Đồng, Bạc, Vàng...
    private void updateSpendingUI() {
        if (!isAdded()) return;

        List<Integer> milestoneColors = Arrays.asList(
                0xFF4A148C, // Square: Tím đậm
                0xFF800000, // Pentagon: Đỏ đô
                0xFFEAB308  // Hexagon: Vàng đậm
        );

        AtomicInteger index = new AtomicInteger(0);
        List<AchievementProgressBar.Milestone> milestones = spendingMilestones.stream()
                .map(value -> {
                    int i = index.getAndIncrement();
                    return new AchievementProgressBar.Milestone(
                            (float) value / maxSpendingMilestone,
                            String.format("%,d", value).replace(',', '.'),
                            milestoneIcons.get(i),
                            milestoneColors.get(i));
                })
                .collect(Collectors.toList());
        achievementBar.setMilestones(milestones);

        float progress = (float) totalSpending / maxSpendingMilestone;
        int badgeId = R.drawable.square_solid_full;
        int badgeColor = 0xFF4A148C;

        if (progress >= (float) spendingMilestones.get(2) / maxSpendingMilestone) {
            badgeId = milestoneIcons.get(2);
            badgeColor = 0xFFEAB308;
        } else if (progress >= (float) spendingMilestones.get(1) / maxSpendingMilestone) {
            badgeId = milestoneIcons.get(1);
            badgeColor = 0xFF800000;
        } else {
            badgeId = milestoneIcons.get(0);
            badgeColor = 0xFF4A148C;
        }

        badgeImage.setImageResource(badgeId);
        badgeImage.setImageTintList(android.content.res.ColorStateList.valueOf(badgeColor));
        achievementBar.setProgress(progress);

        totalSpendingTV.setText(String.format("%,dđ", totalSpending).replace(',', '.'));
    }

    // ── Init views ────────────────────────────────────────────────────────────

    private void initViews(@NonNull View view) {
        achievementBar      = view.findViewById(R.id.achievement_bar);
        userNameTV          = view.findViewById(R.id.profile_user_name);
        totalSpendingTV     = view.findViewById(R.id.profile_total_spending);
        profileAvatar       = view.findViewById(R.id.profile_avatar);
        badgeImage          = view.findViewById(R.id.profile_customer_badge);
        tvMemberLevel       = view.findViewById(R.id.tvMemberLevel);
        tvStarCount         = view.findViewById(R.id.tvStarCount);

        editProfileBtn      = view.findViewById(R.id.profile_edit_btn);
        viewTransactionBtn  = view.findViewById(R.id.profile_transaction_btn);
        viewNotificationBtn = view.findViewById(R.id.profile_notification_btn);
        notificationBadge   = view.findViewById(R.id.profile_notification_badge);
        logOutBtn           = view.findViewById(R.id.profile_logout_btn);
        tvLogoutBtnLabel    = view.findViewById(R.id.tvLogoutBtnLabel);
        btnMemberCard       = view.findViewById(R.id.btnMemberCard);
        btnSettings         = view.findViewById(R.id.btnProfileSettings);

        btnMyTickets        = view.findViewById(R.id.btnMyTickets);
        btnMyRewards        = view.findViewById(R.id.btnMyRewards);

        menuHotline         = view.findViewById(R.id.menuHotline);
        menuEmail           = view.findViewById(R.id.menuEmail);
        menuCompanyInfo     = view.findViewById(R.id.menuCompanyInfo);
        menuTerms           = view.findViewById(R.id.menuTerms);
        menuPaymentPolicy   = view.findViewById(R.id.menuPaymentPolicy);
        menuPrivacyPolicy   = view.findViewById(R.id.menuPrivacyPolicy);
        menuFaq             = view.findViewById(R.id.menuFaq);
    }

    // ── Set rich-text menu labels ─────────────────────────────────────────────

    private void setMenuText(@NonNull View view) {
        setHtmlText(menuHotline, 0,
                "Gọi <b>ĐƯỜNG DÂY NÓNG</b>: <font color='#E8640C'>19002224</font>");
        setHtmlText(menuEmail, 0,
                "Email:  <font color='#E8640C'>hotro@galaxystudio.vn</font>");
    }

    /** Tìm TextView đầu tiên trong LinearLayout và set HTML text */
    private void setHtmlText(LinearLayout row, int childIndex, String html) {
        // TextView là con đầu tiên (index 0) của row
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
                break;
            }
        }
    }

    // ── Bind actions ──────────────────────────────────────────────────────────

    private void bindActions() {
        // Thông tin
        editProfileBtn.setOnClickListener(v ->
                startActivity(new Intent(getContext(), EditProfileActivity.class)));

        // Cài đặt
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v ->
                    startActivity(new Intent(getContext(), SettingsActivity.class)));
        }

        // Giao dịch
        viewTransactionBtn.setOnClickListener(v ->
                AppNavigator.goToTransactionHistory(requireActivity()));

        // Thông báo
        viewNotificationBtn.setOnClickListener(v -> {
            boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
            if (isLoggedIn) {
                startActivity(new Intent(getContext(), com.example.cinemabooking.ui.customer.notification.NotificationActivity.class));
            } else {
                AppNavigator.goToLoginForBooking(requireActivity());
            }
        });

        // Mã thành viên
        if (btnMemberCard != null)
            btnMemberCard.setOnClickListener(v -> {
                boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
                if (!isLoggedIn) {
                    Toast.makeText(getContext(), "Vui lòng đăng nhập để xem mã thành viên", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currentUserProfile == null) {
                    Toast.makeText(getContext(), "Đang tải thông tin thành viên, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                    loadUserProfile();
                    return;
                }
                showMemberCardDialog(currentUserProfile);
            });

        // Feature cards
        if (btnMyTickets != null) {
            btnMyTickets.setOnClickListener(v -> {
                boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
                if (isLoggedIn) {
                    AppNavigator.goToTransactionHistory(requireActivity());
                } else {
                    AppNavigator.goToLoginForBooking(requireActivity());
                }
            });
        }
        if (btnMyRewards != null) {
            btnMyRewards.setOnClickListener(v -> {
                boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
                if (isLoggedIn) {
                    startActivity(new Intent(getContext(), MyPromotionListActivity.class));
                } else {
                    AppNavigator.goToLoginForBooking(requireActivity());
                }
            });
        }

        // Menu items
        if (menuHotline != null)
            menuHotline.setOnClickListener(v -> {
                Intent call = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:19002224"));
                startActivity(call);
            });
        if (menuEmail != null)
            menuEmail.setOnClickListener(v -> {
                Intent mail = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:hotro@galaxystudio.vn"));
                startActivity(mail);
            });
        if (menuCompanyInfo != null)
            menuCompanyInfo.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Thông tin công ty", Toast.LENGTH_SHORT).show());
        if (menuTerms != null)
            menuTerms.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Điều khoản sử dụng", Toast.LENGTH_SHORT).show());
        if (menuPaymentPolicy != null)
            menuPaymentPolicy.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Chính sách thanh toán", Toast.LENGTH_SHORT).show());
        if (menuPrivacyPolicy != null)
            menuPrivacyPolicy.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Chính sách bảo mật", Toast.LENGTH_SHORT).show());
        if (menuFaq != null)
            menuFaq.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Câu hỏi thường gặp", Toast.LENGTH_SHORT).show());

        // ── ĐĂNG NHẬP / ĐĂNG XUẤT — đổi hành vi theo trạng thái đăng nhập ──
        logOutBtn.setOnClickListener(v -> {
            boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;
            if (isLoggedIn) {
                // Đã đăng nhập → hiện dialog xác nhận đăng xuất
                new AlertDialog.Builder(requireContext())
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc muốn đăng xuất không?")
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            authService.logOut();
                            // Sau khi logout → về HomeActivity (guest mode), không clear về Login
                            AppNavigator.goToCustomerHome(requireActivity());
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            } else {
                // Chưa đăng nhập → mở LoginActivity mà không xóa back stack
                // → back hoặc login xong sẽ quay về tab Profile
                AppNavigator.goToLoginForBooking(requireActivity());
            }
        });
    }

    // ── Cập nhật nút Login/Logout theo trạng thái đăng nhập ─────────────────

    private void updateAuthButton() {
        if (!isAdded() || tvLogoutBtnLabel == null) return;

        boolean isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;

        if (isLoggedIn) {
            tvLogoutBtnLabel.setText("Đăng xuất");
            tvLogoutBtnLabel.setTextColor(android.graphics.Color.parseColor("#E8640C")); // cam
        } else {
            tvLogoutBtnLabel.setText("Đăng nhập");
            tvLogoutBtnLabel.setTextColor(android.graphics.Color.parseColor("#1E4F8F")); // xanh
        }
    }

    private void showMemberCardDialog(User user) {
        if (getContext() == null) return;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_member_card, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        com.google.android.material.card.MaterialCardView layoutCardBackground = dialogView.findViewById(R.id.layoutCardBackground);
        ImageView ivQrCode = dialogView.findViewById(R.id.ivQrCode);
        TextView tvMemberName = dialogView.findViewById(R.id.tvMemberName);
        TextView tvMemberLevelBadge = dialogView.findViewById(R.id.tvMemberLevelBadge);
        TextView tvMemberId = dialogView.findViewById(R.id.tvMemberId);
        View btnClose = dialogView.findViewById(R.id.btnClose);
        
        String name = (user.name != null && !user.name.trim().isEmpty()) ? user.name : user.email;
        if (tvMemberName != null) tvMemberName.setText(name);
        
        String level = (user.memberLevel != null) ? user.memberLevel.toLowerCase() : "standard";
        if (tvMemberLevelBadge != null) {
            tvMemberLevelBadge.setText((level.toUpperCase() + " MEMBER"));
        }
        
        if (tvMemberId != null) {
            tvMemberId.setText("ID: " + (user.uid != null ? user.uid : "—"));
        }
        
        // Customize styling based on level
        int cardColor = 0xFF1A3A8C;      // standard
        int badgeTextColor = 0xFF1A3A8C;
        int badgeBgColor = 0xFFEBF0FF;
        
        if ("vip".equals(level)) {
            cardColor = 0xFFA13345;
            badgeTextColor = 0xFFA13345;
            badgeBgColor = 0xFFFFEBEB;
        } else if ("gold".equals(level)) {
            cardColor = 0xFFB8860B;
            badgeTextColor = 0xFFB8860B;
            badgeBgColor = 0xFFFFF8E7;
        } else if ("platinum".equals(level)) {
            cardColor = 0xFF4A4A4A;
            badgeTextColor = 0xFF4A4A4A;
            badgeBgColor = 0xFFF0F0F0;
        }
        
        if (layoutCardBackground != null) {
            layoutCardBackground.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(cardColor));
        }
        if (tvMemberLevelBadge != null) {
            tvMemberLevelBadge.setTextColor(badgeTextColor);
            tvMemberLevelBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(badgeBgColor));
        }
        
        // Generate QR code URL
        String data = user.uid != null ? user.uid : "";
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=" + data;
        
        if (ivQrCode != null && getContext() != null) {
            Glide.with(this)
                .load(qrUrl)
                .placeholder(R.drawable.ic_scan_qr)
                .into(ivQrCode);
        }
        
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }
        
        dialog.show();
    }
}
