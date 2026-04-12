package com.example.template.restfulapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a product.
 *
 * <p>Uses a Java record for immutable, concise DTOs. Validation annotations
 * are applied directly to record components.
 *
 * @param name        product display name
 * @param description optional product description
 * @param price       unit price, must be positive
 */
@Schema(description = "Request payload for creating or updating a product")
public record ProductRequest(
        @Schema(description = "Product name", example = "Widget")
        @NotBlank(message = "Name is required") String name,

        @Schema(description = "Product description", example = "A useful widget")
        String description,

        @Schema(description = "Unit price", example = "29.99")
        @NotNull(message = "Price is required") @Positive(message = "Price must be positive") BigDecimal price) {}
