package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.AppSettingDTO;
import com.example.cinemabooking.domain.model.AppSetting;

public final class AppSettingMapper {
    private AppSettingMapper() {
    }

    public static AppSetting toDomain(AppSettingDTO dto) {
        if (dto == null) return null;
        AppSetting model = new AppSetting();
        model.settingId = dto.settingId;
        model.key = dto.key;
        model.value = dto.value;
        model.updatedAt = dto.updatedAt;
        model.updatedBy = dto.updatedBy;
        return model;
    }

    public static AppSettingDTO toDTO(AppSetting model) {
        if (model == null) return null;
        AppSettingDTO dto = new AppSettingDTO();
        dto.settingId = model.settingId;
        dto.key = model.key;
        dto.value = model.value;
        dto.updatedAt = model.updatedAt;
        dto.updatedBy = model.updatedBy;
        return dto;
    }
}