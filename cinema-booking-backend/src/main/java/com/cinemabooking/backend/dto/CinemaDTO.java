package com.cinemabooking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaDTO {
    public static final String COLLECTION_NAME = "cinemas";

    private String cinemaId;
    private String name;
    private String address;
    private String city;
    private String district;
    private String phone;
    private String status;
    private Double latitude;
    private Double longitude;
    private Long createdAt;
    private Long updatedAt;
    private Boolean deleted;
}
