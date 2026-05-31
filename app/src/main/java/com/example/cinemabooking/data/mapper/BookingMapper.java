package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.BookingDTO;
import com.example.cinemabooking.domain.model.Booking;

public final class BookingMapper {
    private BookingMapper() {
    }

    public static Booking toDomain(BookingDTO dto) {
        if (dto == null) return null;
        Booking model = new Booking();
        model.bookingId = dto.bookingId;
        model.userId = dto.userId;
        model.movieId = dto.movieId;
//        model.cinemaId = dto.cinemaId;
//        model.roomId = dto.roomId;
        model.showtimeId = dto.showtimeId;
        model.movieTitleSnapshot = dto.movieTitleSnapshot;
        model.movieImageUrlSnapshot = dto.movieImageUrlSnapshot;
        model.cinemaNameSnapshot = dto.cinemaNameSnapshot;
        model.roomNameSnapshot = dto.roomNameSnapshot;
        model.showtimeStartAtSnapshot = dto.showtimeStartAtSnapshot;
        model.seatCodes = dto.seatCodes;
        model.seatIds = dto.seatIds;
        model.snackOrderId = dto.snackOrderId;
        model.subtotal = dto.subtotal;
        model.discount = dto.discount;
        model.total = dto.total;
        model.paymentMethod = dto.paymentMethod;
        model.paymentStatus = dto.paymentStatus;
        model.bookingStatus = dto.bookingStatus;
        model.paymentCode = dto.paymentCode;
        model.paymentAt = dto.paymentAt;
        model.qrCodeValue = dto.qrCodeValue;
        model.checkInAt = dto.checkInAt;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        return model;
    }

    public static BookingDTO toDTO(Booking model) {
        if (model == null) return null;
        BookingDTO dto = new BookingDTO();
        dto.bookingId = model.bookingId;
        dto.userId = model.userId;
        dto.movieId = model.movieId;
//        dto.cinemaId = model.cinemaId;
//        dto.roomId = model.roomId;
        dto.showtimeId = model.showtimeId;
        dto.movieTitleSnapshot = model.movieTitleSnapshot;
        dto.movieImageUrlSnapshot = model.movieImageUrlSnapshot;
        dto.cinemaNameSnapshot = model.cinemaNameSnapshot;
        dto.roomNameSnapshot = model.roomNameSnapshot;
        dto.showtimeStartAtSnapshot = model.showtimeStartAtSnapshot;
        dto.seatCodes = model.seatCodes;
        dto.seatIds = model.seatIds;
        dto.snackOrderId = model.snackOrderId;
        dto.subtotal = model.subtotal;
        dto.discount = model.discount;
        dto.total = model.total;
        dto.paymentMethod = model.paymentMethod;
        dto.paymentStatus = model.paymentStatus;
        dto.bookingStatus = model.bookingStatus;
        dto.paymentCode = model.paymentCode;
        dto.paymentAt = model.paymentAt;
        dto.qrCodeValue = model.qrCodeValue;
        dto.checkInAt = model.checkInAt;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        return dto;
    }
}