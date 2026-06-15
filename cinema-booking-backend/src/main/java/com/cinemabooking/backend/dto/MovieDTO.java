/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend DTO
 * 
 * Mô tả: 
 * Đối tượng DTO đại diện cho một bộ Phim.
 * Dùng để trả về chi tiết phim (ảnh poster, trailer, đạo diễn, mô tả ngắn) về cho client.
 */
package com.cinemabooking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    public static final String COLLECTION_NAME = "movies";
    
    private String movieId;
    private String title;
    private String description;
    private String language;
    private String ageRating;
    private String posterUrl;
    private String trailerUrl;
    private Double ratingAvg;
    private Integer ratingCount;
    private String status;
    private List<String> genres;
    private Integer durationMinutes;
    private Long createdAt;
    private Long updatedAt;
    private Boolean isActive;
    private Boolean deleted;
}
