package com.urbaneats.service;

import com.urbaneats.exception.OtpAttemptsExceededException;
import com.urbaneats.exception.OtpCooldownException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * Service for OTP generation, storage, and verification using Redis.
 * OTPs are stored with automatic expiration for security.
 * Implements brute force protection and resend cooldown.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${otp.expiration:120}")
    private long otpExpirationSeconds;

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${otp.cooldown:30}")
    private int cooldownSeconds;

    @Value("${otp.return-in-response:true}")
    private boolean returnInResponse;

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_ATTEMPTS_KEY_PREFIX = "otp:attempts:";
    private static final String OTP_COOLDOWN_KEY_PREFIX = "otp:cooldown:";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a random OTP and store it in Redis.
     * Enforces cooldown period between OTP requests.
     * 
     * @param identifier the email or phone number
     * @return the generated OTP (for development only), or null in production
     */
    public String generateAndStoreOtp(String identifier) {
        // Check cooldown
        String cooldownKey = OTP_COOLDOWN_KEY_PREFIX + identifier;
        Long ttl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
        
        if (ttl != null && ttl > 0) {
            if (log.isDebugEnabled()) {
                log.debug("OTP generation blocked due to cooldown for identifier: {}", identifier);
            }
            throw new OtpCooldownException("Please wait before requesting another OTP", ttl.intValue());
        }

        // Generate and store OTP
        String otp = generateOtp();
        String key = OTP_KEY_PREFIX + identifier;

        // Store OTP in Redis with expiration
        redisTemplate.opsForValue().set(key, otp, otpExpirationSeconds, TimeUnit.SECONDS);
        
        // Set cooldown
        redisTemplate.opsForValue().set(cooldownKey, "1", cooldownSeconds, TimeUnit.SECONDS);
        
        // Reset attempts on new OTP generation
        String attemptsKey = OTP_ATTEMPTS_KEY_PREFIX + identifier;
        redisTemplate.delete(attemptsKey);
        
        if (log.isInfoEnabled()) {
            log.info("OTP generated for identifier: {} (expires in {} seconds)", identifier, otpExpirationSeconds);
        }
        
        // In production, never return OTP (send via email/SMS instead)
        // For development, return it in the response
        return returnInResponse ? otp : null;
    }

    /**
     * Verify the provided OTP against the stored value.
     * Implements brute force protection with attempt tracking.
     *
     * @param identifier the email or phone number
     * @param otp the OTP to verify
     * @return true if OTP is valid, false otherwise
     * @throws OtpAttemptsExceededException if max attempts exceeded
     */
    public boolean verifyOtp(String identifier, String otp) {
        String key = OTP_KEY_PREFIX + identifier;
        String attemptsKey = OTP_ATTEMPTS_KEY_PREFIX + identifier;

        // Check if max attempts exceeded
        Integer attempts = (Integer) redisTemplate.opsForValue().get(attemptsKey);
        if (attempts != null && attempts >= maxAttempts) {
            if (log.isWarnEnabled()) {
                log.warn("Maximum OTP verification attempts exceeded for identifier: {}", identifier);
            }
            throw new OtpAttemptsExceededException("Maximum OTP verification attempts exceeded. Please request a new OTP.");
        }

        String storedOtp = (String) redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            if (log.isDebugEnabled()) {
                log.debug("OTP not found or expired for identifier: {}", identifier);
            }
            return false;
        }

        boolean isValid = storedOtp.equals(otp);
        
        if (isValid) {
            // Delete OTP and reset attempts after successful verification
            redisTemplate.delete(key);
            redisTemplate.delete(attemptsKey);
            
            if (log.isInfoEnabled()) {
                log.info("OTP verified successfully for identifier: {}", identifier);
            }
        } else {
            // Increment failed attempts
            incrementAttempts(identifier, attemptsKey);
            
            if (log.isWarnEnabled()) {
                int currentAttempts = attempts != null ? attempts + 1 : 1;
                log.warn("Invalid OTP attempt ({}/{}) for identifier: {}", currentAttempts, maxAttempts, identifier);
            }
        }

        return isValid;
    }

    /**
     * Increment failed OTP verification attempts.
     * Attempts counter expires with the OTP TTL.
     *
     * @param identifier the email or phone number
     * @param attemptsKey the Redis key for attempts tracking
     */
    private void incrementAttempts(String identifier, String attemptsKey) {
        Long increment = redisTemplate.opsForValue().increment(attemptsKey);
        
        // Set expiration to match OTP expiration (only on first increment)
        if (increment != null && increment == 1) {
            redisTemplate.expire(attemptsKey, otpExpirationSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Generate a random numeric OTP.
     * Never logs the actual OTP value.
     *
     * @return the generated OTP as a string
     */
    private String generateOtp() {
        int max = (int) Math.pow(10, otpLength) - 1;
        int min = (int) Math.pow(10, otpLength - 1);
        int otp = random.nextInt(max - min + 1) + min;
        return String.valueOf(otp);
    }

    /**
     * Delete OTP from Redis (useful for testing or manual cleanup).
     *
     * @param identifier the email or phone number
     */
    public void deleteOtp(String identifier) {
        String key = OTP_KEY_PREFIX + identifier;
        String attemptsKey = OTP_ATTEMPTS_KEY_PREFIX + identifier;
        String cooldownKey = OTP_COOLDOWN_KEY_PREFIX + identifier;
        
        redisTemplate.delete(key);
        redisTemplate.delete(attemptsKey);
        redisTemplate.delete(cooldownKey);
        
        if (log.isInfoEnabled()) {
            log.info("OTP data cleared for identifier: {}", identifier);
        }
    }
}
