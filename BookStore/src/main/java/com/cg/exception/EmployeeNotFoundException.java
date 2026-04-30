package com.cg.exception;
 
/**
 * EmployeeNotFoundException
 *
 * Thrown when an employee record cannot be found by the given emp_id.
 * Caught by GlobalExceptionHandler → returns HTTP 404 Not Found.
 *
 * Usage in a service or Spring Data REST event handler:
 *   throw new EmployeeNotFoundException("Employee with ID 'PMA42628M' not found");
 */
public class EmployeeNotFoundException extends RuntimeException {
 
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}