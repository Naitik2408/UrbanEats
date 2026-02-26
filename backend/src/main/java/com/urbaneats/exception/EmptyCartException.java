package com.urbaneats.exception;

/**
 * Exception thrown when cart is empty during checkout.
 */
public class EmptyCartException extends RuntimeException {
    public EmptyCartException(String message) {
        super(message);
    }
}
