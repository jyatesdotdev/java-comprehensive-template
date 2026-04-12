package com.example.template.restfulapi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a product.
 *
 * <p>Uses a mutable class (not a record) because domain entities typically
 * have identity semantics and mutable lifecycle state.
 */
public class Product {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Instant createdAt;
    private Instant updatedAt;

    /** Default constructor for frameworks (e.g. JPA, Jackson). */
    public Product() { }

    /**
     * Creates a product with the given attributes. Sets {@code createdAt} and {@code updatedAt} to now.
     *
     * @param id          unique identifier
     * @param name        product display name
     * @param description product description
     * @param price       unit price
     */
    public Product(UUID id, String name, String description, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // --- Getters & Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
