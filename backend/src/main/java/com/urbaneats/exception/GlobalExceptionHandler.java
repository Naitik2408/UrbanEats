package com.urbaneats.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler for UrbanEats application.
 * Handles all exceptions and returns structured ProblemDetail responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles EntityNotFoundException.
     * Returns 404 Not Found with problem details.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Entity Not Found");
        problemDetail.setType(URI.create("https://urbaneats.com/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Handles validation errors from @Valid annotations.
     * Returns 400 Bad Request with field-level validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed: " + errors
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://urbaneats.com/errors/validation"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errors", ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage()
                )));
        return problemDetail;
    }

    /**
     * Handles all other uncaught exceptions.
     * Returns 500 Internal Server Error without exposing stacktrace.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://urbaneats.com/errors/internal"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
