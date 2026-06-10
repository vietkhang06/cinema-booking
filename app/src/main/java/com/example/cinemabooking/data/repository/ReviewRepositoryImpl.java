package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.data.dto.ReviewDTO;
import com.example.cinemabooking.data.mapper.ReviewMapper;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Review;
import com.example.cinemabooking.domain.repository.ReviewRepository;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReviewRepositoryImpl implements ReviewRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reviewRef = db.collection(FirestoreCollections.REVIEWS);

    @Override
    public void createReview(Review review, ResultCallback<Review> callback) {
        String id = reviewRef.document().getId();
        review.reviewId = id;
        review.createdAt = System.currentTimeMillis();
        review.updatedAt = System.currentTimeMillis();
        review.status = "active";
        review.deleted = false;

        ReviewDTO dto = ReviewMapper.toDTO(review);
        reviewRef.document(id).set(dto)
                .addOnSuccessListener(aVoid -> callback.onSuccess(review))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getReviewsByMovieId(String movieId, ResultCallback<List<Review>> callback) {
        reviewRef.whereEqualTo("movieId", movieId)
                .whereEqualTo("deleted", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ReviewDTO dto = doc.toObject(ReviewDTO.class);
                        reviews.add(ReviewMapper.toDomain(dto));
                    }
                    callback.onSuccess(reviews);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getReviewById(String reviewId, ResultCallback<Review> callback) {
        reviewRef.document(reviewId).get()
                .addOnSuccessListener(doc -> callback.onSuccess(ReviewMapper.toDomain(doc.toObject(ReviewDTO.class))))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getReviewsByUserId(String userId, ResultCallback<List<Review>> callback) {
        reviewRef.whereEqualTo("userId", userId).get()
                .addOnSuccessListener(qs -> {
                    List<Review> list = new ArrayList<>();
                    for (QueryDocumentSnapshot d : qs) list.add(ReviewMapper.toDomain(d.toObject(ReviewDTO.class)));
                    callback.onSuccess(list);
                }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateReview(Review review, ResultCallback<Review> callback) {
        review.updatedAt = System.currentTimeMillis();
        reviewRef.document(review.reviewId).set(ReviewMapper.toDTO(review))
                .addOnSuccessListener(aVoid -> callback.onSuccess(review))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void hideReview(String reviewId, ResultCallback<Review> callback) {
        reviewRef.document(reviewId).update("status", "hidden")
                .addOnSuccessListener(aVoid -> getReviewById(reviewId, callback))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteReview(String reviewId, ResultCallback<Void> callback) {
        reviewRef.document(reviewId).update("deleted", true)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}