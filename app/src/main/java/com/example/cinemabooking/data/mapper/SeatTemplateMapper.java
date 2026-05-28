package com.example.cinemabooking.data.mapper;

import com.example.cinemabooking.data.dto.SeatTemplateDTO;
import com.example.cinemabooking.domain.model.SeatTemplate;

public final class SeatTemplateMapper {
    private SeatTemplateMapper() {
    }

    public static SeatTemplate toDomain(SeatTemplateDTO dto) {
        if (dto == null) return null;
        SeatTemplate model = new SeatTemplate();
        model.seatId = dto.seatId;
        model.roomId = dto.roomId;
        model.seatCode = dto.seatCode;
        model.rowName = dto.rowName;
        model.columnNo = dto.columnNo;
        model.seatType = dto.seatType;
        model.isEnabled = dto.isEnabled;
        return model;
    }

    public static SeatTemplateDTO toDTO(SeatTemplate model) {
        if (model == null) return null;
        SeatTemplateDTO dto = new SeatTemplateDTO();
        dto.seatId = model.seatId;
        dto.roomId = model.roomId;
        dto.seatCode = model.seatCode;
        dto.rowName = model.rowName;
        dto.columnNo = model.columnNo;
        dto.seatType = model.seatType;
        dto.isEnabled = model.isEnabled;
        return dto;
    }
}