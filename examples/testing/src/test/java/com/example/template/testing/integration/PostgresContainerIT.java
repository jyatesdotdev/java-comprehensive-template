package com.example.template.testing.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test using TestContainers to spin up a real PostgreSQL database.
 *
 * <p>Naming convention: *IT.java — picked up by maven-failsafe-plugin,
 * skipped by surefire (unit tests). Run with:
 * <pre>mvn verify -pl examples/testing -P integration-tests</pre>
 *
 * <p>Requires Docker to be running.
 */
@Testcontainers
@DisplayName("TestContainers PostgreSQL Integration")
class PostgresContainerIT {

    @Container
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(PG.getJdbcUrl(), PG.getUsername(), PG.getPassword());
        try (var stmt = connection.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id    SERIAL PRIMARY KEY,
                        name  VARCHAR(100) NOT NULL,
                        email VARCHAR(255) NOT NULL UNIQUE
                    )
                    """);
            stmt.execute("TRUNCATE users RESTART IDENTITY");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Insert and query a user via JDBC")
    void insertAndQuery() throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO users (name, email) VALUES (?, ?) RETURNING id")) {
            ps.setString(1, "Alice");
            ps.setString(2, "alice@example.com");
            var rs = ps.executeQuery();
            assertThat(rs.next()).isTrue();
            assertThat(rs.getLong("id")).isEqualTo(1L);
        }

        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery("SELECT name, email FROM users WHERE id = 1")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo("Alice");
            assertThat(rs.getString("email")).isEqualTo("alice@example.com");
        }
    }

    @Test
    @DisplayName("Unique constraint prevents duplicate emails")
    void uniqueConstraint() throws SQLException {
        try (var ps = connection.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?)")) {
            ps.setString(1, "Bob");
            ps.setString(2, "bob@example.com");
            ps.executeUpdate();
        }

        try (var ps = connection.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?)")) {
            ps.setString(1, "Bob2");
            ps.setString(2, "bob@example.com");
            assertThatThrownBy(ps::executeUpdate)
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    @DisplayName("Container provides valid JDBC connection properties")
    void containerProperties() {
        assertThat(PG.isRunning()).isTrue();
        assertThat(PG.getJdbcUrl()).contains("testdb");
        assertThat(PG.getDatabaseName()).isEqualTo("testdb");
    }
}
