package com.cg.exception;
import com.cg.exception.ResourceNotFoundException;
/**
 * InvalidTitleDataException — thrown when the request body for a title
 * fails validation (e.g. missing required fields, bad format).
 *
 * This exception is caught by the GlobalExceptionHandler (written by Shubham/manager).
 * The GlobalExceptionHandler maps this to HTTP 400 Bad Request.
 *
 * How to plug this into GlobalExceptionHandler:
 *
 *   @ExceptionHandler(InvalidTitleDataException.class)
 *   public ResponseEntity<ErrorResponse> handleInvalidTitleData(InvalidTitleDataException ex) {
 *       ErrorResponse error = new ErrorResponse(400, ex.getMessage());
 *       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
 *   }
 *
 *   // Also handle Spring's own MethodArgumentNotValidException for @Valid failures:
 *   @ExceptionHandler(MethodArgumentNotValidException.class)
 *   public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
 *       String msg = ex.getBindingResult().getFieldErrors().stream()
 *                      .map(e -> e.getField() + ": " + e.getDefaultMessage())
 *                      .collect(Collectors.joining(", "));
 *       return ResponseEntity.status(HttpStatus.BAD_REQUEST)
 *                            .body(new ErrorResponse(400, msg));
 *   }
 */
public class InvalidTitleDataException extends ResourceNotFoundException {

    public InvalidTitleDataException(String message) {
        super(message);
    }
}
