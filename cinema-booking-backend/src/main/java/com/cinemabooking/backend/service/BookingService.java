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
        WriteBatch batch = firestore.batch();

        batch.set(firestore.collection(COLLECTION).document(data.getBookingId()), data);

        DocumentReference showtimeRef = firestore.collection("showtimes").document(data.getShowtimeId());
        batch.update(showtimeRef, "bookedSeatsCount", FieldValue.increment(data.getSeatIds().size()));

        batch.commit().get();
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
        DocumentReference bookingRef = firestore.collection(COLLECTION).document(bookingId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot bookingSnap = transaction.get(bookingRef).get();
            if (!bookingSnap.exists()) {
                logger.error("[PAYMENT_FAILED] Booking not found for ID: {}", bookingId);
                throw new RuntimeException("Booking not found");
            }
            BookingDTO booking = bookingSnap.toObject(BookingDTO.class);
            if (booking == null) {
                throw new RuntimeException("Booking serialization error");
            }

            List<String> seatIds = booking.getSeatIds();
            if (seatIds == null || seatIds.isEmpty()) {
                logger.warn("Booking {} has no seat IDs associated", bookingId);
                return null;
            }

            long now = System.currentTimeMillis();
            for (String seatId : seatIds) {
                DocumentReference seatRef = firestore.collection("seats").document(seatId);
                DocumentSnapshot seatSnap = transaction.get(seatRef).get();
                if (!seatSnap.exists()) {
                    throw new RuntimeException("Seat " + seatId + " not found");
                }
                String status = seatSnap.getString("status");
                String heldBy = seatSnap.getString("heldBy");

                if ("booked".equalsIgnoreCase(status)) {
                    throw new RuntimeException("Ghế " + seatSnap.getString("seatCode") + " đã được đặt trước bởi người khác!");
                }

                if (!"available".equalsIgnoreCase(status) && !booking.getUserId().equals(heldBy)) {
                    throw new RuntimeException("Ghế " + seatSnap.getString("seatCode") + " đang được giữ bởi người khác!");
                }

                logger.info("[PAYMENT_SUCCESS] Booking {} confirmed. Marking seat {} as booked by {}", bookingId, seatId, booking.getUserId());
                transaction.update(seatRef,
                        "status", "booked",
                        "bookedBy", booking.getUserId(),
                        "bookedAt", now
                );
            }
            return null;
        }).get();
    }


    public void releaseBookingSeats(String bookingId) throws ExecutionException, InterruptedException {
        DocumentReference bookingRef = firestore.collection(COLLECTION).document(bookingId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot bookingSnap = transaction.get(bookingRef).get();
            if (!bookingSnap.exists()) return null;
            BookingDTO booking = bookingSnap.toObject(BookingDTO.class);
            if (booking == null) return null;

            List<String> seatIds = booking.getSeatIds();
            if (seatIds == null || seatIds.isEmpty()) return null;

            for (String seatId : seatIds) {
                DocumentReference seatRef = firestore.collection("seats").document(seatId);
                DocumentSnapshot seatSnap = transaction.get(seatRef).get();
                if (seatSnap.exists()) {
                    String status = seatSnap.getString("status");
                    String heldBy = seatSnap.getString("heldBy");
                    String bookedBy = seatSnap.getString("bookedBy");

                    boolean isHeldBySelf = "held".equalsIgnoreCase(status) && booking.getUserId().equals(heldBy);
                    boolean isBookedBySelf = "booked".equalsIgnoreCase(status) && booking.getUserId().equals(bookedBy);

                    if (isHeldBySelf || isBookedBySelf) {
                        logger.info("[RELEASE_SEAT] Booking {} failed/cancelled. Releasing seat {} (previously {})", bookingId, seatId, status);
                        transaction.update(seatRef,
                                "status", "available",
                                "heldBy", null,
                                "heldUntil", 0L,
                                "bookedBy", null,
                                "bookedAt", null
                        );
                    }
                }
            }

            DocumentReference showtimeRef = firestore.collection("showtimes").document(booking.getShowtimeId());
            transaction.update(showtimeRef, "bookedSeatsCount", FieldValue.increment(-seatIds.size()));
            return null;
        }).get();
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
