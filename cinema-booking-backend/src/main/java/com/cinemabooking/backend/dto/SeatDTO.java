package com.cinemabooking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    public static final String COLLECTION_NAME = "seats";
    
    private String seatId;
    private String showtimeId;
    private String seatCode;
    private String rowName;
    private int columnNo;
    private String seatType;
    private String status;
    private String heldBy;
    private long heldUntil;
    private String bookedBy;
    private long bookedAt;
    private double priceOverride;
}
