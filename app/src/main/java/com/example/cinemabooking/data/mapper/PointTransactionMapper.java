package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.PointTransactionDTO;
import com.example.cinemabooking.domain.model.PointTransaction;

public final class PointTransactionMapper {
    private PointTransactionMapper() {
    }

    public static PointTransaction toDomain(PointTransactionDTO dto) {
        if (dto == null) return null;
        PointTransaction model = new PointTransaction();
        model.transactionId = dto.transactionId;
        model.userId = dto.userId;
        model.type = dto.type;
        model.points = dto.points;
        model.reason = dto.reason;
        model.refId = dto.refId;
        model.createdAt = dto.createdAt;
        return model;
    }

    public static PointTransactionDTO toDTO(PointTransaction model) {
        if (model == null) return null;
        PointTransactionDTO dto = new PointTransactionDTO();
        dto.transactionId = model.transactionId;
        dto.userId = model.userId;
        dto.type = model.type;
        dto.points = model.points;
        dto.reason = model.reason;
        dto.refId = model.refId;
        dto.createdAt = model.createdAt;
        return dto;
    }
}