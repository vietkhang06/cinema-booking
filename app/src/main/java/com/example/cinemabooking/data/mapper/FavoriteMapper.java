package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.FavoriteDTO;
import com.example.cinemabooking.domain.model.Favorite;

public final class FavoriteMapper {
    private FavoriteMapper() {
    }

    public static Favorite toDomain(FavoriteDTO dto) {
        if (dto == null) return null;
        Favorite model = new Favorite();
        model.favoriteId = dto.favoriteId;
        model.userId = dto.userId;
        model.movieId = dto.movieId;
        model.createdAt = dto.createdAt;
        return model;
    }

    public static FavoriteDTO toDTO(Favorite model) {
        if (model == null) return null;
        FavoriteDTO dto = new FavoriteDTO();
        dto.favoriteId = model.favoriteId;
        dto.userId = model.userId;
        dto.movieId = model.movieId;
        dto.createdAt = model.createdAt;
        return dto;
    }
}