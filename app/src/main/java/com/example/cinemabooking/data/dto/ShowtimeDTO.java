package com.example.cinemabooking.data.dto;

public class ShowtimeDTO {
    public String showtimeId;
    public String movieId;
    public String cinemaId;
    public String roomId;
    public Long startAt;
    public Long endAt;
    public double basePrice;
    public String format;
    public String language;
    public String status;
    public Integer totalSeats;
    public Integer bookedSeatsCount;
    public Long createdAt;
    public Long updatedAt;
    public Boolean deleted;
    public Boolean isScheduled;
    public Boolean executed;
    public Long executedAt;

    public ShowtimeDTO() {
    }
}