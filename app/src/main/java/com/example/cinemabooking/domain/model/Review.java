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
    public java.util.List<String> likedBy = new java.util.ArrayList<>();
    public java.util.List<String> dislikedBy = new java.util.ArrayList<>();
    public Integer replyCount = 0;

    public Review() {
    }
}