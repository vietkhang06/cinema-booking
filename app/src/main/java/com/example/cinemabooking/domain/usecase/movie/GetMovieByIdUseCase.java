package com.example.cinemabooking.domain.usecase.movie;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.repository.MovieRepository;

public class GetMovieByIdUseCase {

    private final MovieRepository movieRepository;

    public GetMovieByIdUseCase(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public void execute(String movieId, ResultCallback<Movie> callback) {
        movieRepository.getMovieById(movieId, callback);
    }
}