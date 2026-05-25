package com.cinemabooking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnackOrderSnapshot {
    private String snackId;
    private String snackName;
    private String snackImgURL;
    private double price;
    private int quantity;
}