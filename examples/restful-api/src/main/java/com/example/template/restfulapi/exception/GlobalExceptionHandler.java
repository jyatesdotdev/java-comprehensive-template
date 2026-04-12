package com.example.template.restfulapi.exception;

import com.example.template.restfulapi.dto.ErrorResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the REST API.
 *
 * <p>Translates exceptions into consistent {@link ErrorResponse} bodies.
 * This keeps controllers clean — they throw exceptions and this class
 * handles the HTTP response mapping.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles resource-not-found exceptions and returns a 404 response.
     *
     * @param ex the not-found exception
     * @return 404 error response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.debug("Resource not found: {}", ex.getMessage());
        var body = new ErrorResponse(404, "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Handles bean validation failures and returns a 400 response with field-level details.
     *
     * @param ex the validation exception
     * @return 400 error response with per-field messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        var body = new ErrorResponse(400, "Validation Failed", "Request body has invalid fields", details);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Catch-all handler for unexpected exceptions. Returns a 500 response.
     *
     * @param ex the unhandled exception
     * @return 500 error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        var body = new ErrorResponse(500, "Internal Server Error", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
