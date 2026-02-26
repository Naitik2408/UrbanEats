package com.urbaneats.exception;

/**
 * Exception thrown when invalid variant is selected for an item.
 */
public class InvalidVariantException extends RuntimeException {
    public InvalidVariantException(String message) {
        super(message);
    }
}
