package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.SeatDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private static final Logger logger = LoggerFactory.getLogger(SeatService.class);
    private static final String COLLECTION = "seats";

    @Autowired
    private Firestore firestore;

    public List<SeatDTO> getSeatsByShowtimeId(String showtimeId) throws ExecutionException, InterruptedException {
        logger.info("Fetching seats for showtimeId: {}", showtimeId);
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                .whereEqualTo("showtimeId", showtimeId)
                .get();
        List<SeatDTO> seats = future.get().getDocuments().stream()
                .map(this::mapToDTO)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
                
        if (seats.isEmpty()) {
            logger.info("No seats found for showtimeId: {}. Attempting dynamic generation...", showtimeId);
            seats = generateAndSaveSeatsForShowtime(showtimeId);
        }
        
        logger.info("Loaded {} seats for showtime {}", seats.size(), showtimeId);
        return seats;
    }

    private List<SeatDTO> generateAndSaveSeatsForShowtime(String showtimeId) throws ExecutionException, InterruptedException {
        DocumentSnapshot showtimeDoc = firestore.collection("showtimes").document(showtimeId).get().get();
        if (!showtimeDoc.exists()) {
            logger.warn("Showtime {} does not exist in database.", showtimeId);
            return new ArrayList<>();
        }
        
        String roomId = showtimeDoc.getString("roomId");
        if (roomId == null || roomId.isEmpty()) {
            logger.warn("Showtime {} has no roomId associated.", showtimeId);
            return new ArrayList<>();
        }
        
        // Fetch room to get rows/cols
        DocumentSnapshot roomDoc = firestore.collection("rooms").document(roomId).get().get();
        int roomRows = 6;
        int roomCols = 12;
        if (roomDoc.exists()) {
            Long rowsVal = roomDoc.getLong("seatRows");
            Long colsVal = roomDoc.getLong("seatCols");
            if (rowsVal != null) roomRows = rowsVal.intValue();
            if (colsVal != null) roomCols = colsVal.intValue();
        }
        
        // Fetch seat templates
        ApiFuture<QuerySnapshot> templatesFuture = firestore.collection("seat_templates")
                .whereEqualTo("roomId", roomId)
                .get();
        List<QueryDocumentSnapshot> templatesDocs = templatesFuture.get().getDocuments();
        
        List<SeatDTO> generatedSeats = new ArrayList<>();
        WriteBatch batch = firestore.batch();
        
        if (!templatesDocs.isEmpty()) {
            logger.info("Found {} templates for room {}. Instantiating showtime seats from templates...", templatesDocs.size(), roomId);
            for (DocumentSnapshot doc : templatesDocs) {
                Boolean isEnabledVal = doc.getBoolean("isEnabled");
                boolean isEnabled = isEnabledVal != null ? isEnabledVal : true;
                
                if (isEnabled) {
                    String seatCode = doc.getString("seatCode");
                    String rowName = doc.getString("rowName");
                    Long colNoVal = doc.getLong("columnNo");
                    int colNo = colNoVal != null ? colNoVal.intValue() : 0;
                    String seatType = doc.getString("seatType");
                    
                    SeatDTO seat = SeatDTO.builder()
                            .seatId(showtimeId + "_" + seatCode)
                            .showtimeId(showtimeId)
                            .seatCode(seatCode)
                            .rowName(rowName)
                            .columnNo(colNo)
                            .seatType(seatType != null ? seatType : "STANDARD")
                            .status("available")
                            .heldBy(null)
                            .heldUntil(0L)
                            .bookedBy(null)
                            .bookedAt(0L)
                            .priceOverride(0.0)
                            .build();
                            
                    generatedSeats.add(seat);
                    batch.set(firestore.collection(COLLECTION).document(seat.getSeatId()), seat);
                }
            }
        } else {
            logger.info("No templates found for room {}. Creating default seat grid of {}x{}...", roomId, roomRows, roomCols);
            for (int r = 0; r < roomRows; r++) {
                String rowName = String.valueOf((char) ('A' + r));
                for (int c = 1; c <= roomCols; c++) {
                    String seatCode = rowName + String.format(java.util.Locale.getDefault(), "%02d", c);
                    String seatType = (rowName.equals("C") || rowName.equals("D")) ? "VIP" : "STANDARD";
                    
                    SeatDTO seat = SeatDTO.builder()
                            .seatId(showtimeId + "_" + seatCode)
                            .showtimeId(showtimeId)
                            .seatCode(seatCode)
                            .rowName(rowName)
                            .columnNo(c)
                            .seatType(seatType)
                            .status("available")
                            .heldBy(null)
                            .heldUntil(0L)
                            .bookedBy(null)
                            .bookedAt(0L)
                            .priceOverride(0.0)
                            .build();
                            
                    generatedSeats.add(seat);
                    batch.set(firestore.collection(COLLECTION).document(seat.getSeatId()), seat);
                }
            }
        }
        
        if (!generatedSeats.isEmpty()) {
            batch.commit().get();
            logger.info("Atomically created and saved {} seats for showtime {}", generatedSeats.size(), showtimeId);
        }
        
        return generatedSeats;
    }

    public void lockSeats(String userId, String showtimeId, List<String> seatIds) throws ExecutionException, InterruptedException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        firestore.runTransaction(transaction -> {
            long now = System.currentTimeMillis();
            long holdUntil = now + (7 * 60 * 1000); // 7 minutes
            
            List<DocumentReference> refs = new ArrayList<>();
            for (String seatId : seatIds) {
                DocumentReference ref = firestore.collection(COLLECTION).document(seatId);
                refs.add(ref);
            }
            
            // Read all docs in transaction
            List<DocumentSnapshot> snapshots = transaction.getAll(refs.toArray(new DocumentReference[0])).get();
            
            for (DocumentSnapshot doc : snapshots) {
                if (!doc.exists()) {
                    throw new RuntimeException("Seat " + doc.getId() + " does not exist");
                }
                
                String status = doc.getString("status");
                String heldBy = doc.getString("heldBy");
                Long heldUntilVal = doc.getLong("heldUntil");
                long heldUntil = heldUntilVal != null ? heldUntilVal : 0L;
                
                // Concurrency checks
                if ("booked".equalsIgnoreCase(status)) {
                    logger.warn("[SEAT_CONFLICT] Seat {} is already booked", doc.getId());
                    throw new RuntimeException("Seat " + doc.getId() + " is already booked");
                }
                
                if ("held".equalsIgnoreCase(status) && heldUntil > now && !userId.equals(heldBy)) {
                    logger.warn("[SEAT_CONFLICT] Seat {} is held by another user {} until {}", doc.getId(), heldBy, heldUntil);
                    throw new RuntimeException("Seat " + doc.getId() + " is held by another user");
                }
            }
            
            // If all checks pass, write update
            for (DocumentReference ref : refs) {
                logger.info("[SEAT_LOCK] Locking seat {} for user {} until {}", ref.getId(), userId, holdUntil);
                transaction.update(ref, 
                    "status", "held",
                    "heldBy", userId,
                    "heldUntil", holdUntil
                );
            }
            
            return null;
        }).get();
    }

    public void releaseSeats(String userId, String showtimeId, List<String> seatIds) throws ExecutionException, InterruptedException {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        firestore.runTransaction(transaction -> {
            List<DocumentReference> refs = new ArrayList<>();
            for (String seatId : seatIds) {
                DocumentReference ref = firestore.collection(COLLECTION).document(seatId);
                refs.add(ref);
            }
            
            List<DocumentSnapshot> snapshots = transaction.getAll(refs.toArray(new DocumentReference[0])).get();
            
            for (DocumentSnapshot doc : snapshots) {
                if (!doc.exists()) continue;
                
                String status = doc.getString("status");
                String heldBy = doc.getString("heldBy");
                
                // Only release if held by this user and not booked
                if ("held".equalsIgnoreCase(status) && userId.equals(heldBy)) {
                    logger.info("[SEAT_RELEASE] Releasing seat {} held by user {}", doc.getId(), userId);
                    transaction.update(doc.getReference(), 
                        "status", "available",
                        "heldBy", null,
                        "heldUntil", 0L
                    );
                }
            }
            return null;
        }).get();
    }

    public void releaseSeatsByStaff(String showtimeId, List<String> seatIds) throws ExecutionException, InterruptedException {
        firestore.runTransaction(transaction -> {
            List<DocumentReference> refs = new ArrayList<>();
            for (String seatId : seatIds) {
                DocumentReference ref = firestore.collection(COLLECTION).document(seatId);
                refs.add(ref);
            }
            
            List<DocumentSnapshot> snapshots = transaction.getAll(refs.toArray(new DocumentReference[0])).get();
            
            for (DocumentSnapshot doc : snapshots) {
                if (!doc.exists()) continue;
                
                String status = doc.getString("status");
                
                if ("held".equalsIgnoreCase(status)) {
                    logger.info("[STAFF_SEAT_RELEASE] Staff releasing seat {} held by {}", doc.getId(), doc.getString("heldBy"));
                    transaction.update(doc.getReference(), 
                        "status", "available",
                        "heldBy", null,
                        "heldUntil", 0L
                    );
                }
            }
            return null;
        }).get();
    }

    private SeatDTO mapToDTO(DocumentSnapshot doc) {
        try {
            return SeatDTO.builder()
                    .seatId(doc.getId())
                    .showtimeId(doc.getString("showtimeId"))
                    .seatCode(doc.getString("seatCode"))
                    .rowName(doc.getString("rowName"))
                    .columnNo(doc.getLong("columnNo") != null ? doc.getLong("columnNo").intValue() : 0)
                    .seatType(doc.getString("seatType"))
                    .status(doc.getString("status"))
                    .heldBy(doc.getString("heldBy"))
                    .heldUntil(doc.getLong("heldUntil") != null ? doc.getLong("heldUntil") : 0L)
                    .bookedBy(doc.getString("bookedBy"))
                    .bookedAt(doc.getLong("bookedAt") != null ? doc.getLong("bookedAt") : 0L)
                    .priceOverride(doc.getDouble("priceOverride") != null ? doc.getDouble("priceOverride") : 0.0)
                    .build();
        } catch (Exception e) {
            logger.warn("Error mapping seat doc {}: {}", doc.getId(), e.getMessage());
            return null;
        }
    }
}
