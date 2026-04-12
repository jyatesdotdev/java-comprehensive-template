package com.example.template.restfulapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for product data. Immutable record keeps API responses clean.
 *
 * @param id          unique product identifier
 * @param name        product display name
 * @param description product description
 * @param price       unit price
 * @param createdAt   creation timestamp (UTC)
 * @param updatedAt   last update timestamp (UTC)
 */
@Schema(description = "Product response payload")
public record ProductResponse(
        @Schema(description = "Unique product ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Product name", example = "Widget")
        String name,

        @Schema(description = "Product description", example = "A useful widget")
        String description,

        @Schema(description = "Unit price", example = "29.99")
        BigDecimal price,

        @Schema(description = "Creation timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt) { }
