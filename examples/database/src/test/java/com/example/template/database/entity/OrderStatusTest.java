package com.example.template.database.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Guards the {@link OrderStatus} enum: the database stores status as a string
 * ({@code @Enumerated(EnumType.STRING)}), so renaming or reordering constants is a breaking
 * schema-data change.
 */
@DisplayName("OrderStatus enum")
class OrderStatusTest {

  @Test
  @DisplayName("Contains exactly the five lifecycle states, in declaration order")
  void containsExactlyExpectedConstants() {
    assertThat(OrderStatus.values())
        .containsExactly(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED);
  }

  @ParameterizedTest
  @EnumSource(OrderStatus.class)
  @DisplayName("Every constant round-trips through its name (STRING enum mapping)")
  void nameRoundTripsThroughValueOf(OrderStatus status) {
    assertThat(OrderStatus.valueOf(status.name())).isSameAs(status);
  }
}
