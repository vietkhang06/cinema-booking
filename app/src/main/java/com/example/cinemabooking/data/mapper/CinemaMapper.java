package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.CinemaDTO;
import com.example.cinemabooking.domain.model.Cinema;

public final class CinemaMapper {
    private CinemaMapper() {
    }

    public static Cinema toDomain(CinemaDTO dto) {
        if (dto == null) return null;
        Cinema model = new Cinema();
        model.cinemaId = dto.cinemaId;
        model.name = dto.name;
        model.address = dto.address;
        model.city = dto.city;
        model.district = dto.district;
        model.phone = dto.phone;
        model.latitude = dto.latitude;
        model.longitude = dto.longitude;
        model.status = dto.status;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        return model;
    }

    public static CinemaDTO toDTO(Cinema model) {
        if (model == null) return null;
        CinemaDTO dto = new CinemaDTO();
        dto.cinemaId = model.cinemaId;
        dto.name = model.name;
        dto.address = model.address;
        dto.city = model.city;
        dto.district = model.district;
        dto.phone = model.phone;
        dto.latitude = model.latitude;
        dto.longitude = model.longitude;
        dto.status = model.status;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        return dto;
    }
}