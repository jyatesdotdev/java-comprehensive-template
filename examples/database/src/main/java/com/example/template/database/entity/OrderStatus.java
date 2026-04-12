package com.example.template.database.entity;

/**
 * Lifecycle states for an {@link Order}.
 */
public enum OrderStatus {
    /** Order created but not yet confirmed. */
    PENDING,
    /** Order confirmed by the customer or system. */
    CONFIRMED,
    /** Order has been shipped. */
    SHIPPED,
    /** Order delivered to the customer. */
    DELIVERED,
    /** Order was cancelled. */
    CANCELLED
}
