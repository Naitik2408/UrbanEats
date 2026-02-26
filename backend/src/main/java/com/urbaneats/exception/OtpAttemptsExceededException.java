package com.urbaneats.exception;

/**
 * Exception thrown when OTP verification attempts are exceeded.
 */
public class OtpAttemptsExceededException extends RuntimeException {
    
    public OtpAttemptsExceededException(String message) {
        super(message);
    }
}
