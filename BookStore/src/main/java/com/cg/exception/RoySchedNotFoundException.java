package com.cg.exception;
import com.cg.exception.ResourceNotFoundException;
/**
 * RoySchedNotFoundException — thrown when no royalty schedule exists for a given title.
 *
 * This exception is caught by the GlobalExceptionHandler (written by Shubham/manager).
 * The GlobalExceptionHandler maps this to HTTP 404 Not Found.
 *
 * How to plug this into GlobalExceptionHandler:
 *
 *   @ExceptionHandler(RoySchedNotFoundException.class)
 *   public ResponseEntity<ErrorResponse> handleRoySchedNotFound(RoySchedNotFoundException ex) {
 *       ErrorResponse error = new ErrorResponse(404, ex.getMessage());
 *       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
 *   }
 *
 * Usage in your code (if you ever need to throw manually):
 *   throw new RoySchedNotFoundException("BU1032");
 */
public class RoySchedNotFoundException extends ResourceNotFoundException {

    private final String titleId;

    public RoySchedNotFoundException(String titleId) {
        super("No royalty schedule found for title ID: " + titleId);
        this.titleId = titleId;
    }

    public String getTitleId() {
        return titleId;
    }
}
