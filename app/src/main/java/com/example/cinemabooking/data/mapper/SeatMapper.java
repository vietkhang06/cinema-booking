package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.SeatDTO;
import com.example.cinemabooking.domain.model.Seat;

public final class SeatMapper {
    private SeatMapper() {
    }

    public static Seat toDomain(SeatDTO dto) {
        if (dto == null) return null;
        Seat model = new Seat();
        model.seatId = dto.seatId;
        model.showtimeId = dto.showtimeId;
        model.seatCode = dto.seatCode;
        model.rowName = dto.rowName;
        model.columnNo = dto.columnNo;
        model.seatType = dto.seatType;
        model.status = dto.status;
        model.heldBy = dto.heldBy;
        model.heldUntil = dto.heldUntil;
        model.bookedBy = dto.bookedBy;
        model.bookedAt = dto.bookedAt;
        model.priceOverride = dto.priceOverride;
        return model;
    }

    public static SeatDTO toDTO(Seat model) {
        if (model == null) return null;
        SeatDTO dto = new SeatDTO();
        dto.seatId = model.seatId;
        dto.showtimeId = model.showtimeId;
        dto.seatCode = model.seatCode;
        dto.rowName = model.rowName;
        dto.columnNo = model.columnNo;
        dto.seatType = model.seatType;
        dto.status = model.status;
        dto.heldBy = model.heldBy;
        dto.heldUntil = model.heldUntil;
        dto.bookedBy = model.bookedBy;
        dto.bookedAt = model.bookedAt;
        dto.priceOverride = model.priceOverride;
        return dto;
    }
}