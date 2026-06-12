package com.example.cinemabooking.domain.model;

public class Review {
    public String reviewId;
    public String userId;
    public String movieId;
    public String bookingId;
    public String movieTitleSnapshot;
    public Integer rating;
    public String content;
    public String status;
    public Long createdAt;
    public Long updatedAt;
    public Boolean deleted;

    public Review() {
    }
}