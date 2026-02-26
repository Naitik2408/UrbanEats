package com.urbaneats.exception;

/**
 * Exception thrown when OTP cooldown period is active.
 */
public class OtpCooldownException extends RuntimeException {
    
    private final long remainingSeconds;
    
    public OtpCooldownException(String message, long remainingSeconds) {
        super(message);
        this.remainingSeconds = remainingSeconds;
    }
    
    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}
