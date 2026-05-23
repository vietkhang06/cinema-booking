package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.UserDTO;
import com.example.cinemabooking.domain.model.User;

public final class UserMapper {
    private UserMapper() {
    }

    public static User toDomain(UserDTO dto) {
        if (dto == null) return null;
        User model = new User();
        model.uid = dto.uid;
        model.name = dto.name;
        model.email = dto.email;
        model.phone = dto.phone;
        model.avatarUrl = dto.avatarUrl;
        model.role = dto.role;
        model.status = dto.status;
        model.memberLevel = dto.memberLevel;
        model.points = dto.points;
        model.fcmToken = dto.fcmToken;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        return model;
    }

    public static UserDTO toDTO(User model) {
        if (model == null) return null;
        UserDTO dto = new UserDTO();
        dto.uid = model.uid;
        dto.name = model.name;
        dto.email = model.email;
        dto.phone = model.phone;
        dto.avatarUrl = model.avatarUrl;
        dto.role = model.role;
        dto.status = model.status;
        dto.memberLevel = model.memberLevel;
        dto.points = model.points;
        dto.fcmToken = model.fcmToken;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        return dto;
    }
}