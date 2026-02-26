package com.urbaneats.exception;

/**
 * Exception thrown when JWT secret is too weak on application startup.
 */
public class WeakJwtSecretException extends RuntimeException {
    
    public WeakJwtSecretException(String message) {
        super(message);
    }
}
