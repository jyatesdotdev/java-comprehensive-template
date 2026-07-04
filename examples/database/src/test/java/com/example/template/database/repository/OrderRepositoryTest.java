package com.example.template.database.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.template.database.entity.Order;
import com.example.template.database.entity.OrderItem;
import com.example.template.database.entity.OrderStatus;
import java.math.BigDecimal;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * {@code @DataJpaTest} slice tests for {@link OrderRepository} against embedded H2 with the real
 * Flyway migration applied ({@code ddl-auto: validate} — Flyway owns the schema).
 */
@DataJpaTest
@DisplayName("OrderRepository (H2 + Flyway)")
class OrderRepositoryTest {

  private static final String ALICE = "Alice";
  private static final String BOB = "Bob";
  private static final String CAROL = "Carol";

  @Autowired private OrderRepository repository;

  @Autowired private TestEntityManager entityManager;

  private Order persistOrder(String customerName, OrderStatus status, OrderItem... items) {
    var order = new Order();
    order.setCustomerName(customerName);
    order.setStatus(status);
    for (OrderItem item : items) {
      order.addItem(item);
    }
    return entityManager.persistAndFlush(order);
  }

  private static OrderItem item(String product, int quantity, String unitPrice) {
    return new OrderItem(product, quantity, new BigDecimal(unitPrice));
  }

  // --- Schema / lifecycle sanity ---

  @Test
  @DisplayName("Flyway schema accepts a persisted order and populates managed fields")
  void persistPopulatesManagedFields() {
    Order saved = persistOrder(ALICE, OrderStatus.PENDING, item("Widget", 2, "9.99"));

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getVersion()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
    assertThat(saved.getItems()).allSatisfy(i -> assertThat(i.getId()).isNotNull());
  }

  // --- Derived queries ---

  @Test
  @DisplayName("findByStatus returns only orders with the given status")
  void findByStatusFiltersByStatus() {
    persistOrder(ALICE, OrderStatus.PENDING);
    persistOrder(BOB, OrderStatus.SHIPPED);
    persistOrder(CAROL, OrderStatus.PENDING);

    assertThat(repository.findByStatus(OrderStatus.PENDING))
        .hasSize(2)
        .extracting(Order::getCustomerName)
        .containsExactlyInAnyOrder(ALICE, CAROL);
    assertThat(repository.findByStatus(OrderStatus.CANCELLED)).isEmpty();
  }

  @Test
  @DisplayName("findByCustomerNameContainingIgnoreCase matches substrings case-insensitively")
  void findByCustomerNameContainingIgnoreCaseMatches() {
    persistOrder("Alice Smith", OrderStatus.PENDING);
    persistOrder("MALICE Corp", OrderStatus.PENDING);
    persistOrder(BOB, OrderStatus.PENDING);

    assertThat(repository.findByCustomerNameContainingIgnoreCase("alice"))
        .extracting(Order::getCustomerName)
        .containsExactlyInAnyOrder("Alice Smith", "MALICE Corp");
    assertThat(repository.findByCustomerNameContainingIgnoreCase("zzz")).isEmpty();
  }

  // --- JPQL ---

  @Test
  @DisplayName("findHighValueOrders is inclusive of the threshold and sorts descending")
  void findHighValueOrdersFiltersAndSortsDescending() {
    persistOrder("Low", OrderStatus.PENDING, item("Pin", 1, "10.00"));
    persistOrder("Mid", OrderStatus.PENDING, item("Hammer", 1, "20.00"));
    persistOrder("High", OrderStatus.PENDING, item("Drill", 2, "20.00"));

    assertThat(repository.findHighValueOrders(new BigDecimal("20.00")))
        .extracting(Order::getCustomerName)
        .containsExactly("High", "Mid");
  }

  @Test
  @DisplayName("findByIdWithItems eagerly loads the items collection (JOIN FETCH)")
  void findByIdWithItemsLoadsItems() {
    Long id =
        persistOrder(
                ALICE, OrderStatus.PENDING, item("Widget", 2, "9.99"), item("Gadget", 1, "5.50"))
            .getId();
    entityManager.clear();

    Order fetched = repository.findByIdWithItems(id).orElseThrow();

    assertThat(Hibernate.isInitialized(fetched.getItems())).isTrue();
    assertThat(fetched.getItems())
        .extracting(OrderItem::getProductName)
        .containsExactlyInAnyOrder("Widget", "Gadget");
    assertThat(fetched.getTotalAmount()).isEqualByComparingTo("25.48");
  }

  @Test
  @DisplayName("findByIdWithItems returns empty for an unknown ID")
  void findByIdWithItemsReturnsEmptyForUnknownId() {
    assertThat(repository.findByIdWithItems(9999L)).isEmpty();
  }

  @Test
  @DisplayName("findByIdWithItems returns an item-less order with an empty collection (LEFT JOIN)")
  void findByIdWithItemsReturnsItemlessOrder() {
    Long id = persistOrder("Empty", OrderStatus.PENDING).getId();
    entityManager.clear();

    Order fetched = repository.findByIdWithItems(id).orElseThrow();
    assertThat(fetched.getItems()).isEmpty();
  }

  // --- Native SQL ---

  @Test
  @DisplayName("countByStatusNative counts rows via native SQL")
  void countByStatusNativeCountsRows() {
    persistOrder(ALICE, OrderStatus.PENDING);
    persistOrder(BOB, OrderStatus.PENDING);
    persistOrder(CAROL, OrderStatus.DELIVERED);

    assertThat(repository.countByStatusNative("PENDING")).isEqualTo(2);
    assertThat(repository.countByStatusNative("CANCELLED")).isZero();
  }

  // --- @Modifying bulk update ---

  @Test
  @DisplayName("bulkUpdateStatus updates matching rows and returns the count")
  void bulkUpdateStatusUpdatesMatchingRows() {
    persistOrder(ALICE, OrderStatus.PENDING);
    persistOrder(BOB, OrderStatus.PENDING);
    persistOrder(CAROL, OrderStatus.SHIPPED);

    int updated = repository.bulkUpdateStatus(OrderStatus.PENDING, OrderStatus.CONFIRMED);
    // Bulk JPQL bypasses the persistence context — clear it before re-reading.
    entityManager.clear();

    assertThat(updated).isEqualTo(2);
    assertThat(repository.findByStatus(OrderStatus.PENDING)).isEmpty();
    assertThat(repository.findByStatus(OrderStatus.CONFIRMED))
        .extracting(Order::getCustomerName)
        .containsExactlyInAnyOrder(ALICE, BOB);
    assertThat(repository.findByStatus(OrderStatus.SHIPPED)).hasSize(1);
  }

  @Test
  @DisplayName("bulkUpdateStatus returns zero when nothing matches")
  void bulkUpdateStatusReturnsZeroWhenNothingMatches() {
    persistOrder(ALICE, OrderStatus.DELIVERED);

    assertThat(repository.bulkUpdateStatus(OrderStatus.PENDING, OrderStatus.CONFIRMED)).isZero();
  }
}
