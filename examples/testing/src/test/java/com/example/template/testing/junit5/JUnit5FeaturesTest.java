package com.example.template.testing.junit5;

import com.example.template.testing.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates JUnit 5 features: lifecycle, nested tests, parameterized tests,
 * dynamic tests, assertions, assumptions, conditional execution, and timeouts.
 */
@DisplayName("JUnit 5 Feature Showcase")
class JUnit5FeaturesTest {

    // ── Lifecycle ──────────────────────────────────────────────────────

    @BeforeAll
    static void beforeAll() {
        // Runs once before all tests — e.g. start shared resource
    }

    @AfterAll
    static void afterAll() {
        // Runs once after all tests — e.g. cleanup shared resource
    }

    @BeforeEach
    void setUp() {
        // Runs before each test
    }

    @AfterEach
    void tearDown() {
        // Runs after each test
    }

    // ── Basic Assertions ───────────────────────────────────────────────

    @Test
    @DisplayName("User record stores values correctly")
    void userRecordStoresValues() {
        var user = new User(1L, "Alice", "alice@example.com", LocalDate.of(2024, 1, 1));

        // AssertJ fluent assertions (preferred)
        assertThat(user.name()).isEqualTo("Alice");
        assertThat(user.email()).contains("@");
        assertThat(user.createdAt()).isBefore(LocalDate.now().plusDays(1));
    }

    @Test
    @DisplayName("Grouped assertions report all failures at once")
    void groupedAssertions() {
        var user = new User(1L, "Bob", "bob@example.com", LocalDate.now());

        assertAll("user fields",
                () -> assertThat(user.id()).isEqualTo(1L),
                () -> assertThat(user.name()).isEqualTo("Bob"),
                () -> assertThat(user.email()).endsWith("@example.com")
        );
    }

    // ── Exception Testing ──────────────────────────────────────────────

    @Test
    @DisplayName("Invalid email throws IllegalArgumentException")
    void invalidEmailThrows() {
        assertThatThrownBy(() -> new User("Bad", "no-at-sign"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email");
    }

    @Test
    @DisplayName("Null name throws NullPointerException")
    void nullNameThrows() {
        assertThatNullPointerException()
                .isThrownBy(() -> new User(null, "a@b.com"))
                .withMessageContaining("name");
    }

    // ── Parameterized Tests ────────────────────────────────────────────

    @ParameterizedTest(name = "valid email: {0}")
    @ValueSource(strings = {"a@b.com", "user@domain.org", "test+tag@example.co"})
    void validEmails(String email) {
        assertThatNoException().isThrownBy(() -> new User("Test", email));
    }

    @ParameterizedTest(name = "invalid email: {0}")
    @ValueSource(strings = {"plaintext", "missing-at.com", ""})
    void invalidEmails(String email) {
        assertThatThrownBy(() -> new User("Test", email))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "Alice, alice@example.com",
            "Bob,   bob@test.org"
    })
    void csvSourceParams(String name, String email) {
        var user = new User(name, email);
        assertThat(user.name()).isEqualTo(name);
    }

    @ParameterizedTest
    @MethodSource("userProvider")
    void methodSourceParams(String name, String email) {
        assertThatNoException().isThrownBy(() -> new User(name, email));
    }

    static Stream<Arguments> userProvider() {
        return Stream.of(
                Arguments.of("Alice", "alice@example.com"),
                Arguments.of("Bob", "bob@test.org")
        );
    }

    // ── Nested Tests ───────────────────────────────────────────────────

    @Nested
    @DisplayName("When user has an ID")
    class WithId {
        private final User user = new User(42L, "Carol", "carol@example.com", LocalDate.now());

        @Test
        void idIsPresent() {
            assertThat(user.id()).isNotNull().isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("When user has no ID")
    class WithoutId {
        private final User user = new User("Dave", "dave@example.com");

        @Test
        void idIsNull() {
            assertThat(user.id()).isNull();
        }
    }

    // ── Timeouts ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Operation completes within timeout")
    void completesWithinTimeout() {
        assertTimeout(Duration.ofMillis(500), () -> {
            // Simulate fast operation
            Thread.sleep(10);
        });
    }

    // ── Conditional Execution ──────────────────────────────────────────

    @Test
    @EnabledOnOs(OS.MAC)
    void onlyOnMac() {
        // This test runs only on macOS
        assertThat(System.getProperty("os.name")).containsIgnoringCase("mac");
    }

    // ── Tags for filtering ─────────────────────────────────────────────

    @Test
    @Tag("slow")
    @DisplayName("Tagged test — run with -Dgroups=slow")
    void taggedSlowTest() {
        assertThat(true).isTrue();
    }
}
