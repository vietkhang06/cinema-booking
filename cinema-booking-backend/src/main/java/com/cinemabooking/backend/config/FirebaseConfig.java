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

            byte[] decoded = null;
            if (firebaseKey != null && !firebaseKey.trim().isEmpty()) {
                decoded = Base64.getDecoder().decode(firebaseKey.trim());
            } else {
                String envKey = System.getenv("FIREBASE_SERVICE_ACCOUNT_BASE64");
                if (envKey != null && !envKey.trim().isEmpty()) {
                    decoded = Base64.getDecoder().decode(envKey.trim());
                }
            }

            FirebaseOptions options;
            if (decoded != null) {
                logger.info("Initializing Firebase using Base64 encoded credentials");
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(decoded)))
                        .build();
            } else {
                java.io.File file = new java.io.File("firebase-service-account.json");
                if (file.exists()) {
                    logger.info("Initializing Firebase using local credentials file: {}", file.getAbsolutePath());
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(new java.io.FileInputStream(file)))
                            .build();
                } else {
                    try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("firebase-service-account.json")) {
                        if (is != null) {
                            logger.info("Initializing Firebase using classpath resource: firebase-service-account.json");
                            options = FirebaseOptions.builder()
                                    .setCredentials(GoogleCredentials.fromStream(is))
                                    .build();
                        } else {
                            throw new RuntimeException("Firebase credentials not found. Set FIREBASE_SERVICE_ACCOUNT_BASE64 env var or create firebase-service-account.json.");
                        }
                    }
                }
            }

            FirebaseApp app = FirebaseApp.initializeApp(options);
            logger.info("Firebase initialized successfully");
            return app;
        } catch (Exception e) {
            logger.error("Firebase initialization failed", e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
