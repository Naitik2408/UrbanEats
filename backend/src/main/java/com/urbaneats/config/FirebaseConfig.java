package com.urbaneats.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Configuration
 * 
 * Initializes Firebase Admin SDK for server-side operations.
 * 
 * Required:
 * - firebase.service-account-path in application.yml
 * - Service account JSON file from Firebase Console
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-path:classpath:firebase-service-account.json}")
    private Resource serviceAccountPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = serviceAccountPath.getInputStream();
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                
                log.info("Firebase Admin SDK initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
            throw new RuntimeException("Firebase initialization failed. Please check service account file.", e);
        }
    }
}
