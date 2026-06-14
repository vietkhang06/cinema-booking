package com.example.cinemabooking.ui.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.domain.model.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    public interface ReviewActionListener {
        void onLikeClick(Review review, int position);
        void onDislikeClick(Review review, int position);
        void onReplyClick(Review review, int position);
        default void onDeleteClick(Review review, int position) {}
    }

    private List<Review> reviewList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private ReviewActionListener listener;
    private String currentUserId;

    public void setListener(ReviewActionListener listener, String currentUserId) {
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    public void setReviews(List<Review> reviews) {
        this.reviewList = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addReviews(List<Review> reviews) {
        if (reviews != null && !reviews.isEmpty()) {
            int startPos = this.reviewList.size();
            this.reviewList.addAll(reviews);
            notifyItemRangeInserted(startPos, reviews.size());
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviewList.get(position));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvReviewDate, tvReviewContent;
        android.widget.RatingBar ratingBarReview;
        View btnLike, btnDislike, btnReply;
        TextView tvLikeCount, tvDislikeCount, tvReplyCount;
        android.widget.ImageView imgLike, imgDislike, imgUserAvatar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            ratingBarReview = itemView.findViewById(R.id.ratingBarReview);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);
            btnReply = itemView.findViewById(R.id.btnReply);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvDislikeCount = itemView.findViewById(R.id.tvDislikeCount);
            tvReplyCount = itemView.findViewById(R.id.tvReplyCount);
            imgLike = itemView.findViewById(R.id.imgLike);
            imgDislike = itemView.findViewById(R.id.imgDislike);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            
            btnLike.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onLikeClick(reviewList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
            btnDislike.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDislikeClick(reviewList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
            btnReply.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onReplyClick(reviewList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(reviewList.get(getAdapterPosition()), getAdapterPosition());
                    return true;
                }
                return false;
            });
        }

        void bind(Review review) {
            if (review.userId != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(review.userId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                if (doc.getString("name") != null) {
                                    tvUserName.setText(doc.getString("name"));
                                } else {
                                    tvUserName.setText("Người dùng");
                                }
                                String avatarUrl = doc.getString("avatarUrl");
                                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    com.bumptech.glide.Glide.with(itemView.getContext())
                                            .load(avatarUrl)
                                            .placeholder(R.drawable.ic_user_avatar_24)
                                            .into(imgUserAvatar);
                                } else {
                                    imgUserAvatar.setImageResource(R.drawable.ic_user_avatar_24);
                                }
                            } else {
                                tvUserName.setText("Người dùng");
                                imgUserAvatar.setImageResource(R.drawable.ic_user_avatar_24);
                            }
                        })
                        .addOnFailureListener(e -> tvUserName.setText("Người dùng"));
            } else {
                tvUserName.setText("Người dùng");
            }

            if (review.createdAt > 0) {
                tvReviewDate.setText(dateFormat.format(new Date(review.createdAt)));
            }

            if ("hidden".equals(review.status)) {
                tvReviewContent.setVisibility(View.VISIBLE);
                tvReviewContent.setText("Bình luận này đã bị quản trị viên ẩn.");
                tvReviewContent.setTextColor(android.graphics.Color.GRAY);
                tvReviewContent.setTypeface(null, android.graphics.Typeface.ITALIC);
            } else if (review.content == null || review.content.trim().isEmpty()) {
                tvReviewContent.setVisibility(View.GONE);
            } else {
                tvReviewContent.setVisibility(View.VISIBLE);
                tvReviewContent.setText(review.content);
                tvReviewContent.setTextColor(android.graphics.Color.parseColor("#444444"));
                tvReviewContent.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
            
            if (review.rating != null && review.rating > 0) {
                ratingBarReview.setVisibility(View.VISIBLE);
                ratingBarReview.setRating(review.rating);
            } else {
                ratingBarReview.setVisibility(View.GONE);
            }
            
            tvLikeCount.setText(String.valueOf(review.likedBy != null ? review.likedBy.size() : 0));
            tvDislikeCount.setText(String.valueOf(review.dislikedBy != null ? review.dislikedBy.size() : 0));
            tvReplyCount.setText(review.replyCount != null && review.replyCount > 0 ? review.replyCount + " Trả lời" : "Trả lời");

            boolean isLiked = currentUserId != null && review.likedBy != null && review.likedBy.contains(currentUserId);
            boolean isDisliked = currentUserId != null && review.dislikedBy != null && review.dislikedBy.contains(currentUserId);
            
            imgLike.setColorFilter(isLiked ? android.graphics.Color.parseColor("#1E4F8F") : android.graphics.Color.parseColor("#888888"));
            tvLikeCount.setTextColor(isLiked ? android.graphics.Color.parseColor("#1E4F8F") : android.graphics.Color.parseColor("#888888"));
            
            imgDislike.setColorFilter(isDisliked ? android.graphics.Color.parseColor("#E06A00") : android.graphics.Color.parseColor("#888888"));
            tvDislikeCount.setTextColor(isDisliked ? android.graphics.Color.parseColor("#E06A00") : android.graphics.Color.parseColor("#888888"));
        }
    }
}