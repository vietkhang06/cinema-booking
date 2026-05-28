package com.example.cinemabooking.data.dto;

public class ReviewDTO {
    public String reviewId;
    public String userId;
    public String movieId;
    public String bookingId;
    public String movieTitleSnapshot;
    public int rating;
    public String content;
    public String status;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public ReviewDTO() {
    }
}