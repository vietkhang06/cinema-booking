package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.UserDTO;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "users";

    public UserDTO getUserById(String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(uid).get().get();
        if (doc.exists()) {
            return mapToDTO(doc);
        }
        return null;
    }

    private UserDTO mapToDTO(DocumentSnapshot doc) {
        return UserDTO.builder()
                .uid(doc.getId())
                .email(doc.getString("email"))
                .phone(doc.getString("phone"))
                .name(doc.getString("name"))
                .birthDate(doc.getString("birthDate"))
                .gender(doc.getString("gender"))
                .avatarUrl(doc.getString("avatarUrl"))
                .role(doc.getString("role"))
                .status(doc.getString("status"))
                .memberLevel(doc.getString("memberLevel"))
                .createdAt(doc.get("createdAt") instanceof Number ? doc.getLong("createdAt") : 0L)
                .updatedAt(doc.get("updatedAt") instanceof Number ? doc.getLong("updatedAt") : 0L)
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .build();
    }
}
