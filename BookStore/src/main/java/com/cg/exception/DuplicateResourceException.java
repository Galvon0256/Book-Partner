package com.cg.exception;

/**
 * DuplicateResourceException
 *
 * Thrown when attempting to create a resource with a duplicate primary key or unique constraint.
 * This exception is caught by GlobalExceptionHandler and returns 409 Conflict.
 *
 * Example: Creating a Publisher with pub_id that already exists.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
