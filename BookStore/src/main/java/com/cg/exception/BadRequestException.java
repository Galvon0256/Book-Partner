package com.cg.exception;

/**
 * BadRequestException
 *
 * Thrown when the request is malformed or contains invalid parameters.
 * This exception is caught by GlobalExceptionHandler and returns 400 Bad Request.
 *
 * Example: Invalid query parameters, malformed JSON, missing required parameters.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
