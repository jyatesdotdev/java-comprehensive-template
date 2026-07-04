package com.example.template.restfulapi.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ErrorResponse} convenience constructors and defaults. */
class ErrorResponseTest {

  private static final String NOT_FOUND = "Not Found";
  private static final String MESSAGE = "Product not found";

  @Test
  @DisplayName("three-arg constructor defaults to empty details and a current timestamp")
  void threeArgConstructorShouldDefaultDetailsAndTimestamp() {
    Instant before = Instant.now();

    var response = new ErrorResponse(404, NOT_FOUND, MESSAGE);

    Instant after = Instant.now();
    assertThat(response.status()).isEqualTo(404);
    assertThat(response.error()).isEqualTo(NOT_FOUND);
    assertThat(response.message()).isEqualTo(MESSAGE);
    assertThat(response.details()).isEmpty();
    assertThat(response.timestamp()).isBetween(before, after);
  }

  @Test
  @DisplayName("four-arg constructor keeps details and defaults the timestamp")
  void fourArgConstructorShouldKeepDetailsAndDefaultTimestamp() {
    Instant before = Instant.now();
    List<String> details = List.of("name: Name is required", "price: Price must be positive");

    var response =
        new ErrorResponse(400, "Validation Failed", "Request body has invalid fields", details);

    Instant after = Instant.now();
    assertThat(response.status()).isEqualTo(400);
    assertThat(response.error()).isEqualTo("Validation Failed");
    assertThat(response.message()).isEqualTo("Request body has invalid fields");
    assertThat(response.details()).containsExactlyElementsOf(details);
    assertThat(response.timestamp()).isBetween(before, after);
  }

  @Test
  @DisplayName("canonical constructor keeps all supplied values unchanged")
  void canonicalConstructorShouldKeepAllValues() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    List<String> details = List.of("field: bad");

    var response = new ErrorResponse(500, "Internal Server Error", "boom", details, timestamp);

    assertThat(response.status()).isEqualTo(500);
    assertThat(response.error()).isEqualTo("Internal Server Error");
    assertThat(response.message()).isEqualTo("boom");
    assertThat(response.details()).containsExactly("field: bad");
    assertThat(response.timestamp()).isEqualTo(timestamp);
  }

  @Test
  @DisplayName("records with equal components are equal")
  void equalComponentsShouldBeEqual() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    var first = new ErrorResponse(404, NOT_FOUND, MESSAGE, List.of(), timestamp);
    var second = new ErrorResponse(404, NOT_FOUND, MESSAGE, List.of(), timestamp);

    assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
  }
}
