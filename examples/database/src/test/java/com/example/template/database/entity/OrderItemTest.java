package com.example.template.database.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Unit tests for {@link OrderItem} — construction and line-total arithmetic. */
@DisplayName("OrderItem entity")
class OrderItemTest {

  private static final String WIDGET = "Widget";

  @Test
  @DisplayName("Convenience constructor stores product, quantity, and unit price")
  void constructorStoresValues() {
    var item = new OrderItem(WIDGET, 3, new BigDecimal("4.25"));

    assertThat(item.getProductName()).isEqualTo(WIDGET);
    assertThat(item.getQuantity()).isEqualTo(3);
    assertThat(item.getUnitPrice()).isEqualByComparingTo("4.25");
    assertThat(item.getId()).isNull();
    assertThat(item.getOrder()).isNull();
  }

  @ParameterizedTest(name = "{0} × {1} = {2}")
  @CsvSource({"1, 9.99, 9.99", "3, 4.25, 12.75", "0, 4.25, 0.00", "100, 0.01, 1.00"})
  @DisplayName("getLineTotal multiplies unit price by quantity")
  void lineTotalMultipliesUnitPriceByQuantity(int quantity, String unitPrice, String expected) {
    var item = new OrderItem(WIDGET, quantity, new BigDecimal(unitPrice));

    assertThat(item.getLineTotal()).isEqualByComparingTo(expected);
  }

  @Test
  @DisplayName("getLineTotal reflects setter changes")
  void lineTotalReflectsSetterChanges() {
    var item = new OrderItem(WIDGET, 1, new BigDecimal("2.00"));

    item.setQuantity(5);
    item.setUnitPrice(new BigDecimal("3.00"));

    assertThat(item.getLineTotal()).isEqualByComparingTo("15.00");
  }
}
