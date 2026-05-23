package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.RoomDTO;
import com.example.cinemabooking.domain.model.Room;

public final class RoomMapper {
    private RoomMapper() {
    }

    public static Room toDomain(RoomDTO dto) {
        if (dto == null) return null;
        Room model = new Room();
        model.roomId = dto.roomId;
        model.cinemaId = dto.cinemaId;
        model.name = dto.name;
        model.layoutType = dto.layoutType;
        model.seatRows = dto.seatRows;
        model.seatCols = dto.seatCols;
        model.totalSeats = dto.totalSeats;
        model.status = dto.status;
        model.createdAt = dto.createdAt;
        model.updatedAt = dto.updatedAt;
        model.deleted = dto.deleted;
        return model;
    }

    public static RoomDTO toDTO(Room model) {
        if (model == null) return null;
        RoomDTO dto = new RoomDTO();
        dto.roomId = model.roomId;
        dto.cinemaId = model.cinemaId;
        dto.name = model.name;
        dto.layoutType = model.layoutType;
        dto.seatRows = model.seatRows;
        dto.seatCols = model.seatCols;
        dto.totalSeats = model.totalSeats;
        dto.status = model.status;
        dto.createdAt = model.createdAt;
        dto.updatedAt = model.updatedAt;
        dto.deleted = model.deleted;
        return dto;
    }
}