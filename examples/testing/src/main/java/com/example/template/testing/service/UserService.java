package com.example.template.testing.service;

import com.example.template.testing.model.User;
import com.example.template.testing.repository.UserRepository;
import java.util.List;

/** Service layer with business logic — the primary target for unit tests. */
public class UserService {

    private final UserRepository repository;

    /**
     * Creates a service backed by the given repository.
     *
     * @param repository the user repository
     */
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Registers a new user after verifying the email is not already taken.
     *
     * @param name  display name
     * @param email email address
     * @return the newly saved user
     * @throws IllegalStateException if a user with the given email already exists
     */
    public User register(String name, String email) {
        repository.findByEmail(email).ifPresent(existing -> {
            throw new IllegalStateException("Email already registered: " + email);
        });
        return repository.save(new User(name, email));
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id the user ID
     * @return the matching user
     * @throws IllegalArgumentException if no user exists with the given ID
     */
    public User getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    /**
     * Returns all registered users.
     *
     * @return list of all users
     */
    public List<User> listAll() {
        return repository.findAll();
    }
}
