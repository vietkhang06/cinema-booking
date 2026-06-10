package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Notification;
import com.example.cinemabooking.domain.repository.NotificationRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepositoryImpl implements NotificationRepository {

    private final FirebaseFirestore firestore;

    public NotificationRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void createNotification(Notification notification, ResultCallback<Notification> callback) {
        sendNotification(notification, callback);
    }

    @Override
    public void sendNotification(Notification notification, ResultCallback<Notification> callback) {
        DocumentReference ref = firestore.collection(FirestoreCollections.NOTIFICATIONS).document();
        notification.notificationId = ref.getId();
        if (notification.createdAt == 0) {
            notification.createdAt = System.currentTimeMillis();
        }
        notification.updatedAt = System.currentTimeMillis();
        
        ref.set(notification)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(notification);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getNotificationById(String notificationId, ResultCallback<Notification> callback) {
        firestore.collection(FirestoreCollections.NOTIFICATIONS)
                .document(notificationId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Notification notif = doc.toObject(Notification.class);
                        if (notif != null) {
                            notif.notificationId = doc.getId();
                            if (callback != null) callback.onSuccess(notif);
                        }
                    } else {
                        if (callback != null) callback.onError("Không tìm thấy thông báo.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getNotificationsByUserId(String userId, ResultCallback<List<Notification>> callback) {
        firestore.collection(FirestoreCollections.NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.notificationId = doc.getId();
                            notifications.add(notification);
                        }
                    }
                    if (callback != null) callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public ListenerRegistration listenToUserNotifications(String userId, ResultCallback<List<Notification>> callback) {
        return firestore.collection(FirestoreCollections.NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (callback != null) callback.onError(error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Notification notification = doc.toObject(Notification.class);
                            if (notification != null) {
                                notification.notificationId = doc.getId();
                                notifications.add(notification);
                            }
                        }
                        if (callback != null) callback.onSuccess(notifications);
                    }
                });
    }

    @Override
    public void markAsRead(String notificationId, ResultCallback<Notification> callback) {
        DocumentReference ref = firestore.collection(FirestoreCollections.NOTIFICATIONS).document(notificationId);
        ref.update("isRead", true, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        Notification dummy = new Notification();
                        dummy.notificationId = notificationId;
                        dummy.isRead = true;
                        callback.onSuccess(dummy);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void markAllAsRead(String userId, ResultCallback<Void> callback) {
        // Tính năng mở rộng nếu có nút "Đọc tất cả"
    }

    @Override
    public void deleteNotification(String notificationId, ResultCallback<Void> callback) {
        firestore.collection(FirestoreCollections.NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
}
