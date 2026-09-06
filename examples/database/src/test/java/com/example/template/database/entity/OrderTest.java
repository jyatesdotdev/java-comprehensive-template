package com.example.template.database.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Order} — bidirectional relationship helpers, total recalculation, and JPA
 * lifecycle callbacks (invoked directly, no persistence context required).
 */
@DisplayName("Order entity")
class OrderTest {

  private static final String WIDGET = "Widget";

  @Test
  @DisplayName("A new order defaults to PENDING with a zero total and no items")
  void newOrderHasSensibleDefaults() {
    var order = new Order();

    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(order.getItems()).isEmpty();
    assertThat(order.getId()).isNull();
    assertThat(order.getVersion()).isNull();
    assertThat(order.getCreatedAt()).isNull();
    assertThat(order.getUpdatedAt()).isNull();
  }

  @Test
  @DisplayName("addItem adds the item, sets the back-reference, and recalculates the total")
  void addItemMaintainsBothSidesAndTotal() {
    var order = new Order();
    var item = new OrderItem(WIDGET, 2, new BigDecimal("9.99"));

    order.addItem(item);

    assertThat(order.getItems()).containsExactly(item);
    assertThat(item.getOrder()).isSameAs(order);
    assertThat(order.getTotalAmount()).isEqualByComparingTo("19.98");
  }

  @Test
  @DisplayName("addItem accumulates the total across multiple items")
  void addItemAccumulatesTotal() {
    var order = new Order();
    order.addItem(new OrderItem(WIDGET, 2, new BigDecimal("9.99")));
    order.addItem(new OrderItem("Gadget", 1, new BigDecimal("5.50")));

    assertThat(order.getItems()).hasSize(2);
    assertThat(order.getTotalAmount()).isEqualByComparingTo("25.48");
  }

  @Test
  @DisplayName("removeItem removes the item, clears the back-reference, and recalculates")
  void removeItemMaintainsBothSidesAndTotal() {
    var order = new Order();
    var keep = new OrderItem(WIDGET, 2, new BigDecimal("9.99"));
    var remove = new OrderItem("Gadget", 1, new BigDecimal("5.50"));
    order.addItem(keep);
    order.addItem(remove);

    order.removeItem(remove);

    assertThat(order.getItems()).containsExactly(keep);
    assertThat(remove.getOrder()).isNull();
    assertThat(order.getTotalAmount()).isEqualByComparingTo("19.98");
  }

  @Test
  @DisplayName("Removing the last item resets the total to zero")
  void removeLastItemResetsTotalToZero() {
    var order = new Order();
    var item = new OrderItem(WIDGET, 3, new BigDecimal("4.00"));
    order.addItem(item);

    order.removeItem(item);

    assertThat(order.getItems()).isEmpty();
    assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("getItems returns an unmodifiable view so callers must use addItem")
  void getItemsIsUnmodifiable() {
    var order = new Order();
    var item = new OrderItem(WIDGET, 1, BigDecimal.ONE);

    assertThatThrownBy(() -> order.getItems().add(item))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(order.getItems()).isEmpty();
  }

  @Test
  @DisplayName("@PrePersist callback stamps createdAt and updatedAt with the same instant")
  void prePersistStampsBothTimestamps() {
    var order = new Order();

    order.onCreate();

    assertThat(order.getCreatedAt()).isNotNull();
    assertThat(order.getUpdatedAt()).isNotNull();
    assertThat(order.getUpdatedAt()).isEqualTo(order.getCreatedAt());
  }

  @Test
  @DisplayName("@PreUpdate callback advances updatedAt but never touches createdAt")
  void preUpdateAdvancesUpdatedAtOnly() {
    var order = new Order();
    order.onCreate();
    var created = order.getCreatedAt();

    order.onUpdate();

    assertThat(order.getCreatedAt()).isEqualTo(created);
    assertThat(order.getUpdatedAt()).isNotNull();
    assertThat(order.getUpdatedAt()).isAfterOrEqualTo(created);
  }
}
