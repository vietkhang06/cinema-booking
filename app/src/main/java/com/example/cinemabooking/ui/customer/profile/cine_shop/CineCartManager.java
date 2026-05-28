package com.example.cinemabooking.ui.customer.profile.cine_shop;

import com.example.cinemabooking.domain.model.Snack;

import java.util.ArrayList;
import java.util.List;

/**
 * CineCartManager — Singleton giữ trạng thái giỏ hàng của Cine Shop.
 * Dùng chung giữa CineShopFragment, CineCartActivity và CineCheckoutActivity.
 */
public class CineCartManager {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static CineCartManager instance;

    public static CineCartManager getInstance() {
        if (instance == null) instance = new CineCartManager();
        return instance;
    }

    private CineCartManager() {}

    // ── Data model ────────────────────────────────────────────────────────────
    public static class CartItem {
        public Snack snack;
        public int quantity;

        public CartItem(Snack snack, int quantity) {
            this.snack    = snack;
            this.quantity = quantity;
        }

        public double subtotal() {
            return snack.price * quantity;
        }
    }

    private final List<CartItem> items = new ArrayList<>();

    // ── Public API ────────────────────────────────────────────────────────────

    /** Thêm hoặc cộng dồn số lượng vào giỏ. */
    public void addItem(Snack snack, int quantity) {
        for (CartItem item : items) {
            if (item.snack.snackId.equals(snack.snackId)) {
                item.quantity += quantity;
                return;
            }
        }
        items.add(new CartItem(snack, quantity));
    }

    /** Cập nhật số lượng tuyệt đối. Nếu qty <= 0 → xoá item. */
    public void updateQuantity(String snackId, int quantity) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).snack.snackId.equals(snackId)) {
                if (quantity <= 0) items.remove(i);
                else items.get(i).quantity = quantity;
                return;
            }
        }
    }

    public void removeItem(String snackId) {
        items.removeIf(it -> it.snack.snackId.equals(snackId));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalCount() {
        int total = 0;
        for (CartItem it : items) total += it.quantity;
        return total;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem it : items) total += it.subtotal();
        return total;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}
