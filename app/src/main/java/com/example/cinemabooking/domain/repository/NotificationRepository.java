package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Notification;

import java.util.List;

public interface NotificationRepository {
    void createNotification(Notification notification, ResultCallback<Notification> callback);
    void getNotificationById(String notificationId, ResultCallback<Notification> callback);
    void getNotificationsByUserId(String userId, ResultCallback<List<Notification>> callback);
    void markAsRead(String notificationId, ResultCallback<Notification> callback);
    void markAllAsRead(String userId, ResultCallback<Void> callback);
    void deleteNotification(String notificationId, ResultCallback<Void> callback);
}