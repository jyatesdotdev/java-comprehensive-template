package com.example.template.database;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the database example module.
 *
 * <p>Demonstrates JPA entities, Spring Data repositories, JDBC access,
 * and transaction management with an embedded H2 database.
 */
@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass") // Spring Boot entry point
public class DatabaseExampleApplication {

    /**
     * Launches the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DatabaseExampleApplication.class, args);
    }
}
