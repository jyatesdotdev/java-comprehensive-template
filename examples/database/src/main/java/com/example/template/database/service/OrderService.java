package com.example.template.database.service;

import com.example.template.database.entity.Order;
import com.example.template.database.entity.OrderItem;
import com.example.template.database.entity.OrderStatus;
import com.example.template.database.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service demonstrating Spring transaction management:
 *
 * <ul>
 *   <li>{@code @Transactional} — declarative transaction boundaries
 *   <li>Propagation levels (REQUIRED, REQUIRES_NEW)
 *   <li>Isolation levels
 *   <li>Read-only transactions for queries
 *   <li>Rollback rules
 * </ul>
 */
@Service
@Transactional(readOnly = true) // default: read-only for all methods
public class OrderService {

  private static final Logger log = LoggerFactory.getLogger(OrderService.class);

  private final OrderRepository orderRepository;

  /**
   * Creates the service with the given repository.
   *
   * @param orderRepository the order JPA repository
   */
  public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  // --- Read operations (inherit class-level readOnly = true) ---

  /**
   * Finds an order by ID or throws if not found.
   *
   * @param id the order ID
   * @return the matching order
   * @throws IllegalArgumentException if no order exists with the given ID
   */
  public Order findById(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
  }

  /**
   * Finds an order by ID with its items eagerly fetched, or throws if not found.
   *
   * @param id the order ID
   * @return the order with items loaded
   * @throws IllegalArgumentException if no order exists with the given ID
   */
  public Order findByIdWithItems(Long id) {
    return orderRepository
        .findByIdWithItems(id)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
  }

  /**
   * Finds all orders with the given status.
   *
   * @param status the status to filter by
   * @return matching orders
   */
  public List<Order> findByStatus(OrderStatus status) {
    return orderRepository.findByStatus(status);
  }

  // --- Write operations (override readOnly) ---

  /**
   * Creates a new order for the given customer with the supplied items.
   *
   * @param customerName the customer name
   * @param items the line items to add
   * @return the persisted order
   */
  @Transactional // readOnly = false (default), propagation = REQUIRED (default)
  public Order createOrder(String customerName, List<OrderItem> items) {
    var order = new Order();
    order.setCustomerName(customerName);
    items.forEach(order::addItem);
    Order saved = orderRepository.save(order);
    log.info("Created order {} for {}", saved.getId(), customerName);
    return saved;
  }

  /**
   * Confirms a pending order, transitioning its status to {@link OrderStatus#CONFIRMED}.
   *
   * @param orderId the ID of the order to confirm
   * @return the updated order
   * @throws IllegalArgumentException if the order is not found
   * @throws IllegalStateException if the order is not in PENDING status
   */
  @Transactional(rollbackFor = Exception.class)
  public Order confirmOrder(Long orderId) {
    Order order = findById(orderId);
    if (order.getStatus() != OrderStatus.PENDING) {
      throw new IllegalStateException("Only PENDING orders can be confirmed");
    }
    order.setStatus(OrderStatus.CONFIRMED);
    return orderRepository.save(order);
  }

  /**
   * Bulk status update in its own transaction — demonstrates REQUIRES_NEW propagation. If the
   * caller's transaction rolls back, this change still commits.
   *
   * @param from the current status to match
   * @param to the new status to set
   * @return the number of orders updated
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public int bulkUpdateStatus(OrderStatus from, OrderStatus to) {
    int updated = orderRepository.bulkUpdateStatus(from, to);
    log.info("Bulk updated {} orders from {} to {}", updated, from, to);
    return updated;
  }

  /**
   * Demonstrates REPEATABLE_READ isolation to prevent non-repeatable reads during a long-running
   * calculation.
   *
   * @return the sum of {@code totalAmount} across all DELIVERED orders
   */
  @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
  public BigDecimal calculateTotalRevenue() {
    return orderRepository.findByStatus(OrderStatus.DELIVERED).stream()
        .map(Order::getTotalAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
