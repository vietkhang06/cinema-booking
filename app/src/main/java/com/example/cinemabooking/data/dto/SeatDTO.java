package com.example.cinemabooking.data.dto;

public class SeatDTO {
    public String seatId;
    public String showtimeId;
    public String seatCode;
    public String rowName;
    public int columnNo;
    public String seatType;
    public String status;
    public String heldBy;
    public long heldUntil;
    public String bookedBy;
    public long bookedAt;
    public double priceOverride;
    public transient boolean isSelected = false;

    public SeatDTO() {
    }
}