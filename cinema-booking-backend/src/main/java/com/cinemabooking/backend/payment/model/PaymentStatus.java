package com.cinemabooking.backend.payment.model;

public enum PaymentStatus {
    PENDING,
    WAITING_CONFIRMATION,
    PAID,
    FAILED,
    EXPIRED,
    CANCELLED
}

