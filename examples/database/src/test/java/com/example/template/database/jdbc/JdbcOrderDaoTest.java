package com.example.template.database.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.template.database.entity.OrderStatus;
import com.example.template.database.jdbc.JdbcOrderDao.OrderSummary;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@code @JdbcTest} slice tests for {@link JdbcOrderDao} against embedded H2 with the Flyway
 * migration applied.
 */
@JdbcTest
@Import(JdbcOrderDao.class)
@DisplayName("JdbcOrderDao (H2 + Flyway)")
class JdbcOrderDaoTest {

  private static final String ALICE = "Alice";
  private static final String BOB = "Bob";

  @Autowired private JdbcOrderDao dao;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("insertOrder returns the generated key and persists a PENDING row")
  void insertOrderReturnsGeneratedKey() {
    long id = dao.insertOrder(ALICE);

    assertThat(id).isPositive();
    List<OrderSummary> summaries = dao.findAllSummaries();
    assertThat(summaries).hasSize(1);
    OrderSummary summary = summaries.get(0);
    assertThat(summary.id()).isEqualTo(id);
    assertThat(summary.customerName()).isEqualTo(ALICE);
    assertThat(summary.status()).isEqualTo(OrderStatus.PENDING.name());
    assertThat(summary.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("findAllSummaries returns every order sorted by ID")
  void findAllSummariesSortsById() {
    long first = dao.insertOrder(ALICE);
    long second = dao.insertOrder(BOB);

    assertThat(dao.findAllSummaries()).extracting(OrderSummary::id).containsExactly(first, second);
  }

  @Test
  @DisplayName("countByStatus counts rows via a positional-parameter query")
  void countByStatusCountsRows() {
    dao.insertOrder(ALICE);
    dao.insertOrder(BOB);

    assertThat(dao.countByStatus(OrderStatus.PENDING)).isEqualTo(2);
    assertThat(dao.countByStatus(OrderStatus.SHIPPED)).isZero();
  }

  @Test
  @DisplayName("findByMinAmount is inclusive of the threshold")
  void findByMinAmountFiltersInclusively() {
    long cheap = dao.insertOrder("Cheap");
    long exact = dao.insertOrder("Exact");
    long rich = dao.insertOrder("Rich");
    setTotalAmount(cheap, "10.00");
    setTotalAmount(exact, "25.00");
    setTotalAmount(rich, "99.99");

    assertThat(dao.findByMinAmount(new BigDecimal("25.00")))
        .extracting(OrderSummary::customerName)
        .containsExactlyInAnyOrder("Exact", "Rich");
  }

  @Test
  @DisplayName("batchInsertOrders inserts one PENDING row per name")
  void batchInsertOrdersInsertsAllRows() {
    int[] counts = dao.batchInsertOrders(List.of(ALICE, BOB, "Carol"));

    assertThat(counts).containsExactly(1, 1, 1);
    assertThat(dao.countByStatus(OrderStatus.PENDING)).isEqualTo(3);
    assertThat(dao.findAllSummaries())
        .extracting(OrderSummary::customerName)
        .containsExactlyInAnyOrder(ALICE, BOB, "Carol");
  }

  private void setTotalAmount(long orderId, String amount) {
    int updated =
        jdbcTemplate.update(
            "UPDATE orders SET total_amount = ? WHERE id = ?", new BigDecimal(amount), orderId);
    assertThat(updated).isEqualTo(1);
  }
}
