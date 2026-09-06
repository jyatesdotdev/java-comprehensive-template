package com.example.template.restfulapi.exception;

import com.example.template.restfulapi.dto.ErrorResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for the REST API.
 *
 * <p>Extends {@link ResponseEntityExceptionHandler} so framework client errors (malformed JSON,
 * type mismatches, 405, 415) stay 4xx instead of being swallowed by a catch-all 500. Domain
 * handlers keep returning {@link ErrorResponse}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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
   * @param headers the current headers
   * @param status the suggested status
   * @param request the current request
   * @return 400 error response with per-field messages
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      @NonNull MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
    var body =
        new ErrorResponse(400, "Validation Failed", "Request body has invalid fields", details);
    return ResponseEntity.badRequest().body(body);
  }

  /**
   * Handles malformed JSON request bodies as 400.
   *
   * @param ex the read failure
   * @param headers the current headers
   * @param status the suggested status
   * @param request the current request
   * @return 400 error response
   */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      @NonNull HttpMessageNotReadableException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {
    var body = new ErrorResponse(400, "Malformed JSON", "Request body could not be parsed");
    return ResponseEntity.badRequest().body(body);
  }

  /**
   * Handles path/query type mismatches (e.g. a non-UUID product id) as 400.
   *
   * @param ex the type mismatch
   * @param headers the current headers
   * @param status the suggested status
   * @param request the current request
   * @return 400 error response
   */
  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      @NonNull TypeMismatchException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {
    String name =
        ex instanceof MethodArgumentTypeMismatchException mismatch
            ? mismatch.getName()
            : ex.getPropertyName();
    log.debug("Type mismatch: {}", ex.getMessage());
    var body = new ErrorResponse(400, "Bad Request", "Invalid value for parameter " + name);
    return ResponseEntity.badRequest().body(body);
  }

  /**
   * Catch-all handler for unexpected exceptions. Returns a 500 response.
   *
   * @param ex the unhandled exception
   * @return 500 error response
   */
  @ExceptionHandler(Exception.class)
  @SuppressWarnings("PMD.InvalidLogMessageFormat") // ex is a Throwable arg, not a format arg
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    log.error("Unhandled exception", ex);
    var body = new ErrorResponse(500, "Internal Server Error", "An unexpected error occurred");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
