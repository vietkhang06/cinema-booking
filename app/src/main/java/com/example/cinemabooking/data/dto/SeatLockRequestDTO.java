package com.example.cinemabooking.data.dto;

import java.util.List;

public class SeatLockRequestDTO {
    public String showtimeId;
    public List<String> seatIds;

    public SeatLockRequestDTO() {
    }

    public SeatLockRequestDTO(String showtimeId, List<String> seatIds) {
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
    }
}
