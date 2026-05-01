package com.cg.exception;

/**
 * InvalidPublisherIdException
 *
 * Thrown when a POST / PUT / PATCH supplies a pubId that does not exist
 * in the publishers table. Like InvalidJobIdException, this must be thrown
 * BEFORE the repository save() call so the 400 response is sent before the
 * FK violation reaches MySQL.
 *
 * Caught by GlobalExceptionHandler → returns HTTP 400 Bad Request.
 *
 * Usage:
 *   if (!publisherRepository.existsById(employee.getPubId()))
 *       throw new InvalidPublisherIdException("Publisher with ID " + employee.getPubId() + " does not exist");
 */
public class InvalidPublisherIdException extends RuntimeException {

    public InvalidPublisherIdException(String message) {
        super(message);
    }

    public InvalidPublisherIdException(String message, Throwable cause) {
        super(message, cause);
    }
}