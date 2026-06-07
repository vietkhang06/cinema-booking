package com.example.cinemabooking.data.dto;

public class ShowtimeDTO {
    public String showtimeId;
    public String movieId;
    public String cinemaId;
    public String roomId;
    public long startAt;
    public long endAt;
    public double basePrice;
    public String format;
    public String language;
    public String status;
    public int totalSeats;
    public int bookedSeatsCount;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;
    public boolean isScheduled;
    public boolean executed;
    public long executedAt;

    public ShowtimeDTO() {
    }
}