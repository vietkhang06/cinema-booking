package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.CinemaDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class CinemaService {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "cinemas";

    public List<CinemaDTO> getAllCinemas() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<CinemaDTO> list = new ArrayList<>();

        for (DocumentSnapshot doc : documents) {
            Boolean deleted = doc.getBoolean("deleted");
            if (deleted != null && deleted) continue;

            list.add(mapToDTO(doc));
        }
        return list;
    }

    public CinemaDTO getCinemaById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
        if (doc.exists()) {
            return mapToDTO(doc);
        }
        return null;
    }

    private CinemaDTO mapToDTO(DocumentSnapshot doc) {
        return CinemaDTO.builder()
                .cinemaId(doc.getId())
                .name(doc.getString("name"))
                .address(doc.getString("address"))
                .city(doc.getString("city"))
                .district(doc.getString("district"))
                .phone(doc.getString("phone"))
                .status(doc.getString("status"))
                .latitude(doc.getDouble("latitude"))
                .longitude(doc.getDouble("longitude"))
                .createdAt(doc.getLong("createdAt"))
                .updatedAt(doc.getLong("updatedAt"))
                .deleted(doc.getBoolean("deleted"))
                .build();
    }
}
