package com.example.cinemabooking.domain.model;

public class Room {
    public String roomId;
    public String cinemaId;
    public String name;
    public String layoutType;
    public int seatRows;
    public int seatCols;
    public int totalSeats;
    public String status;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public Room() {
    }
}