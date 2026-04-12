package com.example.template.database.repository;

import com.example.template.database.entity.Order;
import com.example.template.database.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository demonstrating:
 * <ul>
 *   <li>Derived query methods (method-name conventions)</li>
 *   <li>JPQL with {@code @Query}</li>
 *   <li>Native SQL queries</li>
 *   <li>Modifying (bulk update) queries</li>
 * </ul>
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    // --- Derived query methods ---

    /**
     * Finds all orders with the given status.
     *
     * @param status the order status to filter by
     * @return matching orders
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds orders whose customer name contains the given string (case-insensitive).
     *
     * @param name the substring to search for
     * @return matching orders
     */
    List<Order> findByCustomerNameContainingIgnoreCase(String name);

    // --- JPQL ---

    /**
     * Finds orders with a total amount at or above the given threshold, sorted descending.
     *
     * @param minAmount minimum total amount
     * @return matching orders, highest amount first
     */
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount ORDER BY o.totalAmount DESC")
    List<Order> findHighValueOrders(@Param("minAmount") BigDecimal minAmount);

    /**
     * Fetches an order by ID with its items eagerly loaded (avoids N+1).
     *
     * @param id the order ID
     * @return the order with items, or {@code null} if not found
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    Order findByIdWithItems(@Param("id") Long id);

    // --- Native SQL ---

    /**
     * Counts orders by status using a native SQL query.
     *
     * @param status the status string (e.g. {@code "PENDING"})
     * @return the count of matching orders
     */
    @Query(value = "SELECT COUNT(*) FROM orders WHERE status = :status", nativeQuery = true)
    long countByStatusNative(@Param("status") String status);

    // --- Bulk update ---

    /**
     * Updates all orders from one status to another in a single statement.
     *
     * @param oldStatus the current status to match
     * @param newStatus the new status to set
     * @return the number of rows updated
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = :newStatus WHERE o.status = :oldStatus")
    int bulkUpdateStatus(@Param("oldStatus") OrderStatus oldStatus,
                         @Param("newStatus") OrderStatus newStatus);
}
