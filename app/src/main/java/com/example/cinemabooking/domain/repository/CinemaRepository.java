package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;

import java.util.List;

public interface CinemaRepository {
    void createCinema(Cinema cinema, ResultCallback<Cinema> callback);
    void getCinemaById(String cinemaId, ResultCallback<Cinema> callback);
    void getAllCinemas(ResultCallback<List<Cinema>> callback);
    void updateCinema(Cinema cinema, ResultCallback<Cinema> callback);
    void softDeleteCinema(String cinemaId, ResultCallback<Void> callback);
}