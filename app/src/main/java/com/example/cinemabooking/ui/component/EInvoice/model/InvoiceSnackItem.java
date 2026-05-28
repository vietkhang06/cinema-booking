package com.example.cinemabooking.ui.component.EInvoice.model;


public class InvoiceSnackItem {
    public String itemName;
    public int quantity;
    public int price;
    public InvoiceSnackItem(String itemName, int quantity, int price){
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }
}