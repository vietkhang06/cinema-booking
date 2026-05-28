package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.SnackOrderItemDTO;
import com.example.cinemabooking.domain.model.SnackOrderItem;

public final class SnackOrderItemMapper {
    private SnackOrderItemMapper() {
    }

    public static SnackOrderItem toDomain(SnackOrderItemDTO dto) {
        if (dto == null) return null;
        SnackOrderItem model = new SnackOrderItem();
        model.snackId = dto.snackId;
//        model.snackName = dto.snackName;
        model.quantity = dto.quantity;
        model.unitPrice = dto.unitPrice;
        return model;
    }

    public static SnackOrderItemDTO toDTO(SnackOrderItem model) {
        if (model == null) return null;
        SnackOrderItemDTO dto = new SnackOrderItemDTO();
        dto.snackId = model.snackId;
//        dto.snackName = model.snackName;
        dto.quantity = model.quantity;
        dto.unitPrice = model.unitPrice;
        return dto;
    }
}