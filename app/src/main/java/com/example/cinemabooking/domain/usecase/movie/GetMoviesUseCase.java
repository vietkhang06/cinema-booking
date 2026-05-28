package com.example.cinemabooking.domain.usecase.movie;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.repository.MovieRepository;

import java.util.List;

public class GetMoviesUseCase {

    private final MovieRepository movieRepository;

    public GetMoviesUseCase(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public void execute(ResultCallback<List<Movie>> callback) {
        movieRepository.getAllMovies(callback);
    }

    public void executeByStatus(String status, ResultCallback<List<Movie>> callback) {
        movieRepository.getMoviesByStatus(status, callback);
    }
}