package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.*;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
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

    public void updatePaymentStatus(String bookingId, String paymentStatus, String bookingStatus) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(bookingId).set(
                        BookingDTO.builder()
                                .paymentStatus(paymentStatus)
                                .bookingStatus(bookingStatus)
                                .paymentAt("SUCCESS".equalsIgnoreCase(paymentStatus) || "confirmed".equalsIgnoreCase(paymentStatus) ? System.currentTimeMillis() : 0)
                                .updatedAt(System.currentTimeMillis())
                                .build(),
                        SetOptions.mergeFields("paymentStatus", "bookingStatus", "paymentAt", "updatedAt"))
                .get();
    }

    public void confirmBookingSeats(String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = getBookingById(bookingId);
        if (booking == null) {
            logger.error("[PAYMENT_FAILED] Booking not found for ID: {}", bookingId);
            throw new RuntimeException("Booking not found");
        }

        List<String> seatIds = booking.getSeatIds();
        if (seatIds == null || seatIds.isEmpty()) {
            logger.warn("Booking {} has no seat IDs associated", bookingId);
            return;
        }

        long now = System.currentTimeMillis();
        WriteBatch batch = firestore.batch();
        for (String seatId : seatIds) {
            logger.info("[PAYMENT_SUCCESS] Booking {} confirmed. Marking seat {} as booked by {}", bookingId, seatId, booking.getUserId());
            batch.update(firestore.collection("seats").document(seatId),
                    "status", "booked",
                    "bookedBy", booking.getUserId(),
                    "bookedAt", now
            );
        }
        batch.commit().get();
    }

    public void releaseBookingSeats(String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = getBookingById(bookingId);
        if (booking == null) return;

        List<String> seatIds = booking.getSeatIds();
        if (seatIds == null || seatIds.isEmpty()) return;

        WriteBatch batch = firestore.batch();
        for (String seatId : seatIds) {
            logger.info("[PAYMENT_FAILED] Booking {} failed/cancelled. Releasing seat {}", bookingId, seatId);
            batch.update(firestore.collection("seats").document(seatId),
                    "status", "available",
                    "heldBy", null,
                    "heldUntil", 0L
            );
        }
        batch.commit().get();
    }

    public void updateCheckInTime(String bookingId, long checkInAt) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(bookingId).set(
                        BookingDTO.builder()
                                .checkInAt(checkInAt)
                                .updatedAt(System.currentTimeMillis())
                                .build(),
                        com.google.cloud.firestore.SetOptions.mergeFields("checkInAt", "updatedAt"))
                .get();
    }
}
