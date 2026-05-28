package com.example.cinemabooking.ui.customer.cine_shop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.cinemabooking.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Adapter cho màn hình Giỏ hàng (CineCartActivity).
 * Hỗ trợ: tăng/giảm số lượng, xoá item, callback để cập nhật tổng tiền.
 */
public class CineCartAdapter extends RecyclerView.Adapter<CineCartAdapter.CartVH> {

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    private final List<CineCartManager.CartItem> items;
    private OnCartChangeListener listener;

    public CineCartAdapter(List<CineCartManager.CartItem> items) {
        this.items = items;
    }

    public void setOnCartChangeListener(OnCartChangeListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public CartVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cine_cart, parent, false);
        return new CartVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartVH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CartVH extends RecyclerView.ViewHolder {
        ImageView imgItem, btnDelete;
        TextView tvName, tvPrice, tvQty, btnMinus, btnPlus;

        CartVH(@NonNull View v) {
            super(v);
            imgItem   = v.findViewById(R.id.imgCartItem);
            tvName    = v.findViewById(R.id.tvCartItemName);
            tvPrice   = v.findViewById(R.id.tvCartItemPrice);
            tvQty     = v.findViewById(R.id.tvCartQty);
            btnMinus  = v.findViewById(R.id.btnCartMinus);
            btnPlus   = v.findViewById(R.id.btnCartPlus);
            btnDelete = v.findViewById(R.id.btnCartDelete);
        }

        void bind(CineCartManager.CartItem item) {
            DecimalFormat fmt = new DecimalFormat("#,###");

            tvName.setText(item.quantity + "x " + item.snack.name);
            tvPrice.setText(fmt.format(item.snack.price) + "đ");
            tvQty.setText(String.valueOf(item.quantity));

            if (imgItem != null) {
                Glide.with(itemView.getContext())
                        .load(item.snack.imageUrl)
                        .placeholder(R.drawable.bg_banner_placeholder)
                        .error(R.drawable.bg_banner_placeholder)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imgItem);
            }

            btnMinus.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                CineCartManager.CartItem ci = items.get(pos);
                if (ci.quantity > 1) {
                    ci.quantity--;
                    tvName.setText(ci.quantity + "x " + ci.snack.name);
                    tvQty.setText(String.valueOf(ci.quantity));
                    if (listener != null) listener.onCartChanged();
                } else {
                    CineCartManager.getInstance().removeItem(ci.snack.snackId);
                    items.remove(pos);
                    notifyItemRemoved(pos);
                    if (listener != null) listener.onCartChanged();
                }
            });

            btnPlus.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                CineCartManager.CartItem ci = items.get(pos);
                ci.quantity++;
                tvName.setText(ci.quantity + "x " + ci.snack.name);
                tvQty.setText(String.valueOf(ci.quantity));
                if (listener != null) listener.onCartChanged();
            });

            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                CineCartManager.getInstance().removeItem(items.get(pos).snack.snackId);
                items.remove(pos);
                notifyItemRemoved(pos);
                if (listener != null) listener.onCartChanged();
            });
        }
    }
}
