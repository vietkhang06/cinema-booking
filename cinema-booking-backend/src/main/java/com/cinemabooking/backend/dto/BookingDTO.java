package com.cinemabooking.backend.dto;

import com.google.cloud.firestore.annotation.Exclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    public static final String COLLECTION_NAME = "bookings";

    private String bookingId;
    private String userId;
    private String showtimeId;

    private String movieId;
    public String movieImageUrlSnapshot;
    private String movieTitleSnapshot;
    private String cinemaNameSnapshot;
    private String roomNameSnapshot;
    private long showtimeStartAtSnapshot;
    private List<String> seatCodes;
    private List<String> seatIds;
    private String snackOrderId;
    private double subtotal;
    private double discount;
    private double total;
    private String paymentMethod;
    private String paymentStatus;
    private String bookingStatus;
    private String paymentCode;
    private String qrCodeValue;
    private long checkInAt;
    private long createdAt;
    private long updatedAt;
    private boolean deleted;

    private long paymentAt;
    private List<SnackOrderSnapshot> snackOrder;

    // dung cho cac phep JOIN ko hien thi trong firestore
    @Exclude private UserDTO user;
    @Exclude private ShowtimeDTO showtime;
    @Exclude private MovieDTO movie;
}
