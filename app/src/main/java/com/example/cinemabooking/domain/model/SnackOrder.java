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
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public SnackOrder() {
    }
}