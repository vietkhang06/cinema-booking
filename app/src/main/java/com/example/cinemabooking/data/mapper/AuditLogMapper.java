package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.AuditLogDTO;
import com.example.cinemabooking.domain.model.AuditLog;

public final class AuditLogMapper {
    private AuditLogMapper() {
    }

    public static AuditLog toDomain(AuditLogDTO dto) {
        if (dto == null) return null;
        AuditLog model = new AuditLog();
        model.logId = dto.logId;
        model.actorId = dto.actorId;
        model.actorRole = dto.actorRole;
        model.action = dto.action;
        model.targetType = dto.targetType;
        model.targetId = dto.targetId;
        model.note = dto.note;
        model.createdAt = dto.createdAt;
        return model;
    }

    public static AuditLogDTO toDTO(AuditLog model) {
        if (model == null) return null;
        AuditLogDTO dto = new AuditLogDTO();
        dto.logId = model.logId;
        dto.actorId = model.actorId;
        dto.actorRole = model.actorRole;
        dto.action = model.action;
        dto.targetType = model.targetType;
        dto.targetId = model.targetId;
        dto.note = model.note;
        dto.createdAt = model.createdAt;
        return dto;
    }
}