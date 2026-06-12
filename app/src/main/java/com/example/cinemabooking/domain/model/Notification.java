package com.example.cinemabooking.domain.model;

public class Notification {
    public String notificationId;
    public String userId;
    public String title;
    public String message;
    public String type;
    public String refId;
    public Boolean isRead;
    public Long createdAt;
    public Long updatedAt;

    public Notification() {
    }
}