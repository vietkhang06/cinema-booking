package com.example.cinemabooking.domain.usecase.review;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Review;
import com.example.cinemabooking.domain.repository.ReviewRepository;

import java.util.List;

public class GetReviewsByMovieUseCase {
    private final ReviewRepository repository;

    public GetReviewsByMovieUseCase(ReviewRepository repository) {
        this.repository = repository;
    }

    public void execute(String movieId, ResultCallback<List<Review>> callback) {
        repository.getReviewsByMovieId(movieId, callback);
    }
}