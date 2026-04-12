package com.example.template.database.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity demonstrating JPA annotations, relationships, and lifecycle callbacks.
 *
 * <p>Key concepts shown:
 * <ul>
 *   <li>{@code @Entity} / {@code @Table} — mapping to database table</li>
 *   <li>{@code @GeneratedValue} — ID generation strategies</li>
 *   <li>{@code @OneToMany} with cascade and orphan removal</li>
 *   <li>{@code @Enumerated} — mapping Java enums</li>
 *   <li>{@code @PrePersist} / {@code @PreUpdate} — lifecycle callbacks</li>
 *   <li>Optimistic locking with {@code @Version}</li>
 * </ul>
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Version
    private Integer version;

    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // --- Convenience methods ---

    /**
     * Adds an item to this order, sets the back-reference, and recalculates the total.
     *
     * @param item the order item to add
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }

    /**
     * Removes an item from this order, clears the back-reference, and recalculates the total.
     *
     * @param item the order item to remove
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- Getters / Setters ---

    public Long getId() { return id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public List<OrderItem> getItems() { return items; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public Integer getVersion() { return version; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
