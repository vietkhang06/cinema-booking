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
    public long validFrom;
    public long validTo;
    public String status;
    public int usageLimit;
    public int usedCount;
    public String targetRole;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public Promotion() {
    }
}