package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;

import java.util.List;

public interface BookingRepository {
    void createBooking(Booking booking, ResultCallback<Booking> callback);
    void getBookingById(String bookingId, ResultCallback<Booking> callback);
    void getBookingsByUserId(String userId, ResultCallback<List<Booking>> callback);
    void getBookingsByShowtimeId(String showtimeId, ResultCallback<List<Booking>> callback);
    void getAllBookings(ResultCallback<List<Booking>> callback);
    void updateBookingStatus(String bookingId, String status, ResultCallback<Booking> callback);
    void cancelBooking(String bookingId, ResultCallback<Booking> callback);
    void softDeleteBooking(String bookingId, ResultCallback<Void> callback);
}