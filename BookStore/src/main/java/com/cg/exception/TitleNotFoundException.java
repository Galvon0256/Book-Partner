package com.cg.exception;
import com.cg.exception.ResourceNotFoundException;
/**
 * TitleNotFoundException — thrown when a title with the given ID does not exist.
 *
 * This exception is caught by the GlobalExceptionHandler (written by Shubham/manager).
 * The GlobalExceptionHandler maps this to HTTP 404 Not Found.
 *
 * How to plug this into GlobalExceptionHandler:
 *
 *   @ExceptionHandler(TitleNotFoundException.class)
 *   public ResponseEntity<ErrorResponse> handleTitleNotFound(TitleNotFoundException ex) {
 *       ErrorResponse error = new ErrorResponse(404, ex.getMessage());
 *       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
 *   }
 *
 * Usage in your code (if you ever need to throw manually):
 *   throw new TitleNotFoundException("BU1032");
 */
public class TitleNotFoundException extends ResourceNotFoundException {


    public TitleNotFoundException(String titleId) {
        super("Title not found with ID: " + titleId);
    }


}
