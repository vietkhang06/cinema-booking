package com.example.cinemabooking.domain.model;

public class Seat {
    public String seatId;
    public String showtimeId;
    public String seatCode;
    public String rowName;
    public Integer columnNo;
    public String seatType;
    public String status;
    public String heldBy;
    public Long heldUntil;
    public String bookedBy;
    public Long bookedAt;
    public double priceOverride;

    public Seat() {
    }
}