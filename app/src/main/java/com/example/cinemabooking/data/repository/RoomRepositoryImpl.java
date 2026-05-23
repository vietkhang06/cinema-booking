package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Room;
import com.example.cinemabooking.domain.repository.RoomRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RoomRepositoryImpl implements RoomRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void createRoom(Room room, ResultCallback<Room> callback) {
        DocumentReference roomRef = db.collection(FirestoreCollections.ROOMS).document();
        room.roomId = roomRef.getId();
        long now = System.currentTimeMillis();
        room.createdAt = now;
        room.updatedAt = now;
        room.deleted = false;

        db.runTransaction(transaction -> {
            // Write room
            transaction.set(roomRef, room);

            // Update cinema's roomIds list
            if (room.cinemaId != null) {
                DocumentReference cinemaRef = db.collection(FirestoreCollections.CINEMAS).document(room.cinemaId);
                transaction.update(cinemaRef, "roomIds", FieldValue.arrayUnion(room.roomId));
            }
            return room;
        }).addOnSuccessListener(result -> {
            if (callback != null) callback.onSuccess(result);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onError(e.getMessage());
        });
    }

    @Override
    public void getRoomById(String roomId, ResultCallback<Room> callback) {
        db.collection(FirestoreCollections.ROOMS)
                .document(roomId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Room room = doc.toObject(Room.class);
                        if (room != null) room.roomId = doc.getId();
                        if (callback != null) callback.onSuccess(room);
                    } else {
                        if (callback != null) callback.onError("Không tìm thấy phòng chiếu");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getRoomsByCinemaId(String cinemaId, ResultCallback<List<Room>> callback) {
        db.collection(FirestoreCollections.ROOMS)
                .whereEqualTo("cinemaId", cinemaId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Room> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Room room = doc.toObject(Room.class);
                        if (room != null) {
                            room.roomId = doc.getId();
                            if (!room.deleted) {
                                list.add(room);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getAllRooms(ResultCallback<List<Room>> callback) {
        db.collection(FirestoreCollections.ROOMS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Room> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Room room = doc.toObject(Room.class);
                        if (room != null) {
                            room.roomId = doc.getId();
                            if (!room.deleted) {
                                list.add(room);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void updateRoom(Room room, ResultCallback<Room> callback) {
        if (room.roomId == null) {
            if (callback != null) callback.onError("RoomId is null");
            return;
        }
        room.updatedAt = System.currentTimeMillis();
        db.collection(FirestoreCollections.ROOMS)
                .document(room.roomId)
                .set(room)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(room);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void softDeleteRoom(String roomId, ResultCallback<Void> callback) {
        db.collection(FirestoreCollections.ROOMS)
                .document(roomId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        if (callback != null) callback.onError("Không tìm thấy phòng");
                        return;
                    }
                    Room room = doc.toObject(Room.class);
                    if (room == null) {
                        if (callback != null) callback.onError("Lỗi tải phòng");
                        return;
                    }

                    db.runTransaction(transaction -> {
                        // Soft delete room
                        transaction.update(doc.getReference(), "deleted", true, "updatedAt", System.currentTimeMillis());

                        // Remove from cinema's roomIds list
                        if (room.cinemaId != null) {
                            DocumentReference cinemaRef = db.collection(FirestoreCollections.CINEMAS).document(room.cinemaId);
                            transaction.update(cinemaRef, "roomIds", FieldValue.arrayRemove(roomId));
                        }
                        return null;
                    }).addOnSuccessListener(unused -> {
                        if (callback != null) callback.onSuccess(null);
                    }).addOnFailureListener(e -> {
                        if (callback != null) callback.onError(e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
}
