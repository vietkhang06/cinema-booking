package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Room;

import java.util.List;

public interface RoomRepository {
    void createRoom(Room room, ResultCallback<Room> callback);
    void getRoomById(String roomId, ResultCallback<Room> callback);
    void getRoomsByCinemaId(String cinemaId, ResultCallback<List<Room>> callback);
    void getAllRooms(ResultCallback<List<Room>> callback);
    void updateRoom(Room room, ResultCallback<Room> callback);
    void softDeleteRoom(String roomId, ResultCallback<Void> callback);
}