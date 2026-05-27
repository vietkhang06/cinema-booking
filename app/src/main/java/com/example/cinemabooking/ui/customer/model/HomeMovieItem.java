package com.example.cinemabooking.ui.customer.model;

import java.util.ArrayList;
import java.util.List;
public class HomeMovieItem {

    public static final String NOW_SHOWING = "NOW_SHOWING";
    public static final String COMING_SOON = "COMING_SOON";

    private String movieId;
    private String title;
    private String imageUrl;
    private String rating;
    private String ageRating;
    private String status;
    private List<String> genres;

    public HomeMovieItem() {}

    public HomeMovieItem(String movieId, String title, String imageUrl,
                         String rating, String ageRating, String status,
                         List<String> genres) {
        this.movieId = movieId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.ageRating = ageRating;
        this.status = status;
        this.genres = genres;
    }

    public HomeMovieItem(String movieId, String title, String imageUrl, String rating, String ageRating, String status) {
        this(movieId, title, imageUrl, rating, ageRating, status, new ArrayList<>());
    }

    public HomeMovieItem(String title, String imageUrl, String rating, String ageRating, String status) {
        this(null, title, imageUrl, rating, ageRating, status, new ArrayList<>());
    }

    public String getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRating() {
        return rating;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public String getStatus() {
        return status;
    }
    public List<String> getGenres() {
        return genres != null ? genres : new ArrayList<>();
    }

}