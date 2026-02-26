package com.urbaneats.exception;

/**
 * Exception thrown when invalid addon is selected for an item.
 */
public class InvalidAddonException extends RuntimeException {
    public InvalidAddonException(String message) {
        super(message);
    }
}
