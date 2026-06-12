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
    public Long createdAt;
    public Long updatedAt;
    public Boolean deleted;

    public SnackOrderDTO() {
    }
}