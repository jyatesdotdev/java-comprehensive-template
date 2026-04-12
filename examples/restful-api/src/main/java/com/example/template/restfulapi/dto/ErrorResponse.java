package com.example.template.restfulapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/**
 * Standardized error response body.
 *
 * <p>Provides a consistent structure for all API error responses,
 * including validation errors.
 *
 * @param status    HTTP status code
 * @param error     error category (e.g. "Not Found")
 * @param message   human-readable message
 * @param details   field-level validation errors
 * @param timestamp when the error occurred
 */
@Schema(description = "Standardized error response")
public record ErrorResponse(
        @Schema(description = "HTTP status code", example = "404") int status,
        @Schema(description = "Error category", example = "Not Found") String error,
        @Schema(description = "Error message", example = "Product not found") String message,
        @Schema(description = "Validation error details") List<String> details,
        @Schema(description = "Error timestamp") Instant timestamp) {

    /**
     * Creates an error response with no field-level details.
     *
     * @param status  HTTP status code
     * @param error   error category
     * @param message human-readable message
     */
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, List.of(), Instant.now());
    }

    /**
     * Creates an error response with field-level details.
     *
     * @param status  HTTP status code
     * @param error   error category
     * @param message human-readable message
     * @param details field-level validation errors
     */
    public ErrorResponse(int status, String error, String message, List<String> details) {
        this(status, error, message, details, Instant.now());
    }
}
