package com.cg.exception;

/**
 * UnauthorizedException — thrown when a user tries to access something
 * they are not allowed to access (HTTP 401).
 *
 * Example usage:
 *   throw new UnauthorizedException("You are not authorized to perform this action");
 *
 * GlobalExceptionHandler catches this and returns HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
