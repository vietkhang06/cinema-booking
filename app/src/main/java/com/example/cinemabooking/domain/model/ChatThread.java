package com.example.cinemabooking.domain.model;

public class ChatThread {
    public String chatId;
    public String userId;
    public String staffId;
    public String lastMessage;
    public String lastSenderId;
    public int unreadCountUser;
    public int unreadCountStaff;
    public String status;
    public long createdAt;
    public long updatedAt;

    public ChatThread() {
    }
}