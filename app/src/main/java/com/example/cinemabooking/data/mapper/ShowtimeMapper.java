package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.ShowtimeDTO;
import com.example.cinemabooking.domain.model.Showtime;

public final class ShowtimeMapper {
    private ShowtimeMapper() {
    }

    public static Showtime toDomain(ShowtimeDTO dto) {
        if (dto == null) return null;
        Showtime model = new Showtime();
        model.showtimeId = dto.showtimeId;
        model.movieId = dto.movieId;
        model.cinemaId = dto.cinemaId;
        model.roomId = dto.roomId;
        model.startAt = dto.startAt;
        model.endAt = dto.endAt;
        model.basePrice = dto.basePrice;
        model.format = dto.format;
        model.language = dto.language;
        model.status = dto.status;
        model.totalSeats = dto.totalSeats;
        model.bookedSeatsCount = dto.bookedSeatsCount;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        model.isScheduled = dto.isScheduled;
        model.executed = dto.executed;
        model.executedAt = dto.executedAt;
        return model;
    }

    public static ShowtimeDTO toDTO(Showtime model) {
        if (model == null) return null;
        ShowtimeDTO dto = new ShowtimeDTO();
        dto.showtimeId = model.showtimeId;
        dto.movieId = model.movieId;
        dto.cinemaId = model.cinemaId;
        dto.roomId = model.roomId;
        dto.startAt = model.startAt;
        dto.endAt = model.endAt;
        dto.basePrice = model.basePrice;
        dto.format = model.format;
        dto.language = model.language;
        dto.status = model.status;
        dto.totalSeats = model.totalSeats;
        dto.bookedSeatsCount = model.bookedSeatsCount;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        dto.isScheduled = model.isScheduled;
        dto.executed = model.executed;
        dto.executedAt = model.executedAt;
        return dto;
    }
}