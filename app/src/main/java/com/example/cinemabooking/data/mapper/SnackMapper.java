package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.SnackDTO;
import com.example.cinemabooking.domain.model.Snack;

public final class SnackMapper {
    private SnackMapper() {
    }

    public static Snack toDomain(SnackDTO dto) {
        if (dto == null) return null;
        Snack model = new Snack();
        model.snackId = dto.snackId;
        model.categoryId = dto.categoryId;
        model.name = dto.name;
        model.description = dto.description;
        model.price = dto.price;
        model.imageUrl = dto.imageUrl;
        model.isAvailable = dto.isAvailable;
        model.status = dto.status;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        return model;
    }

    public static SnackDTO toDTO(Snack model) {
        if (model == null) return null;
        SnackDTO dto = new SnackDTO();
        dto.snackId = model.snackId;
        dto.categoryId = model.categoryId;
        dto.name = model.name;
        dto.description = model.description;
        dto.price = model.price;
        dto.imageUrl = model.imageUrl;
        dto.isAvailable = model.isAvailable;
        dto.status = model.status;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        return dto;
    }
}