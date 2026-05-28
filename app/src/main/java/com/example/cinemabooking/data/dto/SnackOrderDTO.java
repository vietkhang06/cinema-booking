package com.example.cinemabooking.data.dto;

import java.util.List;

public class SnackOrderDTO {
    public String snackOrderId;
    public String userId;
    public String bookingId;
    public List<SnackOrderItemDTO> items;
    public double subtotal;
    public double discount;
    public double total;
    public String status;
    public String note;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public SnackOrderDTO() {
    }
}