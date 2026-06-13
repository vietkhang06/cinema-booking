package com.example.cinemabooking.data.dto;

public class CineShopOrderResponseDTO {
    private String orderId;
    private String paymentId;
    private String paymentCode;
    private double totalPrice;
    private String paymentMethod;
    private long createdAt;

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
