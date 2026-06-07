package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.ChatMessageDTO;
import com.example.cinemabooking.domain.model.ChatMessage;

public final class ChatMessageMapper {
    private ChatMessageMapper() {
    }

    public static ChatMessage toDomain(ChatMessageDTO dto) {
        if (dto == null) return null;
        ChatMessage model = new ChatMessage();
        model.messageId = dto.messageId;
        model.convoId = dto.threadId;
        model.senderId = dto.senderId;
        model.receiverId = dto.receiverId;
        model.content = dto.content;
        model.type = dto.type;
        model.imgUrl = dto.imageUrl;
        model.sentAt = dto.sentAt;
        return model;
    }

    public static ChatMessageDTO toDTO(ChatMessage model) {
        if (model == null) return null;
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.messageId = model.messageId;
        dto.threadId = model.convoId;
        dto.senderId = model.senderId;
        dto.receiverId = model.receiverId;
        dto.content = model.content;
        dto.type = model.type;
        dto.imageUrl = model.imgUrl;
        dto.sentAt = model.sentAt;
        return dto;
    }
}