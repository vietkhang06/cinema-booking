package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.data.dto.SeatDTO;
import com.example.cinemabooking.data.dto.SeatTemplateDTO;
import com.example.cinemabooking.data.mapper.SeatMapper;
import com.example.cinemabooking.data.mapper.SeatTemplateMapper;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Seat;
import com.example.cinemabooking.domain.model.SeatTemplate;
import com.example.cinemabooking.domain.repository.SeatRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class SeatRepositoryImpl implements SeatRepository {

    private final FirebaseFirestore firestore;

    public SeatRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void createSeatTemplates(String roomId, List<SeatTemplate> templates, ResultCallback<Void> callback) {
        WriteBatch batch = firestore.batch();
        for (SeatTemplate template : templates) {
            SeatTemplateDTO dto = SeatTemplateMapper.toDTO(template);
            String docId = roomId + "_" + template.seatCode;
            batch.set(firestore.collection("seat_templates").document(docId), dto);
        }
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getSeatTemplatesByRoomId(String roomId, ResultCallback<List<SeatTemplate>> callback) {
        firestore.collection("seat_templates")
                .whereEqualTo("roomId", roomId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SeatTemplate> list = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        SeatTemplateDTO dto = doc.toObject(SeatTemplateDTO.class);
                        if (dto != null) {
                            list.add(SeatTemplateMapper.toDomain(dto));
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void generateSeatsForShowtime(String showtimeId, String roomId, ResultCallback<Void> callback) {
        getSeatTemplatesByRoomId(roomId, new ResultCallback<List<SeatTemplate>>() {
            @Override
            public void onSuccess(List<SeatTemplate> templates) {
                WriteBatch batch = firestore.batch();
                for (SeatTemplate t : templates) {
                    if (t.isEnabled) {
                        SeatDTO dto = new SeatDTO();
                        dto.seatId = showtimeId + "_" + t.seatCode;
                        dto.showtimeId = showtimeId;
                        dto.seatCode = t.seatCode;
                        dto.rowName = t.rowName;
                        dto.columnNo = t.columnNo;
                        dto.seatType = t.seatType;
                        dto.status = "available";
                        dto.priceOverride = 0.0;
                        batch.set(firestore.collection("seats").document(dto.seatId), dto);
                    }
                }
                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onSuccess(null);
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onError(e.getMessage());
                        });
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onError(message);
            }
        });
    }

    @Override
    public void getSeatsByShowtimeId(String showtimeId, ResultCallback<List<Seat>> callback) {
        firestore.collection("seats")
                .whereEqualTo("showtimeId", showtimeId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Seat> list = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        SeatDTO dto = doc.toObject(SeatDTO.class);
                        if (dto != null) {
                            dto.seatId = doc.getId();
                            list.add(SeatMapper.toDomain(dto));
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void holdSeat(String showtimeId, String seatId, String userId, long holdUntil, ResultCallback<Seat> callback) {
        String docId = seatId;
        firestore.collection("seats").document(docId)
                .update("status", "held", "heldBy", userId, "heldUntil", holdUntil)
                .addOnSuccessListener(aVoid -> {
                    firestore.collection("seats").document(docId).get()
                            .addOnSuccessListener(doc -> {
                                SeatDTO dto = doc.toObject(SeatDTO.class);
                                if (dto != null && callback != null) {
                                    callback.onSuccess(SeatMapper.toDomain(dto));
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void releaseSeat(String showtimeId, String seatId, ResultCallback<Seat> callback) {
        String docId = seatId;
        firestore.collection("seats").document(docId)
                .update("status", "available", "heldBy", null, "heldUntil", 0)
                .addOnSuccessListener(aVoid -> {
                    firestore.collection("seats").document(docId).get()
                            .addOnSuccessListener(doc -> {
                                SeatDTO dto = doc.toObject(SeatDTO.class);
                                if (dto != null && callback != null) {
                                    callback.onSuccess(SeatMapper.toDomain(dto));
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void bookSeats(String showtimeId, List<String> seatIds, String userId, ResultCallback<List<Seat>> callback) {
        WriteBatch batch = firestore.batch();
        for (String id : seatIds) {
            batch.update(firestore.collection("seats").document(id),
                    "status", "booked", "bookedBy", userId, "bookedAt", System.currentTimeMillis());
        }
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    getSeatsByShowtimeId(showtimeId, callback);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void resetSeatsByShowtime(String showtimeId, ResultCallback<Void> callback) {
        firestore.collection("seats")
                .whereEqualTo("showtimeId", showtimeId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    WriteBatch batch = firestore.batch();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.update(doc.getReference(), "status", "available", "heldBy", null, "heldUntil", 0, "bookedBy", null, "bookedAt", 0);
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
}
