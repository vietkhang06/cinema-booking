package com.example.cinemabooking.ui.customer.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabooking.R;
import com.example.cinemabooking.domain.model.Notification;
import com.example.cinemabooking.utils.DateTimeConverter;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    public interface OnItemClickListener {
        void onClick(Notification notification);
    }

    private List<Notification> notifications;
    private OnItemClickListener listener;

    public NotificationAdapter(List<Notification> notifications, OnItemClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindViewData(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTV, contentTV, dateTV;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.notification_img);
            titleTV = itemView.findViewById(R.id.notification_title);
            contentTV = itemView.findViewById(R.id.notification_content);
            dateTV = itemView.findViewById(R.id.notification_date);
        }

        public void bindViewData(Notification notification){
            titleTV.setText(notification.title);
            contentTV.setText(notification.message);
            dateTV.setText(DateTimeConverter.convertToDateTimeString(notification.createdAt));
            
            if ("Hủy suất chiếu".equals(notification.title) || "SHOWTIME_CANCELLED".equals(notification.type)) {
                 titleTV.setTextColor(android.graphics.Color.RED);
                 imageView.setImageResource(R.drawable.ic_notification);
            } else {
                 titleTV.setTextColor(android.graphics.Color.BLACK);
                 imageView.setImageResource(R.drawable.login_icon); // Fallback icon
            }

            if (!notification.isRead) {
                itemView.setBackgroundColor(android.graphics.Color.parseColor("#E1F5FE")); // Xanh nhạt nổi bật
                titleTV.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                itemView.setBackgroundColor(android.graphics.Color.WHITE);
                titleTV.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(notification);
            });
        }
    }
}
