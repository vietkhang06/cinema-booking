package com.example.cinemabooking.domain.usecase.review;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Review;
import com.example.cinemabooking.domain.repository.ReviewRepository;

public class AddReviewUseCase {
    private final ReviewRepository repository;

    public AddReviewUseCase(ReviewRepository repository) {
        this.repository = repository;
    }

    public void execute(Review review, ResultCallback<Review> callback) {
        if ((review.content == null || review.content.trim().isEmpty()) && (review.rating == null || review.rating == 0)) {
            callback.onError("Nội dung bình luận không được để trống");
            return;
        }

        repository.createReview(review, callback);
    }
}