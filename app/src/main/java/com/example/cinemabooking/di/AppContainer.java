package com.example.cinemabooking.di;

import android.content.Context;

import com.example.cinemabooking.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.domain.repository.MovieRepository;
import com.example.cinemabooking.domain.usecase.movie.GetMoviesUseCase;

public class AppContainer {

    private final MovieRemoteDataSource movieRemoteDataSource;
    private final MovieRepository movieRepository;
    private final GetMoviesUseCase getMoviesUseCase;

    public AppContainer(Context context) {
        movieRemoteDataSource = new MovieRemoteDataSource();
        movieRepository = new MovieRepositoryImpl(movieRemoteDataSource);
        getMoviesUseCase = new GetMoviesUseCase(movieRepository);
    }

    public GetMoviesUseCase getMoviesUseCase() {
        return getMoviesUseCase;
    }
}