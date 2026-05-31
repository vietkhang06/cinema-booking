package com.example.cinemabooking.domain.model;

import java.util.List;

public class Booking {
    public String bookingId;
    public String userId;
    public String showtimeId;
    public String movieId;
    public String movieTitleSnapshot;
    public String movieImageUrlSnapshot;
    public String cinemaNameSnapshot;
    public String roomNameSnapshot;
    public long showtimeStartAtSnapshot;
    public List<String> seatCodes;
    public List<String> seatIds;
    public String snackOrderId;
    public double subtotal;
    public double discount;
    public double total;
    public String paymentMethod;
    public String paymentStatus;
    public String bookingStatus;
    public String paymentCode;
    public long paymentAt;
    public String qrCodeValue;
    public long checkInAt;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public Booking() {
    }
}