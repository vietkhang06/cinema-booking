package com.example.cinemabooking.data.dto;

public class NotificationDTO {
    public String notificationId;
    public String userId;
    public String title;
    public String message;
    public String type;
    public String refId;
    public boolean isRead;
    public long createdAt;
    public long updatedAt;

    public NotificationDTO() {
    }
}