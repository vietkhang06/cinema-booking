package com.example.cinemabooking.domain.model;

public class Promotion {
    public String promoId;
    public String title;
    public String code;
    public String description;
    public String discountType;
    public double discountValue;
    public double minAmount;
    public double maxDiscountAmount;
    public Long validFrom;
    public Long validTo;
    public String status;
    public Integer usageLimit;
    public Integer usedCount;
    public String targetRole;
    public Long createdAt;
    public Long updatedAt;
    public Boolean deleted;
    public String userId;

    public Promotion() {
    }
}