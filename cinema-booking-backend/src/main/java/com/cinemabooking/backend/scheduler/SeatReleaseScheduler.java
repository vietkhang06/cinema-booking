package com.cinemabooking.backend.scheduler;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeatReleaseScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SeatReleaseScheduler.class);
    private static final String COLLECTION = "seats";

    @Autowired
    private Firestore firestore;

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void releaseExpiredSeats() {
        logger.info("Scanning for expired seat holds...");
        long now = System.currentTimeMillis();

        try {
            List<QueryDocumentSnapshot> heldSeats = firestore.collection(COLLECTION)
                    .whereEqualTo("status", "held")
                    .get().get().getDocuments();

            for (DocumentSnapshot doc : heldSeats) {
                Long heldUntilVal = doc.getLong("heldUntil");
                long heldUntil = heldUntilVal != null ? heldUntilVal : 0L;

                if (heldUntil > 0 && heldUntil < now) {
                    String seatId = doc.getId();
                    String heldBy = doc.getString("heldBy");
                    logger.info("[BOOKING_EXPIRED] Seat hold expired for seatId={}. heldUntil={}, now={}. Processing cancellation...",
                            seatId, heldUntil, now);

                    // Find PENDING bookings that contain this seat
                    List<QueryDocumentSnapshot> pendingBookings = firestore.collection("bookings")
                            .whereEqualTo("bookingStatus", "PENDING")
                            .whereArrayContains("seatIds", seatId)
                            .get().get().getDocuments();

                    if (!pendingBookings.isEmpty()) {
                        for (QueryDocumentSnapshot bookingDoc : pendingBookings) {
                            String bookingId = bookingDoc.getId();
                            String bookingUserId = bookingDoc.getString("userId");

                            if (bookingUserId != null && bookingUserId.equals(heldBy)) {
                                logger.info("[SCHEDULER_CANCEL] Cancelling expired booking {} for user {}", bookingId, bookingUserId);
                                cancelBookingAndReleaseSeats(bookingDoc, now);
                            }
                        }
                    } else {
                        firestore.collection(COLLECTION).document(seatId)
                                .update("status", "available", "heldBy", null, "heldUntil", 0L)
                                .get();
                        logger.info("[SEAT_RELEASE] Seat {} hold expired with no active booking. Released.", seatId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error during scanning/releasing expired seats: {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void cancelBookingAndReleaseSeats(DocumentSnapshot bookingDoc, long now) {
        String bookingId = bookingDoc.getId();
        String showtimeId = bookingDoc.getString("showtimeId");
        List<String> seatIds = (List<String>) bookingDoc.get("seatIds");

        try {
            firestore.runTransaction(transaction -> {
                transaction.update(bookingDoc.getReference(),
                        "bookingStatus", "CANCELLED",
                        "paymentStatus", "FAILED",
                        "updatedAt", now
                );

                if (seatIds != null) {
                    for (String sId : seatIds) {
                        DocumentReference seatRef = firestore.collection(COLLECTION).document(sId);
                        transaction.update(seatRef,
                                "status", "available",
                                "heldBy", null,
                                "heldUntil", 0L,
                                "bookedBy", null,
                                "bookedAt", null
                        );
                    }
                    if (showtimeId != null) {
                        DocumentReference showtimeRef = firestore.collection("showtimes").document(showtimeId);
                        transaction.update(showtimeRef, "bookedSeatsCount", FieldValue.increment(-seatIds.size()));
                    }
                }

                try {
                    List<QueryDocumentSnapshot> payments = firestore.collection("payments")
                            .whereEqualTo("bookingId", bookingId)
                            .get().get().getDocuments();
                    for (QueryDocumentSnapshot paymentDoc : payments) {
                        transaction.update(paymentDoc.getReference(),
                                "status", "FAILED",
                                "updatedAt", now
                        );
                    }
                } catch (Exception ex) {
                    logger.error("Failed to cancel payments in transaction for booking: {}", bookingId, ex);
                }

                return null;
            }).get();
            logger.info("[SCHEDULER_CANCEL_SUCCESS] Successfully cancelled booking {} and released seats {}", bookingId, seatIds);
        } catch (Exception e) {
            logger.error("Failed to cancel booking {} and release seats: {}", bookingId, e.getMessage(), e);
        }
    }
}
