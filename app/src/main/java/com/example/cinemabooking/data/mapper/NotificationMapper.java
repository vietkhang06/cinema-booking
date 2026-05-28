package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.NotificationDTO;
import com.example.cinemabooking.domain.model.Notification;

public final class NotificationMapper {
    private NotificationMapper() {
    }

    public static Notification toDomain(NotificationDTO dto) {
        if (dto == null) return null;
        Notification model = new Notification();
        model.notificationId = dto.notificationId;
        model.userId = dto.userId;
        model.title = dto.title;
        model.body = dto.body;
        model.type = dto.type;
        model.refId = dto.refId;
        model.isRead = dto.isRead;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        return model;
    }

    public static NotificationDTO toDTO(Notification model) {
        if (model == null) return null;
        NotificationDTO dto = new NotificationDTO();
        dto.notificationId = model.notificationId;
        dto.userId = model.userId;
        dto.title = model.title;
        dto.body = model.body;
        dto.type = model.type;
        dto.refId = model.refId;
        dto.isRead = model.isRead;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        return dto;
    }
}