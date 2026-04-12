package com.example.template.database.jdbc;

import com.example.template.database.entity.OrderStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Raw JDBC access using Spring's {@link JdbcTemplate} and {@link NamedParameterJdbcTemplate}.
 *
 * <p>Use JDBC when you need:
 * <ul>
 *   <li>Complex SQL that doesn't map well to JPA/JPQL</li>
 *   <li>Bulk operations for performance</li>
 *   <li>Stored procedure calls</li>
 *   <li>Fine-grained control over result set mapping</li>
 * </ul>
 */
@Repository
public class JdbcOrderDao {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final SimpleJdbcInsert orderInsert;

    /**
     * Creates the DAO, initialising JDBC helpers and the {@link SimpleJdbcInsert}.
     *
     * @param jdbc the Spring-managed {@link JdbcTemplate}
     */
    public JdbcOrderDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        this.orderInsert = new SimpleJdbcInsert(jdbc)
                .withTableName("orders")
                .usingGeneratedKeyColumns("id");
    }

    /**
     * Lightweight projection of an order row for summary queries.
     *
     * @param id           the order ID
     * @param customerName the customer name
     * @param status       the order status string
     * @param totalAmount  the order total
     */
    public record OrderSummary(Long id, String customerName, String status, BigDecimal totalAmount) {}

    private static final RowMapper<OrderSummary> ORDER_SUMMARY_MAPPER = (rs, rowNum) ->
            new OrderSummary(
                    rs.getLong("id"),
                    rs.getString("customer_name"),
                    rs.getString("status"),
                    rs.getBigDecimal("total_amount"));

    // --- JdbcTemplate examples ---

    /**
     * Returns a summary of every order, sorted by ID.
     *
     * @return all order summaries
     */
    public List<OrderSummary> findAllSummaries() {
        return jdbc.query(
                "SELECT id, customer_name, status, total_amount FROM orders ORDER BY id",
                ORDER_SUMMARY_MAPPER);
    }

    /**
     * Counts orders with the given status using a positional-parameter query.
     *
     * @param status the status to count
     * @return the number of matching orders
     */
    public int countByStatus(OrderStatus status) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE status = ?",
                Integer.class, status.name());
        return count != null ? count : 0;
    }

    // --- NamedParameterJdbcTemplate examples ---

    /**
     * Finds order summaries whose total is at or above the given amount.
     *
     * @param minAmount the minimum total amount
     * @return matching order summaries
     */
    public List<OrderSummary> findByMinAmount(BigDecimal minAmount) {
        var params = new MapSqlParameterSource("minAmount", minAmount);
        return namedJdbc.query(
                "SELECT id, customer_name, status, total_amount FROM orders WHERE total_amount >= :minAmount",
                params, ORDER_SUMMARY_MAPPER);
    }

    // --- SimpleJdbcInsert example ---

    /**
     * Inserts a new PENDING order using {@link SimpleJdbcInsert} and returns the generated ID.
     *
     * @param customerName the customer name
     * @return the generated order ID
     */
    public long insertOrder(String customerName) {
        Number key = orderInsert.executeAndReturnKey(Map.of(
                "customer_name", customerName,
                "status", OrderStatus.PENDING.name(),
                "total_amount", BigDecimal.ZERO,
                "version", 0,
                "created_at", Instant.now(),
                "updated_at", Instant.now()));
        return key.longValue();
    }

    // --- Batch update ---

    /**
     * Inserts multiple PENDING orders in a single batch statement.
     *
     * @param customerNames the customer names to insert
     * @return an array of update counts, one per statement in the batch
     */
    public int[] batchInsertOrders(List<String> customerNames) {
        return jdbc.batchUpdate(
                "INSERT INTO orders (customer_name, status, total_amount, version, created_at, updated_at) VALUES (?, ?, 0, 0, NOW(), NOW())",
                customerNames.stream()
                        .map(name -> new Object[]{name, OrderStatus.PENDING.name()})
                        .toList());
    }
}
