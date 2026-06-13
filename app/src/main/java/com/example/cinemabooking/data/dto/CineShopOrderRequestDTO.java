package com.example.cinemabooking.data.dto;

public class CineShopOrderRequestDTO {
    private String itemName;
    private String itemImageUrl;
    private int quantity;
    private double totalPrice;
    private String paymentMethod;

    public CineShopOrderRequestDTO(String itemName, String itemImageUrl, int quantity, double totalPrice, String paymentMethod) {
        this.itemName = itemName;
        this.itemImageUrl = itemImageUrl;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemImageUrl() {
        return itemImageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
