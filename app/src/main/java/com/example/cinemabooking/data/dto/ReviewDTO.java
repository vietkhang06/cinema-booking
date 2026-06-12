package com.example.cinemabooking.data.dto;

public class ReviewDTO {
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

    public ReviewDTO() {
    }
}