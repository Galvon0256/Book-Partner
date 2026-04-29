package com.cg.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — catches ALL exceptions from the entire application.
 *
 * Instead of writing try-catch in every place, we handle exceptions here globally.
 * @RestControllerAdvice means: "apply this to all @RestController classes"
 *
 * Each @ExceptionHandler method returns a ResponseEntity with:
 *   - Proper HTTP status code (400, 401, 404, 500, etc.)
 *   - A clear error message in JSON format
 *   - A timestamp so the client knows when the error happened
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------
    // Helper method: builds a standard error response map
    // So all error responses look the same format
    // -------------------------------------------------------
    private Map<String, Object> buildErrorResponse(int statusCode, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", statusCode);
        body.put("error", error);
        body.put("message", message);
        return body;
    }

    // -------------------------------------------------------
    // 400 BAD REQUEST — Validation failed
    // Triggered when @Valid fails (e.g., blank name, wrong format)
    // -------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {

        // Collect all field error messages into a list
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> body = buildErrorResponse(400, "Validation Failed", "One or more fields are invalid");
        body.put("validationErrors", errors);   // Extra field showing each specific error

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);   // 400
    }

    // -------------------------------------------------------
    // 400 BAD REQUEST — Illegal argument passed
    // -------------------------------------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = buildErrorResponse(400, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);   // 400
    }

    // -------------------------------------------------------
    // 401 UNAUTHORIZED — Custom exception for auth issues
    // -------------------------------------------------------
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        Map<String, Object> body = buildErrorResponse(401, "Unauthorized", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);   // 401
    }

    // -------------------------------------------------------
    // 404 NOT FOUND — Resource doesn't exist
    // Triggered when we throw ResourceNotFoundException manually
    // -------------------------------------------------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }

    // -------------------------------------------------------
    // 404 NOT FOUND — No element found in Optional
    // -------------------------------------------------------
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElement(NoSuchElementException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found", "The requested record was not found");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }

    // -------------------------------------------------------
    // 404 NOT FOUND — No URL handler found
    // Triggered when user calls a URL that doesn't exist
    // -------------------------------------------------------
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandler(NoHandlerFoundException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found",
                "The URL '" + ex.getRequestURL() + "' does not exist");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }

    // -------------------------------------------------------
    // 500 INTERNAL SERVER ERROR — Unexpected errors
    // This is a catch-all for anything we didn't predict
    // -------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = buildErrorResponse(500, "Internal Server Error",
                "Something went wrong on the server. Please try again later.");
        // In production, log ex.getMessage() to your logging system
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);   // 500
    }

    // -------------------------------------------------------
    // 505 HTTP VERSION NOT SUPPORTED — just to show the code
    // Spring rarely throws this but we include it per requirements
    // -------------------------------------------------------
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupported(UnsupportedOperationException ex) {
        Map<String, Object> body = buildErrorResponse(505, "HTTP Version Not Supported", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.HTTP_VERSION_NOT_SUPPORTED);   // 505
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex) {

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());

        Map<String, Object> body = buildErrorResponse(400, "Validation Failed", "One or more fields are invalid");
        body.put("validationErrors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    // Add these handlers in the proper order (between 400 and 401 sections)

    // -------------------------------------------------------
    // 400 BAD REQUEST — Invalid request data
    // -------------------------------------------------------
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidRequestException ex) {
        Map<String, Object> body = buildErrorResponse(400, "Invalid Request", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);   // 400
    }

    // -------------------------------------------------------
    // 400 BAD REQUEST — Bad request parameters
    // -------------------------------------------------------
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        Map<String, Object> body = buildErrorResponse(400, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);   // 400
    }

    // -------------------------------------------------------
    // 409 CONFLICT — Duplicate resource
    // -------------------------------------------------------
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(DuplicateResourceException ex) {
        Map<String, Object> body = buildErrorResponse(409, "Conflict", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);   // 409
    }

    // -------------------------------------------------------
    // 409 CONFLICT — General conflict with existing state
    // -------------------------------------------------------
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        Map<String, Object> body = buildErrorResponse(409, "Conflict", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);   // 409
    }
    
   
    
}
