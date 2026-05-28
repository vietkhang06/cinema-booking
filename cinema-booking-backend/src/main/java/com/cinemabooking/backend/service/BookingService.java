package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.*;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class BookingService {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "bookings";

    public BookingDTO getBookingById(String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = firestore.collection(BookingDTO.COLLECTION_NAME).document(bookingId).get()
                .get().toObject(BookingDTO.class);

        return booking;
    }

    public BookingDTO createBooking(
            BookingDTO data
    ) throws ExecutionException, InterruptedException{
        firestore.collection(COLLECTION).document(data.getBookingId()).set(data).get();
        return data;
    }

    public void updatePaymentStatus(String bookingId, String status) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(bookingId).set(
                BookingDTO.builder()
                    .paymentStatus(status)
                    .paymentAt(status.equals("confirmed") ? System.currentTimeMillis() : 0),
                SetOptions.mergeFields("paymentStatus", "paymentAt"))
        .get();
    }
}
