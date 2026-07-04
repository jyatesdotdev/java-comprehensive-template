package com.example.template.restfulapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.template.restfulapi.domain.Product;
import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link InMemoryProductService}: CRUD behaviour, not-found handling, and
 * immutability of the returned product list.
 */
class InMemoryProductServiceTest {

  private static final String WIDGET_NAME = "Widget";
  private static final String WIDGET_DESCRIPTION = "A useful widget";
  private static final BigDecimal WIDGET_PRICE = new BigDecimal("29.99");
  private static final String NOT_FOUND_PREFIX = "Product not found: ";
  private static final String UPDATED_NAME = "Gadget";

  private InMemoryProductService service;

  @BeforeEach
  void setUp() {
    service = new InMemoryProductService();
  }

  private Product createWidget() {
    return service.create(new ProductRequest(WIDGET_NAME, WIDGET_DESCRIPTION, WIDGET_PRICE));
  }

  // ── create ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("create assigns a unique id and timestamps and stores the request fields")
  void createShouldAssignIdAndTimestamps() {
    Product product = createWidget();

    assertThat(product.getId()).isNotNull();
    assertThat(product.getName()).isEqualTo(WIDGET_NAME);
    assertThat(product.getDescription()).isEqualTo(WIDGET_DESCRIPTION);
    assertThat(product.getPrice()).isEqualByComparingTo(WIDGET_PRICE);
    assertThat(product.getCreatedAt()).isNotNull();
    assertThat(product.getUpdatedAt()).isEqualTo(product.getCreatedAt());
  }

  @Test
  @DisplayName("create assigns distinct ids to distinct products")
  void createShouldAssignDistinctIds() {
    Product first = createWidget();
    Product second = createWidget();

    assertThat(first.getId()).isNotEqualTo(second.getId());
    assertThat(service.findAll()).hasSize(2);
  }

  // ── findById ───────────────────────────────────────────────────────

  @Test
  @DisplayName("findById returns the stored product")
  void findByIdShouldReturnStoredProduct() {
    Product created = createWidget();

    Product found = service.findById(created.getId());

    assertThat(found.getId()).isEqualTo(created.getId());
    assertThat(found.getName()).isEqualTo(WIDGET_NAME);
  }

  @Test
  @DisplayName("findById throws ResourceNotFoundException for an unknown id")
  void findByIdShouldThrowWhenMissing() {
    UUID missingId = UUID.randomUUID();

    assertThatThrownBy(() -> service.findById(missingId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(NOT_FOUND_PREFIX + missingId);
  }

  // ── findAll ────────────────────────────────────────────────────────

  @Test
  @DisplayName("findAll returns an empty list when no products exist")
  void findAllShouldReturnEmptyListInitially() {
    assertThat(service.findAll()).isEmpty();
  }

  @Test
  @DisplayName("findAll returns every stored product")
  void findAllShouldReturnAllProducts() {
    Product first = createWidget();
    Product second = createWidget();

    List<Product> all = service.findAll();

    assertThat(all)
        .hasSize(2)
        .extracting(Product::getId)
        .containsExactlyInAnyOrder(first.getId(), second.getId());
  }

  @Test
  @DisplayName("findAll returns an immutable snapshot")
  void findAllShouldReturnImmutableList() {
    Product product = createWidget();
    List<Product> all = service.findAll();

    assertThatThrownBy(() -> all.add(product)).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> all.remove(0)).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @DisplayName("findAll snapshot does not reflect later mutations of the store")
  void findAllSnapshotShouldNotReflectLaterChanges() {
    createWidget();
    List<Product> snapshot = service.findAll();

    createWidget();

    assertThat(snapshot).hasSize(1);
    assertThat(service.findAll()).hasSize(2);
  }

  // ── update ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("update applies request fields and refreshes updatedAt")
  void updateShouldApplyFieldsAndRefreshUpdatedAt() {
    Product created = createWidget();
    var request = new ProductRequest(UPDATED_NAME, "An updated gadget", new BigDecimal("49.99"));

    Product updated = service.update(created.getId(), request);

    assertThat(updated.getId()).isEqualTo(created.getId());
    assertThat(updated.getName()).isEqualTo(UPDATED_NAME);
    assertThat(updated.getDescription()).isEqualTo("An updated gadget");
    assertThat(updated.getPrice()).isEqualByComparingTo("49.99");
    assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());
  }

  @Test
  @DisplayName("update persists the changes so a subsequent findById sees them")
  void updateShouldPersistChanges() {
    Product created = createWidget();
    service.update(created.getId(), new ProductRequest(UPDATED_NAME, null, BigDecimal.ONE));

    Product found = service.findById(created.getId());

    assertThat(found.getName()).isEqualTo(UPDATED_NAME);
    assertThat(found.getDescription()).isNull();
    assertThat(found.getPrice()).isEqualByComparingTo(BigDecimal.ONE);
  }

  @Test
  @DisplayName("update throws ResourceNotFoundException for an unknown id")
  void updateShouldThrowWhenMissing() {
    UUID missingId = UUID.randomUUID();
    var request = new ProductRequest(WIDGET_NAME, WIDGET_DESCRIPTION, WIDGET_PRICE);

    assertThatThrownBy(() -> service.update(missingId, request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(NOT_FOUND_PREFIX + missingId);
  }

  // ── delete ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("delete removes the product from the store")
  void deleteShouldRemoveProduct() {
    Product created = createWidget();
    UUID id = created.getId();

    service.delete(id);

    assertThat(service.findAll()).isEmpty();
    assertThatThrownBy(() -> service.findById(id)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("delete throws ResourceNotFoundException for an unknown id")
  void deleteShouldThrowWhenMissing() {
    UUID missingId = UUID.randomUUID();

    assertThatThrownBy(() -> service.delete(missingId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(NOT_FOUND_PREFIX + missingId);
  }
}
