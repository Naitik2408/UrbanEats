package com.urbaneats.exception;

/**
 * Exception thrown when order cancellation window has expired.
 */
public class CancellationWindowExpiredException extends RuntimeException {
    public CancellationWindowExpiredException(String message) {
        super(message);
    }
}
