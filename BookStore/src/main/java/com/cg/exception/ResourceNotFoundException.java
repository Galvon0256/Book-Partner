package com.cg.exception;

/**
 * ResourceNotFoundException — thrown when a record is not found in the database.
 *
 * Example usage:
 *   throw new ResourceNotFoundException("Author with ID 123-45-6789 not found");
 *
 * GlobalExceptionHandler catches this and returns HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
