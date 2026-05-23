package com.cinemabooking.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-base64}")
    private String firebaseKey;

    @Bean
    public FirebaseApp firebaseApp() {

        try {

            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            byte[] decoded =
                    Base64.getDecoder().decode(firebaseKey);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(
                                    new ByteArrayInputStream(decoded)
                            )
                    )
                    .build();

            FirebaseApp app =
                    FirebaseApp.initializeApp(options);

            logger.info("Firebase initialized successfully");

            return app;

        } catch (Exception e) {

            logger.error("Firebase initialization failed", e);

            throw new RuntimeException(
                    "Failed to initialize Firebase",
                    e
            );
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
