package com.example.cinemabooking.domain.model;

import java.util.List;

public class SnackOrder {
    public String snackOrderId;
    public String userId;
    public String bookingId;
    public List<SnackOrderItem> items;
    public double subtotal;
    public double discount;
    public double total;
    public String status;
    public String note;
    public Long createdAt;
    public Long updatedAt;
    public Boolean deleted;

    public SnackOrder() {
    }
}