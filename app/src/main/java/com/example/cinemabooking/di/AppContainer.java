package com.example.cinemabooking.di;

import android.content.Context;

import com.example.cinemabooking.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabooking.core.session.SessionManager;
import com.example.cinemabooking.data.repository.MovieRepositoryImpl;
import com.example.cinemabooking.domain.repository.MovieRepository;
import com.example.cinemabooking.domain.usecase.movie.GetMoviesUseCase;

import com.example.cinemabooking.data.remote.datasource.BannerRemoteDataSource;
import com.example.cinemabooking.data.repository.BannerRepositoryImpl;
import com.example.cinemabooking.domain.repository.BannerRepository;
import com.example.cinemabooking.domain.usecase.banner.GetBannersUseCase;

public class AppContainer {

    private final SessionManager sessionManager;
    private final MovieRemoteDataSource movieRemoteDataSource;
    private final MovieRepository movieRepository;
    private final GetMoviesUseCase getMoviesUseCase;
    private final BannerRemoteDataSource bannerRemoteDataSource;
    private final BannerRepository bannerRepository;
    private final GetBannersUseCase getBannersUseCase;

    public AppContainer(Context context) {
        sessionManager = new SessionManager(context);
        movieRemoteDataSource = new MovieRemoteDataSource();
        movieRepository = new MovieRepositoryImpl(movieRemoteDataSource);
        getMoviesUseCase = new GetMoviesUseCase(movieRepository);
        bannerRemoteDataSource = new BannerRemoteDataSource();
        bannerRepository = new BannerRepositoryImpl(bannerRemoteDataSource);
        getBannersUseCase = new GetBannersUseCase(bannerRepository);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public GetMoviesUseCase getMoviesUseCase() {
        return getMoviesUseCase;
    }

    public GetBannersUseCase getBannersUseCase() {
        return getBannersUseCase;
    }
}