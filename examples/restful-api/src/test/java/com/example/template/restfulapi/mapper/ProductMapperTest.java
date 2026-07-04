package com.example.template.restfulapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.template.restfulapi.domain.Product;
import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.dto.ProductResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProductMapper}: entity-to-DTO mapping and entity updates. */
class ProductMapperTest {

  private static final String WIDGET_NAME = "Widget";
  private static final String WIDGET_DESCRIPTION = "A useful widget";
  private static final BigDecimal WIDGET_PRICE = new BigDecimal("29.99");

  // ── toResponse ─────────────────────────────────────────────────────

  @Test
  @DisplayName("toResponse copies every entity field into the DTO")
  void toResponseShouldMapAllFields() {
    UUID id = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
    Instant updatedAt = Instant.parse("2026-01-02T12:30:00Z");
    Product product = new Product(id, WIDGET_NAME, WIDGET_DESCRIPTION, WIDGET_PRICE);
    product.setCreatedAt(createdAt);
    product.setUpdatedAt(updatedAt);

    ProductResponse response = ProductMapper.toResponse(product);

    assertThat(response.id()).isEqualTo(id);
    assertThat(response.name()).isEqualTo(WIDGET_NAME);
    assertThat(response.description()).isEqualTo(WIDGET_DESCRIPTION);
    assertThat(response.price()).isEqualByComparingTo(WIDGET_PRICE);
    assertThat(response.createdAt()).isEqualTo(createdAt);
    assertThat(response.updatedAt()).isEqualTo(updatedAt);
  }

  @Test
  @DisplayName("toResponse preserves a null description")
  void toResponseShouldPreserveNullDescription() {
    Product product = new Product(UUID.randomUUID(), WIDGET_NAME, null, WIDGET_PRICE);

    ProductResponse response = ProductMapper.toResponse(product);

    assertThat(response.description()).isNull();
    assertThat(response.name()).isEqualTo(WIDGET_NAME);
  }

  // ── updateEntity ───────────────────────────────────────────────────

  @Test
  @DisplayName("updateEntity applies name, description and price from the request")
  void updateEntityShouldApplyRequestFields() {
    Product product = new Product(UUID.randomUUID(), WIDGET_NAME, WIDGET_DESCRIPTION, WIDGET_PRICE);
    var request = new ProductRequest("Gadget", "An updated gadget", new BigDecimal("49.99"));

    ProductMapper.updateEntity(product, request);

    assertThat(product.getName()).isEqualTo("Gadget");
    assertThat(product.getDescription()).isEqualTo("An updated gadget");
    assertThat(product.getPrice()).isEqualByComparingTo("49.99");
  }

  @Test
  @DisplayName("updateEntity leaves id and timestamps untouched")
  void updateEntityShouldNotTouchIdOrTimestamps() {
    UUID id = UUID.randomUUID();
    Product product = new Product(id, WIDGET_NAME, WIDGET_DESCRIPTION, WIDGET_PRICE);
    Instant createdAt = product.getCreatedAt();
    Instant updatedAt = product.getUpdatedAt();

    ProductMapper.updateEntity(product, new ProductRequest("Gadget", null, BigDecimal.TEN));

    assertThat(product.getId()).isEqualTo(id);
    assertThat(product.getCreatedAt()).isEqualTo(createdAt);
    assertThat(product.getUpdatedAt()).isEqualTo(updatedAt);
  }

  @Test
  @DisplayName("updateEntity overwrites the description with null when the request omits it")
  void updateEntityShouldOverwriteDescriptionWithNull() {
    Product product = new Product(UUID.randomUUID(), WIDGET_NAME, WIDGET_DESCRIPTION, WIDGET_PRICE);

    ProductMapper.updateEntity(product, new ProductRequest(WIDGET_NAME, null, WIDGET_PRICE));

    assertThat(product.getDescription()).isNull();
  }
}
