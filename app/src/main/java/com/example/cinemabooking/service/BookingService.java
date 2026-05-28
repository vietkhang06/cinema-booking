package com.example.cinemabooking.service;

import com.example.cinemabooking.data.repository.BookingRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.domain.repository.BookingRepository;

import java.util.List;

public class BookingService {
    private final BookingRepository bookingRepo;

    public BookingService() {
        this.bookingRepo = new BookingRepositoryImpl();
    }

    public BookingService(BookingRepository bookingRepo) {
        this.bookingRepo = bookingRepo;
    }

    /**
     * Get bookings for the current authenticated user.
     */
    public void getMyBookings(ResultCallback<List<Booking>> callback) {
        // userId is ignored because the backend /my endpoint uses the auth token
        bookingRepo.getBookingsByUserId(null, callback);
    }

    /**
     * Get details of a specific booking.
     */
    public void getBookingDetails(String bookingId, ResultCallback<Booking> callback) {
        bookingRepo.getBookingById(bookingId, callback);
    }
}
