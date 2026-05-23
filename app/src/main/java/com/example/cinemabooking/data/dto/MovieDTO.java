package com.example.cinemabooking.data.dto;

import java.util.List;

public class MovieDTO {
    public String movieId;
    public String title;
    public String description;
    public List<String> genres;
    public String language;
    public int durationMinutes;
    public long releaseDate;
    public String ageRating;
    public String posterUrl;
    public String trailerUrl;
    public double ratingAvg;
    public int ratingCount;
    public String status;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public MovieDTO() {
    }
}