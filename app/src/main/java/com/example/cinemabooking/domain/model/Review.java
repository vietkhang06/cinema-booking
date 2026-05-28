package com.example.cinemabooking.domain.model;

public class Review {
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

    public Review() {
    }
}