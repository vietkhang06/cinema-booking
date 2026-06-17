package com.example.cinemabooking.data.dto;

import java.util.List;

public class BookingDTO {
    public String bookingId;
    public String userId;
    public String movieId;
    public String cinemaId;
    public String roomId;
    public String showtimeId;
    public String movieTitleSnapshot;
    public String movieImageUrlSnapshot;
    public String cinemaNameSnapshot;
    public String roomNameSnapshot;
    public Long showtimeStartAtSnapshot;
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
    public Long paymentAt;
    public String qrCodeValue;
    public Long checkInAt;
    public Long createdAt;
    public Long updatedAt;
    public Boolean deleted;
    public int pointsConsumed;
    public String promoCode;
    public double discountVoucher;

    public BookingDTO() {
    }
}