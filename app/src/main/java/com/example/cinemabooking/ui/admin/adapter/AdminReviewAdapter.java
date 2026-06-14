package com.example.cinemabooking.ui.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
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

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.AdminReviewViewHolder> {

    public interface AdminReviewActionListener {
        void onDeleteClick(Review review, int position);
    }

    private List<Review> reviewList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private AdminReviewActionListener listener;

    public void setListener(AdminReviewActionListener listener) {
        this.listener = listener;
    }

    public void setReviews(List<Review> reviews) {
        this.reviewList = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_review, parent, false);
        return new AdminReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminReviewViewHolder holder, int position) {
        holder.bind(reviewList.get(position));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    class AdminReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminUserName, tvAdminReviewDate, tvAdminReviewContent;
        RatingBar ratingBarAdminReview;
        ImageView btnAdminDeleteReview, imgAdminUserAvatar;

        AdminReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminUserName = itemView.findViewById(R.id.tvAdminUserName);
            tvAdminReviewDate = itemView.findViewById(R.id.tvAdminReviewDate);
            tvAdminReviewContent = itemView.findViewById(R.id.tvAdminReviewContent);
            ratingBarAdminReview = itemView.findViewById(R.id.ratingBarAdminReview);
            btnAdminDeleteReview = itemView.findViewById(R.id.btnAdminDeleteReview);
            imgAdminUserAvatar = itemView.findViewById(R.id.imgAdminUserAvatar);

            btnAdminDeleteReview.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(reviewList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }

        void bind(Review review) {
            if (review.userId != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(review.userId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                if (doc.getString("name") != null) {
                                    tvAdminUserName.setText(doc.getString("name"));
                                } else {
                                    tvAdminUserName.setText("Người dùng");
                                }
                                String avatarUrl = doc.getString("avatarUrl");
                                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    com.bumptech.glide.Glide.with(itemView.getContext())
                                            .load(avatarUrl)
                                            .placeholder(R.drawable.login_icon)
                                            .into(imgAdminUserAvatar);
                                } else {
                                    imgAdminUserAvatar.setImageResource(R.drawable.login_icon);
                                }
                            } else {
                                tvAdminUserName.setText("Người dùng");
                                imgAdminUserAvatar.setImageResource(R.drawable.login_icon);
                            }
                        })
                        .addOnFailureListener(e -> tvAdminUserName.setText("Người dùng"));
            } else {
                tvAdminUserName.setText("Người dùng");
            }

            if (review.createdAt > 0) {
                tvAdminReviewDate.setText(dateFormat.format(new Date(review.createdAt)));
            }

            if ("hidden".equals(review.status)) {
                tvAdminReviewContent.setVisibility(View.VISIBLE);
                tvAdminReviewContent.setText("Bình luận này đã bị quản trị viên ẩn.");
                tvAdminReviewContent.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
                tvAdminReviewContent.setTypeface(null, android.graphics.Typeface.ITALIC);
                btnAdminDeleteReview.setVisibility(View.GONE);
            } else if (review.content == null || review.content.trim().isEmpty()) {
                tvAdminReviewContent.setVisibility(View.GONE);
                btnAdminDeleteReview.setVisibility(View.GONE); // Nothing to delete
            } else {
                tvAdminReviewContent.setVisibility(View.VISIBLE);
                tvAdminReviewContent.setText(review.content);
                tvAdminReviewContent.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                tvAdminReviewContent.setTypeface(null, android.graphics.Typeface.NORMAL);
                btnAdminDeleteReview.setVisibility(View.VISIBLE);
            }

            if (review.rating != null && review.rating > 0) {
                ratingBarAdminReview.setVisibility(View.VISIBLE);
                ratingBarAdminReview.setRating(review.rating);
            } else {
                ratingBarAdminReview.setVisibility(View.GONE);
            }
        }
    }
}
