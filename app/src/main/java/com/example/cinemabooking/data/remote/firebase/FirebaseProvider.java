package com.example.cinemabooking.data.remote.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public final class FirebaseProvider {

    private FirebaseProvider() {
    }

    public static FirebaseAuth provideAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseFirestore provideFirestore() {
        return FirebaseFirestore.getInstance();
    }

    public static FirebaseStorage provideStorage() {
        return FirebaseStorage.getInstance();
    }
}