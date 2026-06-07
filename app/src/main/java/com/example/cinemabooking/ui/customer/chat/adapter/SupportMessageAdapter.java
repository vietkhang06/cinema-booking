package com.example.cinemabooking.ui.customer.chat.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.domain.model.ChatMessage;
import com.example.cinemabooking.utils.DateTimeConverter;
import com.google.android.material.card.MaterialCardView;

public class SupportMessageAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_SYSTEM = 4;

    private final String authUserId;

    public SupportMessageAdapter(String authUserId) {
        super(DIFF_CALLBACK);
        this.authUserId = authUserId;
    }

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ChatMessage>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                    return oldItem.messageId != null && oldItem.messageId.equals(newItem.messageId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                    return oldItem.content != null && oldItem.content.equals(newItem.content)
                            && oldItem.sentAt == newItem.sentAt;
                }
            };

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = getItem(position);
        if (message.senderId != null && message.senderId.equals(authUserId)) {
            return VIEW_TYPE_SENT;
        } else if ("SUPPORT_BOT".equals(message.senderId)) {
            return VIEW_TYPE_BOT;
        } else {
            return VIEW_TYPE_SYSTEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_support_received, parent, false);
            return new ReceivedSupportViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = getItem(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedSupportViewHolder) {
            int viewType = getItemViewType(position);
            ((ReceivedSupportViewHolder) holder).bind(message, viewType);
        }
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessageText;
        private final TextView tvTimestamp;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }

        public void bind(ChatMessage message) {
            tvMessageText.setText(message.content);
            tvTimestamp.setText(DateTimeConverter.convertToDateTimeString(message.sentAt));
        }
    }

    public static class ReceivedSupportViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSenderName;
        private final TextView tvMessageText;
        private final TextView tvTimestamp;
        private final ImageView ivSenderAvatar;
        private final MaterialCardView cvMessageCard;

        public ReceivedSupportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivSenderAvatar = itemView.findViewById(R.id.ivSenderAvatar);
            cvMessageCard = itemView.findViewById(R.id.cvMessageCard);
        }

        public void bind(ChatMessage message, int viewType) {
            tvMessageText.setText(message.content);
            tvTimestamp.setText(DateTimeConverter.convertToDateTimeString(message.sentAt));

            if (viewType == VIEW_TYPE_BOT) {
                tvSenderName.setText("Trợ lý ảo (Bot)");
                cvMessageCard.setCardBackgroundColor(Color.parseColor("#E8EAF6")); // Light indigo
                tvMessageText.setTextColor(Color.parseColor("#111111"));
                ivSenderAvatar.setImageResource(R.drawable.user_solid_full); 
                ivSenderAvatar.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3F51B5"))); // Indigo tinted
            } else {
                tvSenderName.setText("Hệ thống");
                cvMessageCard.setCardBackgroundColor(Color.parseColor("#FFF3E0")); // Light orange
                tvMessageText.setTextColor(Color.parseColor("#E65100"));
                ivSenderAvatar.setImageResource(R.drawable.circle_info_solid_full); 
                ivSenderAvatar.setImageTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E65100")));
            }
        }
    }
}
