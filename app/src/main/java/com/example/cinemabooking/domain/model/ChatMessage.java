package com.example.cinemabooking.domain.model;

public class ChatMessage {
    public String messageId;
    public String threadId;
    public String senderId;
    public String receiverId;
    public String content;
    public String type;
    public String imageUrl;
    public boolean isRead;
    public long sentAt;

    public ChatMessage() {
    }
}