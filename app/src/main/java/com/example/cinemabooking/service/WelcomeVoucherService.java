package com.example.cinemabooking.service;

import android.util.Log;
import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.domain.model.Notification;
import com.example.cinemabooking.domain.model.NotificationType;
import com.example.cinemabooking.domain.model.Voucher;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
public class WelcomeVoucherService {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public void sendWelcomeVoucher(String userId) {
        if (userId == null || userId.isEmpty()) return;

        WriteBatch batch = db.batch();
        long currentTime = System.currentTimeMillis();

        // Tạo voucher (lưu vào bảng promotions)
        DocumentReference promoRef = db.collection("promotions").document();
        
        // Sinh ngẫu nhiên mã code 6 ký tự dạng NEW_XXXXXX
        String shortUid = userId.length() > 6 ? userId.substring(0, 6).toUpperCase() : userId.toUpperCase();
        String promoCode = "NEW_" + shortUid;

        java.util.Map<String, Object> promo = new java.util.HashMap<>();
        promo.put("code", promoCode);
        promo.put("title", "Quà Tân Binh 200K");
        promo.put("discountType", "amount");
        promo.put("discountValue", 200000.0);
        promo.put("status", "active");
        promo.put("deleted", false);
        promo.put("usageLimit", 1L);
        promo.put("usedCount", 0L);
        promo.put("targetRole", "all");
        promo.put("userId", userId); // Liên kết với tài khoản
        promo.put("createdAt", currentTime);
        promo.put("validFrom", currentTime);
        
        batch.set(promoRef, promo);

        // Tạo thông báo
        DocumentReference notifRef = db.collection(FirestoreCollections.NOTIFICATIONS).document();
        Notification notif = new Notification();
        notif.notificationId = notifRef.getId();
        notif.userId = userId;
        notif.title = "Chào mừng Khách hàng mới!";
        notif.message = "Bạn vừa nhận được Voucher 200.000đ áp dụng cho mọi giao dịch. Chúc bạn xem phim vui vẻ!";
        notif.type = NotificationType.VOUCHER_RECEIVED.name();
        notif.isRead = false;
        notif.createdAt = currentTime;
        notif.updatedAt = currentTime;
        batch.set(notifRef, notif);

        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d("WelcomeVoucher", "Đã gửi voucher thành công cho user: " + userId))
                .addOnFailureListener(e -> Log.e("WelcomeVoucher", "Lỗi gửi voucher: " + e.getMessage()));
    }
}
