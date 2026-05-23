package com.example.cinemabooking.domain.model;

public class Cinema {
    public String cinemaId;
    public String name;
    public String address;
    public String city;
    public String district;
    public String phone;
    public double latitude;
    public double longitude;
    public String status;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;
    public java.util.List<String> roomIds;

    public Cinema() {
    }
}