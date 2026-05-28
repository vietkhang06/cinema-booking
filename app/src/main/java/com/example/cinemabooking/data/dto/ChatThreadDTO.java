package com.example.cinemabooking.data.dto;

public class ChatThreadDTO {
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

    public ChatThreadDTO() {
    }
}