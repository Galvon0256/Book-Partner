package com.cg.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.core.JsonParseException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
    // 400 BAD REQUEST — Jakarta validation constraint violation
    // Triggered by Spring Data REST validation when @NotBlank, @Size, @Pattern etc. fail
    // -------------------------------------------------------
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

    // -------------------------------------------------------
    // 400 BAD REQUEST — Spring Data REST repository constraint violation
    // Triggered when Spring Data REST validation fails on entity creation/update
    // -------------------------------------------------------
    @ExceptionHandler(RepositoryConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleRepositoryConstraintViolation(
            RepositoryConstraintViolationException ex) {

        List<String> errors = ex.getErrors()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        // If no field errors, check for global errors
        if (errors.isEmpty()) {
            errors = ex.getErrors()
                    .getGlobalErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
        }

        Map<String, Object> body = buildErrorResponse(400, "Validation Failed", "One or more fields are invalid");
        body.put("validationErrors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
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
    // 404 NOT FOUND - Spring Data REST resource doesn't exist
    // Triggered when repository endpoints cannot find a resource by ID
    // -------------------------------------------------------
    @ExceptionHandler(org.springframework.data.rest.webmvc.ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDataRestNotFound(
            org.springframework.data.rest.webmvc.ResourceNotFoundException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found", "The requested record was not found");
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
    // 404 NOT FOUND - No static/resource handler found
    // Triggered for unmapped repository routes after methods are hidden
    // -------------------------------------------------------
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found",
                "The requested URL does not exist");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }

    // -------------------------------------------------------
    // 405 METHOD NOT ALLOWED - HTTP method is not supported
    // Triggered when Spring Data REST delete methods are not exported
    // -------------------------------------------------------
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        Map<String, Object> body = buildErrorResponse(405, "Method Not Allowed", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);   // 405
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
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDBException(DataIntegrityViolationException ex) {
        String message = "Invalid input: violates database constraints";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorType = "Bad Request";

        // Check the cause to determine the type of constraint violation
        Throwable cause = ex.getCause();
        if (cause != null) {
            String causeMsg = cause.getMessage();
            if (causeMsg != null) {
                causeMsg = causeMsg.toLowerCase();
                
                // Check for duplicate key violation
                if (causeMsg.contains("duplicate") || causeMsg.contains("unique")) {
                    message = "Duplicate record: This value already exists in the database";
                    errorType = "Duplicate Record";
                }
                // Check for foreign key constraint violation
                else if (causeMsg.contains("foreign key") || causeMsg.contains("fk_") || causeMsg.contains("constraint")) {
                    message = "Invalid foreign key: Referenced record does not exist";
                    errorType = "Foreign Key Violation";
                }
                // Check for null constraint violation
                else if (causeMsg.contains("not null") || causeMsg.contains("cannot be null")) {
                    message = "Required field is missing or null";
                    errorType = "Null Constraint Violation";
                }
                // Check for validation constraint violations
                else if (causeMsg.contains("validation") || causeMsg.contains("invalid")) {
                    message = "One or more fields contain invalid data";
                    errorType = "Validation Error";
                }
            }

            // Print the actual cause for debugging
            ex.printStackTrace();
        }

        Map<String, Object> body = buildErrorResponse(400, errorType, message);
        return new ResponseEntity<>(body, status);
    }

    // -------------------------------------------------------
    // 404 NOT FOUND — Title not found by ID
    // -------------------------------------------------------
    @ExceptionHandler(TitleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTitleNotFound(TitleNotFoundException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }

    // -------------------------------------------------------
    // 404 NOT FOUND — Royalty schedule not found for a title
    // -------------------------------------------------------
    @ExceptionHandler(RoySchedNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoySchedNotFound(RoySchedNotFoundException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }

    // -------------------------------------------------------
    // 404 NOT FOUND — Store not found by ID
    // -------------------------------------------------------
    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleStoreNotFound(StoreNotFoundException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }

    // -------------------------------------------------------
    // 404 NOT FOUND — No discounts found for a store
    // -------------------------------------------------------
    @ExceptionHandler(DiscountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDiscountNotFound(DiscountNotFoundException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }

    // -------------------------------------------------------
    // 404 NOT FOUND — No sales records found
    // -------------------------------------------------------
    @ExceptionHandler(SalesNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSalesNotFound(SalesNotFoundException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }

    // -------------------------------------------------------
    // 400 BAD REQUEST — Invalid store data
    // -------------------------------------------------------
    @ExceptionHandler(InvalidStoreDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStoreData(InvalidStoreDataException ex) {
        Map<String, Object> body = buildErrorResponse(400, "Invalid Store Data", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);   // 400
    }

    // -------------------------------------------------------
    // 400 BAD REQUEST — Invalid title data
    // -------------------------------------------------------
    @ExceptionHandler(InvalidTitleDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTitleData(InvalidTitleDataException ex) {
        Map<String, Object> body = buildErrorResponse(400, "Invalid Title Data", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);   // 400
    }

    
    // -------------------------------------------------------
    // 400 BAD REQUEST — jobId does not exist in the jobs table
    // Thrown by the RepositoryEventHandler before save, so the FK
    // violation never reaches the database layer.
    // -------------------------------------------------------

    
    @ExceptionHandler(InvalidJobIdException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJobId(InvalidJobIdException ex) {
        Map<String, Object> body = buildErrorResponse(400, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);   // 400
    }
    
    
    // -------------------------------------------------------
    // 400 BAD REQUEST — pubId does not exist in the publishers table
    // Thrown by the RepositoryEventHandler before save, so the FK
    // violation never reaches the database layer.
    // -------------------------------------------------------
    @ExceptionHandler(InvalidPublisherIdException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPublisherId(InvalidPublisherIdException ex) {
        Map<String, Object> body = buildErrorResponse(400, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);   // 400
    }
    
    // -------------------------------------------------------
    // 404 NOT FOUND — Employee not found by emp_id
    // Thrown by the RepositoryEventHandler or service layer when
    // GET / PATCH targets an emp_id that does not exist.
    // -------------------------------------------------------
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        Map<String, Object> body = buildErrorResponse(404, "Not Found", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(HttpMessageNotReadableException ex) {
        Map<String, Object> body = buildErrorResponse(400, "Bad Request", 
            "Invalid JSON: please check your request body for missing or malformed values");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(InvalidJobLevelException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJobLevel(InvalidJobLevelException ex) {
        Map<String, Object> body = buildErrorResponse(400, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


}
