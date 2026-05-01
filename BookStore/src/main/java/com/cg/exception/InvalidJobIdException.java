package com.cg.exception;

/**
 * InvalidJobIdException
 *
 * Thrown when a POST / PUT / PATCH supplies a jobId that does not exist
 * in the jobs table. This must be thrown BEFORE the repository save() call
 * (e.g. in a service layer or a Spring Data REST @HandleBeforeCreate /
 * @HandleBeforeSave event handler) so that the HTTP response is sent
 * before the transaction commits and hits the MySQL FK constraint.
 *
 * Caught by GlobalExceptionHandler → returns HTTP 400 Bad Request.
 *
 * Usage:
 *   if (!jobRepository.existsById(employee.getJobId()))
 *       throw new InvalidJobIdException("Job with ID " + employee.getJobId() + " does not exist");
 */
public class InvalidJobIdException extends RuntimeException {

    public InvalidJobIdException(String message) {
        super(message);
    }

    public InvalidJobIdException(String message, Throwable cause) {
        super(message, cause);
    }
}