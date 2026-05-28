package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.ChatThreadDTO;
import com.example.cinemabooking.domain.model.ChatThread;

public final class ChatThreadMapper {
    private ChatThreadMapper() {
    }

    public static ChatThread toDomain(ChatThreadDTO dto) {
        if (dto == null) return null;
        ChatThread model = new ChatThread();
        model.chatId = dto.chatId;
        model.userId = dto.userId;
        model.staffId = dto.staffId;
        model.lastMessage = dto.lastMessage;
        model.lastSenderId = dto.lastSenderId;
        model.unreadCountUser = dto.unreadCountUser;
        model.unreadCountStaff = dto.unreadCountStaff;
        model.status = dto.status;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        return model;
    }

    public static ChatThreadDTO toDTO(ChatThread model) {
        if (model == null) return null;
        ChatThreadDTO dto = new ChatThreadDTO();
        dto.chatId = model.chatId;
        dto.userId = model.userId;
        dto.staffId = model.staffId;
        dto.lastMessage = model.lastMessage;
        dto.lastSenderId = model.lastSenderId;
        dto.unreadCountUser = model.unreadCountUser;
        dto.unreadCountStaff = model.unreadCountStaff;
        dto.status = model.status;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        return dto;
    }
}