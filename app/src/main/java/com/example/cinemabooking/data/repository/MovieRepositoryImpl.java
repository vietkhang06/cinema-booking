package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.repository.MovieRepository;

import java.util.List;

public class MovieRepositoryImpl implements MovieRepository {

    private final MovieRemoteDataSource remoteDataSource;

    public MovieRepositoryImpl() {
        this(new MovieRemoteDataSource());
    }

    public MovieRepositoryImpl(MovieRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public void createMovie(Movie movie, ResultCallback<Movie> callback) {
        remoteDataSource.createMovie(movie, callback);
    }

    @Override
    public void getMovieById(String movieId, ResultCallback<Movie> callback) {
        remoteDataSource.getMovieById(movieId, callback);
    }

    @Override
    public void getAllMovies(ResultCallback<List<Movie>> callback) {
        remoteDataSource.getAllMovies(callback);
    }

    @Override
    public void getMoviesByStatus(String status, ResultCallback<List<Movie>> callback) {
        remoteDataSource.getMoviesByStatus(status, callback);
    }

    @Override
    public void searchMovies(String keyword, ResultCallback<List<Movie>> callback) {
        remoteDataSource.searchMovies(keyword, callback);
    }

    @Override
    public void updateMovie(Movie movie, ResultCallback<Movie> callback) {
        remoteDataSource.updateMovie(movie, callback);
    }

    @Override
    public void softDeleteMovie(String movieId, ResultCallback<Void> callback) {
        remoteDataSource.softDeleteMovie(movieId, callback);
    }
}