package com.cg.exception;

/**
 * InvalidRequestException
 *
 * Thrown when a request contains invalid data or violates business rules.
 * This exception is caught by GlobalExceptionHandler and returns 400 Bad Request.
 *
 * Example: Missing required fields, invalid field values, constraint violations.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
