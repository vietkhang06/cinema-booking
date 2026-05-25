package com.example.cinemabooking.ui.admin.movie.model;

public class AdminMovieItem  {
    public String movieId;
    public String title;
    public String posterUrl;
    public String status;
    public int duration;

    public AdminMovieItem(String movieId, String title, String posterUrl, String status, int duration) {
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.status = status;
        this.duration = duration;
    }
}