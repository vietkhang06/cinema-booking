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
                .addOnSuccessListener(aVoid -> {
                    updateMovieRating(review.movieId);
                    callback.onSuccess(review);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getReviewsByMovieId(String movieId, ResultCallback<List<Review>> callback) {
        reviewRef.whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ReviewDTO dto = doc.toObject(ReviewDTO.class);
                        if (dto.deleted != null && dto.deleted) continue;
                        reviews.add(ReviewMapper.toDomain(dto));
                    }
                    reviews.sort((r1, r2) -> Long.compare(r2.createdAt != null ? r2.createdAt : 0, r1.createdAt != null ? r1.createdAt : 0));
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
                .addOnSuccessListener(aVoid -> {
                    updateMovieRating(review.movieId);
                    callback.onSuccess(review);
                })
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
                .addOnSuccessListener(aVoid -> {
                    // Update rating logic requires movieId, so we first fetch the review
                    getReviewById(reviewId, new ResultCallback<Review>() {
                        @Override
                        public void onSuccess(Review data) {
                            if (data != null && data.movieId != null) {
                                updateMovieRating(data.movieId);
                            }
                            callback.onSuccess(null);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onSuccess(null);
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getUserReviewForMovie(String userId, String movieId, ResultCallback<Review> callback) {
        reviewRef.whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess(null);
                    } else {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                            ReviewDTO dto = doc.toObject(ReviewDTO.class);
                            if (dto.deleted != null && dto.deleted) continue;
                            callback.onSuccess(ReviewMapper.toDomain(dto));
                            return;
                        }
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getReviewsByMovieIdPaged(String movieId, com.google.firebase.firestore.DocumentSnapshot lastVisible, int limit, ResultCallback<android.util.Pair<List<Review>, com.google.firebase.firestore.DocumentSnapshot>> callback) {
        // Lấy tất cả và tự phân trang ở client để tránh lỗi thiếu Composite Index trên Firestore
        reviewRef.whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ReviewDTO dto = doc.toObject(ReviewDTO.class);
                        if (dto.deleted != null && dto.deleted) continue;
                        reviews.add(ReviewMapper.toDomain(dto));
                    }
                    // Sort descending by createdAt
                    reviews.sort((r1, r2) -> Long.compare(r2.createdAt != null ? r2.createdAt : 0, r1.createdAt != null ? r1.createdAt : 0));
                    
                    // Simple client-side pagination simulation
                    // Since we can't easily use DocumentSnapshot with local sorting, we just return all of them or slice it
                    // To keep UI working without changes, just return ALL reviews for now and set lastVisible = null (hasMore = false)
                    callback.onSuccess(new android.util.Pair<>(reviews, null));
                }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void toggleLike(String reviewId, String userId, ResultCallback<Review> callback) {
        db.runTransaction((com.google.firebase.firestore.Transaction.Function<ReviewDTO>) transaction -> {
            com.google.firebase.firestore.DocumentReference docRef = reviewRef.document(reviewId);
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(docRef);
            ReviewDTO dto = snapshot.toObject(ReviewDTO.class);
            if (dto == null) return null;

            if (dto.likedBy == null) dto.likedBy = new ArrayList<>();
            if (dto.dislikedBy == null) dto.dislikedBy = new ArrayList<>();

            if (dto.likedBy.contains(userId)) {
                dto.likedBy.remove(userId);
            } else {
                dto.likedBy.add(userId);
                dto.dislikedBy.remove(userId);
            }
            transaction.set(docRef, dto);
            return dto;
        }).addOnSuccessListener(dto -> {
            if (dto != null) callback.onSuccess(ReviewMapper.toDomain(dto));
            else callback.onError("Review not found");
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void toggleDislike(String reviewId, String userId, ResultCallback<Review> callback) {
        db.runTransaction((com.google.firebase.firestore.Transaction.Function<ReviewDTO>) transaction -> {
            com.google.firebase.firestore.DocumentReference docRef = reviewRef.document(reviewId);
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(docRef);
            ReviewDTO dto = snapshot.toObject(ReviewDTO.class);
            if (dto == null) return null;

            if (dto.likedBy == null) dto.likedBy = new ArrayList<>();
            if (dto.dislikedBy == null) dto.dislikedBy = new ArrayList<>();

            if (dto.dislikedBy.contains(userId)) {
                dto.dislikedBy.remove(userId);
            } else {
                dto.dislikedBy.add(userId);
                dto.likedBy.remove(userId);
            }
            transaction.set(docRef, dto);
            return dto;
        }).addOnSuccessListener(dto -> {
            if (dto != null) callback.onSuccess(ReviewMapper.toDomain(dto));
            else callback.onError("Review not found");
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void updateMovieRating(String movieId) {
        if (movieId == null || movieId.isEmpty()) return;
        reviewRef.whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double totalRating = 0;
                    int count = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ReviewDTO dto = doc.toObject(ReviewDTO.class);
                        if (dto.deleted != null && dto.deleted) continue;
                        if (dto.rating != null && dto.rating > 0) {
                            totalRating += dto.rating;
                            count++;
                        }
                    }
                    if (count > 0) {
                        double avg = totalRating / count;
                        avg = Math.round(avg * 10.0) / 10.0;
                        db.collection("movies").document(movieId)
                                .update("ratingAvg", avg, "ratingCount", count);
                    }
                });
    }
}