package com.example.template.testing.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Simple domain entity for testing demonstrations.
 *
 * @param id        unique identifier, may be {@code null} for unsaved users
 * @param name      user display name, must not be {@code null}
 * @param email     email address containing {@code @}, must not be {@code null}
 * @param createdAt date the user was created
 */
public record User(Long id, String name, String email, LocalDate createdAt) {

    /**
     * Validates that required fields are non-null and the email contains {@code @}.
     *
     * @throws NullPointerException     if {@code name} or {@code email} is {@code null}
     * @throws IllegalArgumentException if {@code email} does not contain {@code @}
     */
    public User {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(email, "email must not be null");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
    }

    /**
     * Convenience constructor for new (unsaved) users with {@code id = null}
     * and {@code createdAt = today}.
     *
     * @param name  user display name
     * @param email email address
     */
    public User(String name, String email) {
        this(null, name, email, LocalDate.now());
    }
}
