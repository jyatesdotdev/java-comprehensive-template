# Java Best Practices Guide (Java 17+)

This guide covers code style, naming conventions, error handling, logging, and general best practices for modern Java development.

---

## Table of Contents

1. [Code Style](#code-style)
2. [Naming Conventions](#naming-conventions)
3. [Error Handling](#error-handling)
4. [Logging](#logging)
5. [Null Safety](#null-safety)
6. [Records & Sealed Classes](#records--sealed-classes)
7. [Collections & Streams](#collections--streams)
8. [Concurrency](#concurrency)
9. [Resource Management](#resource-management)
10. [General Principles](#general-principles)

---

## Code Style

### Formatting

Use a consistent formatter across the team. This project uses [Spotless](https://github.com/diffplug/spotless) with Google Java Format:

```bash
mvn spotless:apply   # auto-format all sources
mvn spotless:check   # CI gate — fails on unformatted code
```

Key rules:
- 4-space indentation (no tabs)
- 100-character line limit
- Braces on same line (`K&R` style)
- One blank line between methods; no consecutive blank lines

### Imports

- No wildcard imports (`import java.util.*` → import each type)
- Static imports only for test assertions and constants
- Order: `java.*`, blank line, `javax.*`, blank line, third-party, blank line, project

```java
import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import com.example.template.model.Order;
```

### Modern Java Features to Prefer

| Instead of | Use (Java 17+) |
|---|---|
| `instanceof` + cast | Pattern matching: `if (obj instanceof String s)` |
| Verbose data carriers | `record OrderDto(long id, String name) {}` |
| Long `if-else` type checks | Sealed classes + switch expressions |
| `String` concatenation in complex cases | Text blocks `"""..."""` |
| Mutable builder patterns for simple DTOs | Records with compact constructors |

```java
// Pattern matching for instanceof (Java 16+)
if (shape instanceof Circle c) {
    return Math.PI * c.radius() * c.radius();
}

// Switch expression (Java 14+)
String label = switch (status) {
    case ACTIVE -> "Active";
    case INACTIVE -> "Inactive";
    case PENDING -> "Pending";
};

// Text blocks (Java 15+)
String query = """
        SELECT id, name
        FROM users
        WHERE active = true
        ORDER BY name
        """;
```

---

## Naming Conventions

### General Rules

| Element | Convention | Example |
|---|---|---|
| Packages | lowercase, dot-separated | `com.example.template.order` |
| Classes / Interfaces | `PascalCase`, noun | `OrderService`, `Cacheable` |
| Methods | `camelCase`, verb | `findById`, `calculateTotal` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Local variables | `camelCase`, descriptive | `activeUsers` (not `au`) |
| Type parameters | Single uppercase letter or short name | `T`, `K`, `V`, `E` |
| Booleans | Prefix with `is`, `has`, `can`, `should` | `isActive`, `hasPermission` |

### Specific Patterns

```java
// Interfaces — describe capability, no "I" prefix
public interface Exportable { }
public interface OrderRepository { }

// Implementations — suffix describes strategy, not "Impl"
public class JpaOrderRepository implements OrderRepository { }
public class CsvExporter implements Exportable { }

// Exception classes — always suffix with Exception
public class OrderNotFoundException extends RuntimeException { }

// DTOs / request-response — suffix with purpose
public record CreateOrderRequest(String product, int quantity) { }
public record OrderResponse(long id, String product, Instant createdAt) { }

// Enums — singular name, UPPER_SNAKE values
public enum OrderStatus { PENDING, CONFIRMED, SHIPPED, DELIVERED }
```

### Package Structure

```
com.example.template
├── config/          # Spring @Configuration, properties binding
├── controller/      # REST controllers (thin — delegate to services)
├── service/         # Business logic
├── repository/      # Data access
├── model/           # JPA entities, domain objects
├── dto/             # Request/response DTOs, records
├── exception/       # Custom exceptions + global handler
└── util/            # Stateless utility classes (use sparingly)
```

---

## Error Handling

### Principles

1. **Fail fast** — validate inputs at the boundary, throw early
2. **Use unchecked exceptions** for programming errors; checked exceptions only when the caller can meaningfully recover
3. **Never catch `Exception` or `Throwable`** in business logic — be specific
4. **Never swallow exceptions** — at minimum, log them
5. **Use domain-specific exceptions** — not generic `RuntimeException`

### Exception Hierarchy

```java
// Base exception for the domain
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
    protected DomainException(String message, Throwable cause) { super(message, cause); }
}

// Specific exceptions
public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(long id) {
        super("Order not found: " + id);
    }
}

public class InsufficientStockException extends DomainException {
    public InsufficientStockException(String product, int requested, int available) {
        super("Insufficient stock for %s: requested %d, available %d"
                .formatted(product, requested, available));
    }
}
```

### Global Exception Handling (Spring Boot)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}

public record ErrorResponse(String code, String message) { }
```

### Anti-Patterns to Avoid

```java
// ❌ Catching generic Exception
try { processOrder(order); }
catch (Exception e) { /* too broad */ }

// ❌ Swallowing exceptions
try { processOrder(order); }
catch (OrderProcessingException e) { /* empty — silent failure */ }

// ❌ Using exceptions for control flow
try {
    return repository.findById(id);
} catch (NoResultException e) {
    return null;  // use Optional instead
}

// ✅ Correct approach
public Optional<Order> findById(long id) {
    return repository.findById(id);
}
```

---

## Logging

### Framework

Use **SLF4J** as the facade with **Logback** as the implementation (Spring Boot default).

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
}
```

> With Lombok: `@Slf4j` on the class generates the `log` field automatically.

### Log Levels

| Level | Use For | Example |
|---|---|---|
| `ERROR` | Failures requiring attention; system cannot fulfill request | DB connection failure, unhandled exception |
| `WARN` | Recoverable issues; degraded behavior | Retry succeeded, fallback used, deprecated API called |
| `INFO` | Significant business events | Order created, payment processed, user logged in |
| `DEBUG` | Diagnostic detail for development | Method entry/exit, intermediate values |
| `TRACE` | Very fine-grained; rarely enabled | Full request/response bodies, loop iterations |

### Best Practices

```java
// ✅ Use parameterized messages — avoids string concatenation when level is disabled
log.info("Order {} created for customer {}", orderId, customerId);

// ✅ Log exceptions with the throwable as the last argument
log.error("Failed to process order {}", orderId, exception);

// ✅ Use MDC for request-scoped context (correlationId, userId)
MDC.put("correlationId", UUID.randomUUID().toString());
try {
    processRequest(request);
} finally {
    MDC.clear();
}

// ❌ Don't log and throw — pick one (usually throw; let the handler log)
log.error("Order not found: {}", id);
throw new OrderNotFoundException(id);  // handler will log this

// ❌ Don't log sensitive data
log.info("User authenticated: {}", user.getPassword());  // NEVER
```

### Logback Configuration (`logback-spring.xml`)

```xml
<configuration>
    <property name="LOG_PATTERN"
              value="%d{ISO8601} [%thread] [%X{correlationId}] %-5level %logger{36} - %msg%n"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder><pattern>${LOG_PATTERN}</pattern></encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder><pattern>${LOG_PATTERN}</pattern></encoder>
    </appender>

    <logger name="com.example.template" level="DEBUG"/>
    <logger name="org.springframework" level="INFO"/>
    <logger name="org.hibernate.SQL" level="DEBUG"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

---

## Null Safety

### Principles

1. **Never return `null`** from public methods — use `Optional<T>` for "might not exist"
2. **Never pass `null`** as a method argument — use overloads or builder patterns
3. **Validate at boundaries** — use `Objects.requireNonNull` or `@NotNull`
4. **Use `Optional` only as return types** — not fields, parameters, or collections

```java
// ✅ Return Optional for queries that may have no result
public Optional<Order> findById(long id) {
    return repository.findById(id);
}

// ✅ Validate non-null parameters
public void process(Order order) {
    Objects.requireNonNull(order, "order must not be null");
    // ...
}

// ❌ Don't use Optional as a field or parameter
private Optional<String> name;                    // bad
public void setName(Optional<String> name) { }   // bad

// ✅ Use empty collections instead of null
public List<Order> findByCustomer(long customerId) {
    // returns empty list, never null
    return repository.findByCustomerId(customerId);
}
```

---

## Records & Sealed Classes

### Records (Java 16+)

Use records for immutable data carriers — DTOs, value objects, events:

```java
// Compact, immutable, auto-generates equals/hashCode/toString
public record Money(BigDecimal amount, Currency currency) {
    // Compact constructor for validation
    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
    }
}
```

### Sealed Classes (Java 17)

Use sealed classes to model closed type hierarchies:

```java
public sealed interface Shape permits Circle, Rectangle, Triangle {
    double area();
}

public record Circle(double radius) implements Shape {
    public double area() { return Math.PI * radius * radius; }
}

public record Rectangle(double width, double height) implements Shape {
    public double area() { return width * height; }
}

public record Triangle(double base, double height) implements Shape {
    public double area() { return 0.5 * base * height; }
}

// Exhaustive switch — compiler verifies all cases covered
public static String describe(Shape shape) {
    return switch (shape) {
        case Circle c    -> "Circle with radius " + c.radius();
        case Rectangle r -> "Rectangle %sx%s".formatted(r.width(), r.height());
        case Triangle t  -> "Triangle with base " + t.base();
    };
}
```

---

## Collections & Streams

```java
// ✅ Use factory methods for immutable collections
var names = List.of("Alice", "Bob", "Charlie");
var lookup = Map.of("key1", "value1", "key2", "value2");

// ✅ Prefer streams for transformations; loops for side effects
List<String> activeNames = users.stream()
        .filter(User::isActive)
        .map(User::name)
        .sorted()
        .toList();  // Java 16+ — returns unmodifiable list

// ✅ Use Collectors.toUnmodifiableList() pre-Java 16
// ✅ Use toList() on Java 16+

// ❌ Don't use streams for simple iterations with side effects
users.stream().forEach(u -> u.sendNotification());  // bad
users.forEach(User::sendNotification);               // better, but still side-effectful
for (var user : users) { user.sendNotification(); }  // clearest intent
```

---

## Concurrency

```java
// ✅ Virtual threads (Java 21+) for I/O-bound work
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<Result>> futures = tasks.stream()
            .map(task -> executor.submit(() -> process(task)))
            .toList();
}

// ✅ CompletableFuture for async pipelines
CompletableFuture.supplyAsync(() -> fetchOrder(id))
        .thenApply(this::enrichOrder)
        .thenAccept(this::saveOrder)
        .exceptionally(ex -> { log.error("Pipeline failed", ex); return null; });

// ✅ Use concurrent collections, not synchronized wrappers
ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
counters.computeIfAbsent("hits", k -> new AtomicLong()).incrementAndGet();

// ❌ Don't share mutable state without synchronization
// ❌ Don't use Thread.stop(), Thread.suspend(), or Thread.resume()
// ❌ Don't catch InterruptedException without restoring the interrupt flag
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();  // ✅ restore flag
    throw new RuntimeException("Interrupted", e);
}
```

---

## Resource Management

```java
// ✅ Always use try-with-resources for AutoCloseable resources
try (var conn = dataSource.getConnection();
     var stmt = conn.prepareStatement(sql);
     var rs = stmt.executeQuery()) {
    while (rs.next()) {
        results.add(mapRow(rs));
    }
}

// ✅ For non-AutoCloseable resources, use try-finally
var lock = new ReentrantLock();
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();
}
```

---

## General Principles

1. **Favor immutability** — use `final` fields, records, unmodifiable collections
2. **Keep methods short** — a method should do one thing; aim for < 20 lines
3. **Prefer composition over inheritance** — use interfaces and delegation
4. **Program to interfaces** — declare `List<T>`, not `ArrayList<T>`
5. **Minimize visibility** — default to `private`; widen only when needed
6. **Avoid premature optimization** — write clear code first, profile, then optimize
7. **Write self-documenting code** — good names reduce the need for comments
8. **Use `var` judiciously** — fine when the type is obvious from the right-hand side
9. **Keep dependencies minimal** — don't add a library for one utility method
10. **Follow the Principle of Least Surprise** — APIs should behave as callers expect

```java
// ✅ var is fine here — type is obvious
var orders = new ArrayList<Order>();
var name = user.getName();

// ❌ var obscures the type
var result = service.process(data);  // what type is result?
```


---

## See Also

- [Main README](../README.md) — Project overview and quick start
- [Architecture Patterns](architecture-patterns.md) — Project structure and layer design
- [Development Workflow](development-workflow.md) — CI/CD and quality gates
- [Documentation Standards](documentation-standards.md) — Javadoc conventions
- [Tutorial](TUTORIAL.md) — New developer walkthrough
