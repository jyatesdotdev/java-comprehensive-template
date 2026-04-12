package com.example.template.database.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Child entity demonstrating {@code @ManyToOne} relationship and derived columns.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** JPA-required default constructor. */
    public OrderItem() {}

    /**
     * Creates an order item with the given product details.
     *
     * @param productName name of the product
     * @param quantity    number of units
     * @param unitPrice   price per unit
     */
    public OrderItem(String productName, int quantity, BigDecimal unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /**
     * Calculates the line total ({@code unitPrice × quantity}).
     *
     * @return the total price for this line item
     */
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // --- Getters / Setters ---

    public Long getId() { return id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
