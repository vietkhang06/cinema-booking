package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.SnackCategoryDTO;
import com.example.cinemabooking.domain.model.SnackCategory;

public final class SnackCategoryMapper {
    private SnackCategoryMapper() {
    }

    public static SnackCategory toDomain(SnackCategoryDTO dto) {
        if (dto == null) return null;
        SnackCategory model = new SnackCategory();
        model.categoryId = dto.categoryId;
        model.name = dto.name;
        model.iconUrl = dto.iconUrl;
        model.status = dto.status;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        return model;
    }

    public static SnackCategoryDTO toDTO(SnackCategory model) {
        if (model == null) return null;
        SnackCategoryDTO dto = new SnackCategoryDTO();
        dto.categoryId = model.categoryId;
        dto.name = model.name;
        dto.iconUrl = model.iconUrl;
        dto.status = model.status;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        return dto;
    }
}