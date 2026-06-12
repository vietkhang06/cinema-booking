package com.example.cinemabooking.domain.model;

public class Room {
    public String roomId;
    public String cinemaId;
    public String name;
    public String layoutType;
    public Integer seatRows;
    public Integer seatCols;
    public Integer totalSeats;
    public String status;
    public Long createdAt;
    public Long updatedAt;
    public Boolean deleted;

    public Room() {
    }
}