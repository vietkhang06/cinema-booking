package com.example.cinemabooking.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.ui.admin.model.AdminPayment;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminPaymentAdapter extends RecyclerView.Adapter<AdminPaymentAdapter.PaymentViewHolder> {

    public interface OnPaymentClickListener {
        void onPaymentClick(AdminPayment payment);
    }

    private final List<AdminPayment> payments = new ArrayList<>();
    private final OnPaymentClickListener listener;
    private final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public AdminPaymentAdapter(OnPaymentClickListener listener) {
        this.listener = listener;
    }

    public void setPayments(List<AdminPayment> newPayments) {
        this.payments.clear();
        if (newPayments != null) {
            this.payments.addAll(newPayments);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        holder.bind(payments.get(position));
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    class PaymentViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardProviderIcon;
        private final ImageView imgProviderIcon;
        private final TextView tvPaymentCode;
        private final TextView tvPaymentMethod;
        private final TextView tvPaymentDate;
        private final TextView tvPaymentAmount;
        private final MaterialCardView cardStatusBadge;
        private final TextView tvStatusBadge;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProviderIcon = itemView.findViewById(R.id.cardProviderIcon);
            imgProviderIcon = itemView.findViewById(R.id.imgProviderIcon);
            tvPaymentCode = itemView.findViewById(R.id.tvPaymentCode);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvPaymentDate = itemView.findViewById(R.id.tvPaymentDate);
            tvPaymentAmount = itemView.findViewById(R.id.tvPaymentAmount);
            cardStatusBadge = itemView.findViewById(R.id.cardStatusBadge);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPaymentClick(payments.get(pos));
                }
            });
        }

        public void bind(AdminPayment payment) {
            tvPaymentCode.setText(payment.paymentCode != null ? payment.paymentCode : "—");
            tvPaymentAmount.setText(currencyFormatter.format(payment.amount) + " đ");
            tvPaymentDate.setText(dateFormatter.format(new Date(payment.createdAt)));

            // Style based on provider
            if ("momo".equalsIgnoreCase(payment.provider)) {
                tvPaymentMethod.setText("Ví điện tử MoMo");
                cardProviderIcon.setCardBackgroundColor(0xFFFFF0F2); // Pink light
                imgProviderIcon.setColorFilter(0xFFC2185B); // Pink dark
            } else {
                tvPaymentMethod.setText("Chuyển khoản Ngân hàng");
                cardProviderIcon.setCardBackgroundColor(0xFFE3F2FD); // Blue light
                imgProviderIcon.setColorFilter(0xFF1565C0); // Blue dark
            }

            // Style based on status
            String status = payment.status != null ? payment.status.toUpperCase() : "PENDING";
            if ("SUCCESS".equals(status) || "PAID".equals(status)) {
                tvStatusBadge.setText("Thành công");
                tvStatusBadge.setTextColor(0xFF2E7D32);
                cardStatusBadge.setCardBackgroundColor(0xFFE8F5E9);
            } else if ("FAILED".equals(status) || "CANCELLED".equals(status)) {
                tvStatusBadge.setText("Đã huỷ");
                tvStatusBadge.setTextColor(0xFFC62828);
                cardStatusBadge.setCardBackgroundColor(0xFFFFEBEE);
            } else {
                // PENDING or WAITING_CONFIRMATION
                tvStatusBadge.setText("Chờ duyệt");
                tvStatusBadge.setTextColor(0xFFE65100);
                cardStatusBadge.setCardBackgroundColor(0xFFFFF3E0);
            }
        }
    }
}
