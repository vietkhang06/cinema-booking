package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.ReviewDTO;
import com.example.cinemabooking.domain.model.Review;

public final class ReviewMapper {
    private ReviewMapper() {
    }

    public static Review toDomain(ReviewDTO dto) {
        if (dto == null) return null;
        Review model = new Review();
        model.reviewId = dto.reviewId;
        model.userId = dto.userId;
        model.movieId = dto.movieId;
        model.bookingId = dto.bookingId;
        model.movieTitleSnapshot = dto.movieTitleSnapshot;
        model.rating = dto.rating;
        model.content = dto.content;
        model.status = dto.status;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        return model;
    }

    public static ReviewDTO toDTO(Review model) {
        if (model == null) return null;
        ReviewDTO dto = new ReviewDTO();
        dto.reviewId = model.reviewId;
        dto.userId = model.userId;
        dto.movieId = model.movieId;
        dto.bookingId = model.bookingId;
        dto.movieTitleSnapshot = model.movieTitleSnapshot;
        dto.rating = model.rating;
        dto.content = model.content;
        dto.status = model.status;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        return dto;
    }
}