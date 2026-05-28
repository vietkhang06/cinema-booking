package com.example.cinemabooking.utils;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class CineShopSeeder {
    private static final String TAG = "CineShopSeeder";

    public static void seedIfNeeded() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("cine_shop_banners")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "CineShop database is empty. Starting background seeding...");
                        performSeeding(db);
                    } else {
                        Log.d(TAG, "CineShop database already contains data. Seeding skipped.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking CineShop collections: " + e.getMessage()));
    }

    private static void performSeeding(FirebaseFirestore db) {
        WriteBatch batch = db.batch();
        long now = System.currentTimeMillis();

        // 1. Seed Banners
        addBanner(batch, db, "banner_cs_01", "Mùa Capybara Thả Ga Bắp Nước", "https://www.galaxycine.vn/media/2024/3/6/z5217435133604-0ee030cd3eb04ea12ed2539d09c6f932_1709712711718.jpg", "promo", "galaxy50", 1, now);
        addBanner(batch, db, "banner_cs_02", "Combo Yummy Tiện Lợi", "https://www.galaxycine.vn/media/2024/5/1/combo-g-1_1714552467319.jpg", "combo", "MV02", 2, now);
        addBanner(batch, db, "banner_cs_03", "Ưu Đãi Phim Bom Tấn", "https://www.galaxycine.vn/media/2024/5/1/combo-g-2_1714552469493.jpg", "movie", "MV03", 3, now);

        // 2. Seed Categories
        addCategory(batch, db, "CAT_SEASONAL", "Sản phẩm sự kiện (Seasonal)", "https://img.icons8.com/color/96/popcorn.png", 1);
        addCategory(batch, db, "CAT_MOVIE", "Combo rạp phim (Movie)", "https://img.icons8.com/color/96/soda-bottle.png", 2);

        // 3. Seed Items
        addItem(batch, db, "SE01", "Ly nước Capybara", "Ly nước Capybara phiên bản giới hạn cực kỳ đáng yêu, kèm theo ống hút và sticker trang trí.", "https://static.kinhtedothi.vn/w960/images/upload-images/2023/12/12/capybara.jpg", 350000.0, "CAT_SEASONAL", 100, "available", 1);
        addItem(batch, db, "SE02", "Combo Yummy Capybara", "Combo gồm 1 ly Capybara Premium + 1 bắp ngọt lớn + 1 pepsi lớn mát lạnh.", "https://www.galaxycine.vn/media/2024/3/6/z5217435133604-0ee030cd3eb04ea12ed2539d09c6f932_1709712711718.jpg", 500000.0, "CAT_SEASONAL", 50, "available", 2);
        addItem(batch, db, "SE03", "Set quà tặng Galaxy", "Hộp quà lưu niệm độc quyền chứa 1 sổ tay cine, 1 móc khóa Capybara và voucher giảm giá 50k.", "https://cdn.galaxycine.vn/media/2024/5/1/combo-g-1_1714552467319.jpg", 250000.0, "CAT_SEASONAL", 200, "available", 3);

        addItem(batch, db, "MV01", "Combo Bắp + Nước (1 người)", "Combo truyền thống tiết kiệm gồm 1 bắp lớn (ngọt/phô mai) và 1 ly nước ngọt Pepsi cỡ vừa.", "https://galaxycine.vn/media/2023/10/24/combo-couple.jpg", 85000.0, "CAT_MOVIE", 999, "available", 4);
        addItem(batch, db, "MV02", "Combo Couple (2 Bắp + 2 Nước)", "Combo lãng mạn dành cho cặp đôi gồm 2 bắp lớn tùy chọn vị ngọt/mặn và 2 ly nước ngọt Pepsi mát lạnh.", "https://galaxycine.vn/media/2023/10/24/combo-couple.jpg", 145000.0, "CAT_MOVIE", 999, "available", 5);
        addItem(batch, db, "MV03", "Pepsi Lớn", "Một ly nước ngọt Pepsi dung tích lớn cực đã khát, sảng khoái suốt buổi xem phim.", "https://www.galaxycine.vn/media/2024/5/1/combo-g-2_1714552469493.jpg", 35000.0, "CAT_MOVIE", 999, "available", 6);

        batch.commit()
                .addOnSuccessListener(unused -> Log.d(TAG, "CineShop sample data seeded successfully."))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to commit CineShop seed data: " + e.getMessage()));
    }

    private static void addBanner(WriteBatch batch, FirebaseFirestore db, String id, String title, String imageUrl, String targetType, String targetId, int sortOrder, long now) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("bannerId", id);
        doc.put("title", title);
        doc.put("imageUrl", imageUrl);
        doc.put("targetType", targetType);
        doc.put("targetId", targetId);
        doc.put("sortOrder", sortOrder);
        doc.put("isActive", true);
        doc.put("createdAt", now);
        doc.put("updatedAt", now);

        batch.set(db.collection("cine_shop_banners").document(id), doc);
    }

    private static void addCategory(WriteBatch batch, FirebaseFirestore db, String id, String name, String iconUrl, int sortOrder) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("categoryId", id);
        doc.put("name", name);
        doc.put("iconUrl", iconUrl);
        doc.put("sortOrder", sortOrder);
        doc.put("isActive", true);

        batch.set(db.collection("cine_shop_categories").document(id), doc);
    }

    private static void addItem(WriteBatch batch, FirebaseFirestore db, String id, String name, String description, String imageUrl, double price, String categoryId, int stock, String status, int sortOrder) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("itemId", id);
        doc.put("name", name);
        doc.put("description", description);
        doc.put("imageUrl", imageUrl);
        doc.put("price", price);
        doc.put("categoryId", categoryId);
        doc.put("stock", stock);
        doc.put("status", status);
        doc.put("sortOrder", sortOrder);
        doc.put("isActive", true);

        batch.set(db.collection("cine_shop_items").document(id), doc);
    }
}
