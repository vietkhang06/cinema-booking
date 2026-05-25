package com.cinemabooking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeDTO {
    public static final String COLLECTION_NAME = "showtimes";

    private String showtimeId;
    private String movieId;
    private String cinemaId;
    private String roomId;
    private long startAt;
    private long endAt;
    private double basePrice;
    private String format;
    private String language;
    private String status;
    private int totalSeats;
    private int bookedSeatsCount;
    private long createdAt;
    private long updatedAt;
    private boolean deleted;
}
