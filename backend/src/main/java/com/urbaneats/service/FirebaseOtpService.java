package com.urbaneats.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Firebase OTP Service
 * 
 * Handles OTP generation and verification using Firebase Phone Authentication.
 * 
 * Note: Firebase Phone Auth is primarily client-side. This service provides
 * backend verification of Firebase custom tokens.
 */
@Slf4j
@Service
public class FirebaseOtpService {

    // In-memory storage for OTP verification (for development)
    // In production, use Redis or database
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    
    /**
     * Generate and store OTP for a phone number
     * 
     * Note: Firebase Phone Auth sends OTP directly from client.
     * This method is for backend-initiated OTP if needed.
     * 
     * @param phoneNumber Phone number in E.164 format (e.g., +919876543210)
     * @return Generated OTP
     */
    public String generateOtp(String phoneNumber) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        
        // Store OTP with 5-minute expiry
        otpStorage.put(phoneNumber, otp);
        
        log.info("OTP generated for phone: {}", maskPhone(phoneNumber));
        
        // TODO: Send OTP via SMS provider (Twilio, SNS, etc.)
        // For now, return OTP for development purposes
        return otp;
    }
    
    /**
     * Verify OTP for a phone number
     * 
     * @param phoneNumber Phone number
     * @param otp OTP to verify
     * @return true if OTP is valid
     */
    public boolean verifyOtp(String phoneNumber, String otp) {
        String storedOtp = otpStorage.get(phoneNumber);
        
        if (storedOtp == null) {
            log.warn("No OTP found for phone: {}", maskPhone(phoneNumber));
            return false;
        }
        
        boolean isValid = storedOtp.equals(otp);
        
        if (isValid) {
            // Remove OTP after successful verification
            otpStorage.remove(phoneNumber);
            log.info("OTP verified successfully for phone: {}", maskPhone(phoneNumber));
        } else {
            log.warn("Invalid OTP for phone: {}", maskPhone(phoneNumber));
        }
        
        return isValid;
    }
    
    /**
     * Verify Firebase ID Token
     * 
     * When using Firebase Phone Auth from client, verify the ID token
     * 
     * @param idToken Firebase ID token from client
     * @return Phone number from token (UID for phone auth)
     * @throws FirebaseAuthException if token is invalid
     */
    public String verifyFirebaseToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        
        // For phone authentication, the UID is the phone number
        // Or we can get it from custom claims if set during token creation
        String uid = decodedToken.getUid();
        
        // Try to get phone number from custom claims first
        String phoneNumber = (String) decodedToken.getClaims().get("phone_number");
        
        // If not in claims, check if UID is a phone number format
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            // Firebase Phone Auth stores the phone number in 'firebase.identities.phone'
            Map<String, Object> firebaseClaim = (Map<String, Object>) decodedToken.getClaims().get("firebase");
            if (firebaseClaim != null) {
                Map<String, Object> identities = (Map<String, Object>) firebaseClaim.get("identities");
                if (identities != null && identities.containsKey("phone")) {
                    Object phoneArray = identities.get("phone");
                    if (phoneArray instanceof java.util.List && !((java.util.List<?>) phoneArray).isEmpty()) {
                        phoneNumber = (String) ((java.util.List<?>) phoneArray).get(0);
                    }
                }
            }
        }
        
        // Fallback to UID if still null
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneNumber = uid;
        }
        
        log.info("Firebase token verified for UID: {}, Phone: {}", uid, maskPhone(phoneNumber));
        
        return phoneNumber;
    }
    
    /**
     * Create custom token for phone number
     * 
     * @param phoneNumber Phone number
     * @return Custom token
     * @throws FirebaseAuthException if token creation fails
     */
    public String createCustomToken(String phoneNumber) throws FirebaseAuthException {
        // Create custom claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("phone", phoneNumber);
        
        String customToken = FirebaseAuth.getInstance().createCustomToken(phoneNumber, claims);
        
        log.info("Custom token created for phone: {}", maskPhone(phoneNumber));
        
        return customToken;
    }
    
    /**
     * Mask phone number for logging
     */
    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }
}
