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

//    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
//    public void releaseExpiredSeats() {
//        logger.info("Scanning for expired seat holds...");
//        long now = System.currentTimeMillis();
//
//        try {
//            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
//                    .whereEqualTo("status", "held")
//                    .get();
//
//            List<QueryDocumentSnapshot> heldSeats = future.get().getDocuments();
//            WriteBatch batch = firestore.batch();
//            int count = 0;
//
//            for (DocumentSnapshot doc : heldSeats) {
//                Long heldUntilVal = doc.getLong("heldUntil");
//                long heldUntil = heldUntilVal != null ? heldUntilVal : 0L;
//
//                if (heldUntil > 0 && heldUntil < now) {
//                    logger.info("[BOOKING_EXPIRED] Seat hold expired for seatId={}. heldUntil={}, now={}. Releasing seat...",
//                            doc.getId(), heldUntil, now);
//                    batch.update(doc.getReference(),
//                            "status", "available",
//                            "heldBy", null,
//                            "heldUntil", 0L
//                    );
//                    count++;
//                }
//            }
//
//            if (count > 0) {
//                batch.commit().get();
//                logger.info("[SEAT_RELEASE] Batch released {} expired seats", count);
//            }
//        } catch (Exception e) {
//            logger.error("Error during scanning/releasing expired seats: {}", e.getMessage(), e);
//        }
//    }
}
