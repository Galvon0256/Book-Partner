package com.cg.exception;

/**
 * ConflictException
 *
 * Thrown when an operation cannot be completed due to a conflict with existing state.
 * This exception is caught by GlobalExceptionHandler and returns 409 Conflict.
 *
 * Example: Attempting to delete a Publisher that has child records (foreign key constraint),
 *          or updating a resource that has been modified by another request.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
