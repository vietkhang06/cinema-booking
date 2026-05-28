package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Seat;
import com.example.cinemabooking.domain.model.SeatTemplate;

import java.util.List;

public interface SeatRepository {
    void createSeatTemplates(String roomId, List<SeatTemplate> templates, ResultCallback<Void> callback);
    void getSeatTemplatesByRoomId(String roomId, ResultCallback<List<SeatTemplate>> callback);
    void generateSeatsForShowtime(String showtimeId, String roomId, ResultCallback<Void> callback);
    void getSeatsByShowtimeId(String showtimeId, ResultCallback<List<Seat>> callback);
    void holdSeat(String showtimeId, String seatId, String userId, long holdUntil, ResultCallback<Seat> callback);
    void releaseSeat(String showtimeId, String seatId, ResultCallback<Seat> callback);
    void bookSeats(String showtimeId, List<String> seatIds, String userId, ResultCallback<List<Seat>> callback);
    void resetSeatsByShowtime(String showtimeId, ResultCallback<Void> callback);
}