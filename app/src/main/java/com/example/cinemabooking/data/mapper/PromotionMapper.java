package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.PromotionDTO;
import com.example.cinemabooking.domain.model.Promotion;

public final class PromotionMapper {
    private PromotionMapper() {
    }

    public static Promotion toDomain(PromotionDTO dto) {
        if (dto == null) return null;
        Promotion model = new Promotion();
        model.promoId = dto.promoId;
        model.title = dto.title;
        model.code = dto.code;
        model.description = dto.description;
        model.discountType = dto.discountType;
        model.discountValue = dto.discountValue;
        model.minAmount = dto.minAmount;
        model.maxDiscountAmount = dto.maxDiscountAmount;
        model.validFrom = dto.validFrom;
        model.validTo = dto.validTo;
        model.status = dto.status;
        model.usageLimit = dto.usageLimit;
        model.usedCount = dto.usedCount;
        model.targetRole = dto.targetRole;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        return model;
    }

    public static PromotionDTO toDTO(Promotion model) {
        if (model == null) return null;
        PromotionDTO dto = new PromotionDTO();
        dto.promoId = model.promoId;
        dto.title = model.title;
        dto.code = model.code;
        dto.description = model.description;
        dto.discountType = model.discountType;
        dto.discountValue = model.discountValue;
        dto.minAmount = model.minAmount;
        dto.maxDiscountAmount = model.maxDiscountAmount;
        dto.validFrom = model.validFrom;
        dto.validTo = model.validTo;
        dto.status = model.status;
        dto.usageLimit = model.usageLimit;
        dto.usedCount = model.usedCount;
        dto.targetRole = model.targetRole;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        return dto;
    }
}