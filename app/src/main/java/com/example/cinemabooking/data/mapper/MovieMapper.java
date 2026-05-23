package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.MovieDTO;
import com.example.cinemabooking.domain.model.Movie;

public final class MovieMapper {
    private MovieMapper() {
    }

    public static Movie toDomain(MovieDTO dto) {
        if (dto == null) return null;
        Movie model = new Movie();
        model.movieId = dto.movieId;
        model.title = dto.title;
        model.description = dto.description;
        model.genres = dto.genres;
        model.language = dto.language;
        model.durationMinutes = dto.durationMinutes;
        model.releaseDate = dto.releaseDate;
        model.ageRating = dto.ageRating;
        model.posterUrl = dto.posterUrl;
        model.trailerUrl = dto.trailerUrl;
        model.ratingAvg = dto.ratingAvg;
        model.ratingCount = dto.ratingCount;
        model.status = dto.status;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        return model;
    }

    public static MovieDTO toDTO(Movie model) {
        if (model == null) return null;
        MovieDTO dto = new MovieDTO();
        dto.movieId = model.movieId;
        dto.title = model.title;
        dto.description = model.description;
        dto.genres = model.genres;
        dto.language = model.language;
        dto.durationMinutes = model.durationMinutes;
        dto.releaseDate = model.releaseDate;
        dto.ageRating = model.ageRating;
        dto.posterUrl = model.posterUrl;
        dto.trailerUrl = model.trailerUrl;
        dto.ratingAvg = model.ratingAvg;
        dto.ratingCount = model.ratingCount;
        dto.status = model.status;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        return dto;
    }
}