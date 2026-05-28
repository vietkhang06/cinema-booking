package com.example.cinemabooking.data.dto;

public class ChatMessageDTO {
    public String messageId;
    public String threadId;
    public String senderId;
    public String receiverId;
    public String content;
    public String type;
    public String imageUrl;
    public boolean isRead;
    public long sentAt;

    public ChatMessageDTO() {
    }
}