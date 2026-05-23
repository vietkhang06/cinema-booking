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
