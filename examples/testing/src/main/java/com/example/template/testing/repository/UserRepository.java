package com.example.template.testing.repository;

import com.example.template.testing.model.User;
import java.util.List;
import java.util.Optional;

/** Repository interface — demonstrates coding to interfaces for testability. */
public interface UserRepository {

    /**
     * Persists a user, assigning an ID if new.
     *
     * @param user the user to save
     * @return the saved user with a non-null ID
     */
    User save(User user);

    /**
     * Finds a user by their unique ID.
     *
     * @param id the user ID
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns all users.
     *
     * @return an unmodifiable list of all users
     */
    List<User> findAll();

    /**
     * Deletes the user with the given ID.
     *
     * @param id the ID of the user to delete
     */
    void deleteById(Long id);
}
