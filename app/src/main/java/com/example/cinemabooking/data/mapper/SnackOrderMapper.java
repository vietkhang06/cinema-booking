package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.SnackOrderDTO;
import com.example.cinemabooking.domain.model.SnackOrder;

import java.util.ArrayList;
import java.util.List;

public final class SnackOrderMapper {
    private SnackOrderMapper() {
    }

    public static SnackOrder toDomain(SnackOrderDTO dto) {
        if (dto == null) return null;
        SnackOrder model = new SnackOrder();
        model.snackOrderId = dto.snackOrderId;
        model.userId = dto.userId;
        model.bookingId = dto.bookingId;
        model.subtotal = dto.subtotal;
        model.discount = dto.discount;
        model.total = dto.total;
        model.status = dto.status;
        model.note = dto.note;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        if (dto.items != null) {
            model.items = new ArrayList<>();
            dto.items.forEach(item -> model.items.add(SnackOrderItemMapper.toDomain(item)));
        }
        return model;
    }

    public static SnackOrderDTO toDTO(SnackOrder model) {
        if (model == null) return null;
        SnackOrderDTO dto = new SnackOrderDTO();
        dto.snackOrderId = model.snackOrderId;
        dto.userId = model.userId;
        dto.bookingId = model.bookingId;
        dto.subtotal = model.subtotal;
        dto.discount = model.discount;
        dto.total = model.total;
        dto.status = model.status;
        dto.note = model.note;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        if (model.items != null) {
            List<com.example.cinemabooking.data.dto.SnackOrderItemDTO> items = new ArrayList<>();
            model.items.forEach(item -> items.add(SnackOrderItemMapper.toDTO(item)));
            dto.items = items;
        }
        return dto;
    }
}