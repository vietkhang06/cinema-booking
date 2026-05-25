package com.example.cinemabooking.ui.admin.model;

public class AdminPayment {
    public String paymentId;
    public String bookingId;
    public String paymentCode;
    public String userId;
    public String provider;
    public double amount;
    public String status;
    public String transactionId;
    public String payUrl;
    public long createdAt;
    public long updatedAt;

    public AdminPayment() {
        // Default constructor required for Firestore toObject()
    }
}
