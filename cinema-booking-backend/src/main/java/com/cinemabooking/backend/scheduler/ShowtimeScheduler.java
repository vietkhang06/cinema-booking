package com.cinemabooking.backend.scheduler;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ShowtimeScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ShowtimeScheduler.class);
    private static final String SCHEDULE_COLLECTION = "showtime_schedules";
    private static final String SHOWTIME_COLLECTION = "showtimes";

    @Autowired
    private Firestore firestore;

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void executeSchedules() {
        logger.info("Scanning for pending showtime schedules...");
        long now = System.currentTimeMillis();

        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(SCHEDULE_COLLECTION)
                    .whereEqualTo("executed", false)
                    .get();

            List<QueryDocumentSnapshot> schedules = future.get().getDocuments();
            WriteBatch batch = firestore.batch();
            int count = 0;

            for (DocumentSnapshot doc : schedules) {
                Long startAtVal = doc.getLong("startAt");
                long startAt = startAtVal != null ? startAtVal : 0L;

                Boolean deletedVal = doc.getBoolean("deleted");
                boolean deleted = deletedVal != null ? deletedVal : false;

                if (deleted) {
                    // If marked as deleted, flag as executed so we don't scan it again
                    batch.update(doc.getReference(), "executed", true, "updatedAt", now);
                    count++;
                    continue;
                }

                if (startAt > 0 && now >= startAt) {
                    String scheduleId = doc.getId();
                    logger.info("[SCHEDULE_ACTIVATE] Activating schedule ID={}, startAt={}, now={}",
                            scheduleId, startAt, now);

                    // Map to Showtime fields
                    Map<String, Object> showtimeData = new HashMap<>();
                    showtimeData.put("showtimeId", scheduleId);
                    showtimeData.put("movieId", doc.getString("movieId"));
                    showtimeData.put("cinemaId", doc.getString("cinemaId"));
                    showtimeData.put("roomId", doc.getString("roomId"));
                    showtimeData.put("startAt", startAt);
                    showtimeData.put("endAt", doc.getLong("endAt"));
                    showtimeData.put("basePrice", doc.getDouble("basePrice"));
                    showtimeData.put("format", doc.getString("format"));
                    showtimeData.put("language", doc.getString("language"));
                    showtimeData.put("status", "active");
                    showtimeData.put("isScheduled", false);

                    Long totalSeatsVal = doc.getLong("totalSeats");
                    showtimeData.put("totalSeats", totalSeatsVal != null ? totalSeatsVal.intValue() : 64);
                    showtimeData.put("bookedSeatsCount", 0);
                    showtimeData.put("deleted", false);
                    showtimeData.put("createdAt", now);
                    showtimeData.put("updatedAt", now);

                    // Write showtime
                    DocumentReference showtimeRef = firestore.collection(SHOWTIME_COLLECTION).document(scheduleId);
                    batch.set(showtimeRef, showtimeData);

                    // Update schedule
                    batch.update(doc.getReference(),
                            "executed", true,
                            "executedAt", now,
                            "updatedAt", now
                    );
                    count++;
                }
            }

            if (count > 0) {
                batch.commit().get();
                logger.info("[SCHEDULE_PROCESS] Processed and committed {} schedules", count);
            }
        } catch (Exception e) {
            logger.error("Error during scanning/processing showtime schedules: {}", e.getMessage(), e);
        }
    }
}
