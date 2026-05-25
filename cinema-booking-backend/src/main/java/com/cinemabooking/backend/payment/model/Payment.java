package com.cinemabooking.backend.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private String paymentId;
    private String bookingId;
    private String paymentCode;
    private String userId;
    private String provider;
    private double amount;
    private String status;
    private String transactionId;
    private String payUrl;
    private long createdAt;
    private long updatedAt;
}
