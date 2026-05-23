package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.data.remote.datasource.CinemaRemoteDataSource;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.repository.CinemaRepository;

import java.util.List;

public class CinemaRepositoryImpl implements CinemaRepository {

    private final CinemaRemoteDataSource remote = new CinemaRemoteDataSource();

    @Override
    public void createCinema(Cinema cinema, ResultCallback<Cinema> callback) {
        remote.createCinema(cinema, callback);
    }

    @Override
    public void getAllCinemas(ResultCallback<List<Cinema>> callback) {
        remote.getAllCinemas(callback);
    }

    @Override
    public void getCinemaById(String cinemaId, ResultCallback<Cinema> callback) {
        remote.getCinemaById(cinemaId, callback);
    }

    @Override
    public void updateCinema(Cinema cinema, ResultCallback<Cinema> callback) {
        remote.updateCinema(cinema, callback);
    }

    @Override
    public void softDeleteCinema(String cinemaId, ResultCallback<Void> callback) {
        remote.softDeleteCinema(cinemaId, callback);
    }
}
