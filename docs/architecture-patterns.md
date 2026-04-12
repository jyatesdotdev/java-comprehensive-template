# Architecture Patterns

Comprehensive guide to this template's architecture and enterprise architecture patterns with Java 17+ examples.

---

## Table of Contents

0. [Template Architecture](#0-template-architecture)
   - [Project Structure](#project-structure)
   - [Maven Multi-Module Relationships](#maven-multi-module-relationships)
   - [Package Hierarchy](#package-hierarchy)
   - [Layer Diagram](#layer-diagram)
   - [Dependency Injection Patterns](#dependency-injection-patterns)
1. [Microservices Architecture](#1-microservices-architecture)
2. [Hexagonal Architecture (Ports & Adapters)](#2-hexagonal-architecture-ports--adapters)
3. [CQRS (Command Query Responsibility Segregation)](#3-cqrs)
4. [Event Sourcing](#4-event-sourcing)
5. [Combining Patterns](#5-combining-patterns)
6. [Decision Guide](#6-decision-guide)

---

## 0. Template Architecture

### Project Structure

```
java-enterprise-template/
├── pom.xml                        # Parent POM — dependency & plugin management
├── checkstyle.xml                 # Checkstyle rules
├── checkstyle-suppressions.xml    # Checkstyle suppressions
├── pmd-ruleset.xml                # PMD custom ruleset
├── spotbugs-exclude.xml           # SpotBugs exclusions
├── owasp-suppressions.xml         # OWASP dependency-check suppressions
├── .github/workflows/ci.yml       # CI pipeline
├── docs/                          # Project documentation
│   ├── architecture-patterns.md   # ← You are here
│   ├── best-practices.md
│   ├── development-workflow.md
│   ├── documentation-standards.md
│   ├── SECURITY_SCANNING.md
│   └── third-party-libraries.md
└── examples/                      # Maven sub-modules
    ├── restful-api/               # Spring Boot REST API with OpenAPI
    ├── database/                  # JPA, JDBC, HikariCP, Flyway
    ├── etl/                       # Data pipelines, batch processing, Spark
    ├── hpc/                       # Concurrency: virtual threads, parallel streams
    ├── patterns/                  # GoF design patterns (creational, structural, behavioral)
    ├── simulation/                # Monte Carlo, discrete-event simulation
    ├── systems/                   # JNI, off-heap memory, profiling, benchmarks
    └── testing/                   # JUnit 5, Mockito, Testcontainers, ArchUnit
```

### Maven Multi-Module Relationships

The root `pom.xml` (`com.example.template:java-enterprise-template`) is a `<packaging>pom</packaging>` parent that provides:

- **Centralized dependency versions** via `<dependencyManagement>` — child modules declare dependencies without specifying versions.
- **Centralized plugin configuration** via `<pluginManagement>` — quality tools (SpotBugs, Checkstyle, PMD, OWASP, JaCoCo) are configured once and inherited by all modules.
- **Shared properties** — Java version, encoding, library versions defined in `<properties>`.

```
java-enterprise-template (parent POM, packaging=pom)
 ├── examples/restful-api   (jar, inherits parent)
 ├── examples/database       (jar, inherits parent)
 ├── examples/etl            (jar, inherits parent)
 ├── examples/hpc            (jar, inherits parent)
 ├── examples/patterns       (jar, inherits parent)
 ├── examples/simulation     (jar, inherits parent)
 ├── examples/systems        (jar, inherits parent)
 └── examples/testing        (jar, inherits parent)
```

Each child module's `pom.xml` references the parent:

```xml
<parent>
    <groupId>com.example.template</groupId>
    <artifactId>java-enterprise-template</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../../pom.xml</relativePath>
</parent>
```

Building from the root (`mvn verify`) compiles and tests all modules. Build a single module with:

```bash
mvn verify -pl examples/restful-api
```

### Package Hierarchy

All modules share the base package `com.example.template`:

| Module | Base Package | Purpose |
|--------|-------------|---------|
| restful-api | `com.example.template.restfulapi` | REST controllers, DTOs, services, clients |
| database | `com.example.template.database` | Entities, repositories, JDBC, config |
| etl | `com.example.template.etl` | Pipelines, batch config, Spark |
| hpc | `com.example.template.hpc` | Concurrency and parallelism examples |
| patterns | `com.example.template.patterns` | Creational, structural, behavioral patterns |
| simulation | `com.example.template.simulation` | Monte Carlo, discrete-event simulation |
| systems | `com.example.template.systems` | JNI, off-heap memory, profiling |
| testing | `com.example.template.testing` | Test examples (model, service, repository) |

The `restful-api` module demonstrates the most complete layered structure:

```
com.example.template.restfulapi/
├── controller/     # REST endpoints (@RestController)
├── service/        # Business logic (interface + implementation)
├── domain/         # Domain entities (Product)
├── dto/            # Request/response DTOs (ProductRequest, ProductResponse, ErrorResponse)
├── mapper/         # DTO ↔ domain mapping (ProductMapper)
├── exception/      # Custom exceptions + global handler
├── client/         # HTTP client examples (WebClient, RestTemplate, JAX-RS)
└── config/         # Spring @Configuration beans (OpenApiConfig)
```

### Layer Diagram

The `restful-api` module follows a classic layered architecture:

```
┌─────────────────────────────────────────────────────┐
│                   HTTP Client                       │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│  Controller Layer    (@RestController)               │
│  ProductController — validates input, returns DTOs   │
│  GlobalExceptionHandler — maps exceptions to HTTP    │
└──────────────────────┬──────────────────────────────┘
                       │ uses ProductMapper (DTO ↔ domain)
                       ▼
┌─────────────────────────────────────────────────────┐
│  Service Layer       (interface + impl)              │
│  ProductService (interface)                          │
│  InMemoryProductService (@Service)                   │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│  Domain Layer        (POJOs)                         │
│  Product — core business entity                      │
└─────────────────────────────────────────────────────┘
```

The `database` module adds a persistence layer:

```
Controller → Service → Repository/DAO → Database
                         │
              ┌──────────┴──────────┐
              │ OrderRepository     │  (Spring Data JPA)
              │ JdbcOrderDao        │  (raw JDBC)
              └─────────────────────┘
```

### Dependency Injection Patterns

This template uses Spring's dependency injection throughout. Three patterns appear:

#### 1. Constructor Injection (preferred)

Used in controllers and services. Spring auto-wires the single constructor — no `@Autowired` needed:

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    // Spring injects ProductService automatically
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
}
```

#### 2. `@Bean` Factory Methods

Used in `@Configuration` classes to create beans that need custom setup:

```java
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
```

#### 3. Interface-Based Abstraction

Services are defined as interfaces, with implementations annotated `@Service`. This enables swapping implementations (e.g., in-memory → database-backed) without changing consumers:

```java
public interface ProductService {
    List<Product> findAll();
    Product findById(UUID id);
    Product create(ProductRequest request);
    // ...
}

@Service
public class InMemoryProductService implements ProductService {
    // implementation
}
```

> **Convention**: Use constructor injection for all production code. Reserve `@Bean` methods for infrastructure wiring (data sources, HTTP clients, third-party library configuration).

---

## 1. Microservices Architecture

### Overview

Decompose an application into small, independently deployable services, each owning its data and communicating via well-defined APIs.

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│  Order   │    │ Inventory│    │ Payment  │
│ Service  │───▶│ Service  │    │ Service  │
│  :8081   │    │  :8082   │    │  :8083   │
└────┬─────┘    └────┬─────┘    └────┬─────┘
     │               │               │
  ┌──┴──┐         ┌──┴──┐         ┌──┴──┐
  │ DB  │         │ DB  │         │ DB  │
  └─────┘         └─────┘         └─────┘
```

### Service Structure (Spring Boot)

Each microservice follows a standard layout:

```
order-service/
├── src/main/java/com/example/order/
│   ├── OrderServiceApplication.java
│   ├── api/           # REST controllers (inbound)
│   ├── domain/        # Business logic, entities
│   ├── infrastructure/# DB repos, external clients
│   └── config/        # Spring configuration
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

### Inter-Service Communication

#### Synchronous — REST with Spring WebClient

```java
@Service
public class InventoryClient {

    private final WebClient webClient;

    public InventoryClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://inventory-service:8082").build();
    }

    public boolean checkStock(String sku, int quantity) {
        return webClient.get()
                .uri("/api/inventory/{sku}?qty={qty}", sku, quantity)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
```

#### Asynchronous — Events with Spring Cloud Stream / Kafka

```java
// Producer
@Service
public class OrderEventPublisher {

    private final StreamBridge streamBridge;

    public OrderEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        streamBridge.send("orders-out-0", event);
    }
}

// Consumer (in Inventory Service)
@Configuration
public class OrderEventConsumer {

    @Bean
    public Consumer<OrderCreatedEvent> orders() {
        return event -> {
            // Reserve inventory for the order
            inventoryService.reserve(event.sku(), event.quantity());
        };
    }
}
```

#### Event Record

```java
public record OrderCreatedEvent(
        String orderId,
        String sku,
        int quantity,
        Instant timestamp
) {}
```

### Service Discovery & API Gateway

```yaml
# application.yml for API Gateway (Spring Cloud Gateway)
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
        - id: inventory-service
          uri: lb://inventory-service
          predicates:
            - Path=/api/inventory/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Resilience — Circuit Breaker with Resilience4j

```java
@Service
public class ResilientInventoryClient {

    private final WebClient webClient;

    public ResilientInventoryClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://inventory-service:8082").build();
    }

    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackStock")
    @Retry(name = "inventory")
    public boolean checkStock(String sku, int quantity) {
        return webClient.get()
                .uri("/api/inventory/{sku}?qty={qty}", sku, quantity)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    private boolean fallbackStock(String sku, int quantity, Throwable t) {
        // Optimistic fallback — accept order, reconcile later
        return true;
    }
}
```

### When to Use Microservices

| Use When | Avoid When |
|----------|------------|
| Multiple teams need independent deployment | Small team, simple domain |
| Different scaling requirements per component | Tight latency requirements across components |
| Polyglot persistence is beneficial | Shared transactional boundaries |
| Organizational boundaries align with services | Overhead of distributed systems isn't justified |

---

## 2. Hexagonal Architecture (Ports & Adapters)

### Overview

Isolate business logic from infrastructure by defining ports (interfaces) that adapters implement. The domain has zero dependencies on frameworks or I/O.

```
            ┌─────────────────────────────────┐
            │         DOMAIN CORE             │
  Driving   │                                 │   Driven
  Adapters  │   ┌───────────────────────┐     │   Adapters
            │   │   Business Logic      │     │
 ┌──────┐   │   │   (pure Java)         │     │   ┌──────┐
 │ REST ├──▶Port──▶                     ──▶Port──▶│  DB  │
 └──────┘   │   │                       │     │   └──────┘
 ┌──────┐   │   └───────────────────────┘     │   ┌──────┐
 │ CLI  ├──▶Port                          ──▶Port──▶│ MQ   │
 └──────┘   │                                 │   └──────┘
            └─────────────────────────────────┘
```

### Package Structure

```
com.example.order/
├── domain/
│   ├── model/          # Entities, value objects (no annotations)
│   ├── port/
│   │   ├── in/         # Driving ports (use cases)
│   │   └── out/        # Driven ports (repositories, external)
│   └── service/        # Domain service implementations
├── adapter/
│   ├── in/
│   │   ├── web/        # REST controllers
│   │   └── cli/        # CLI adapters
│   └── out/
│       ├── persistence/# JPA repositories
│       └── messaging/  # Kafka producers
└── config/             # Spring wiring
```

### Domain Model (No Framework Dependencies)

```java
// Pure domain entity — no JPA, no Spring, no Jackson
public class Order {

    private final String id;
    private final List<LineItem> items;
    private OrderStatus status;

    public Order(String id, List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        this.id = id;
        this.items = List.copyOf(items);
        this.status = OrderStatus.CREATED;
    }

    public BigDecimal total() {
        return items.stream()
                .map(LineItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirm() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Can only confirm CREATED orders");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    // Getters omitted for brevity
}

public record LineItem(String productId, int quantity, BigDecimal unitPrice) {
    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

public enum OrderStatus { CREATED, CONFIRMED, SHIPPED, CANCELLED }
```

### Ports

```java
// Driving port — defines what the application CAN DO
public interface CreateOrderUseCase {
    Order createOrder(CreateOrderCommand command);
}

public record CreateOrderCommand(String customerId, List<LineItem> items) {}

// Driven port — defines what the application NEEDS
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
}

public interface OrderEventPublisher {
    void publish(OrderCreatedEvent event);
}
```

### Domain Service (Implements Driving Port)

```java
public class OrderService implements CreateOrderUseCase {

    private final OrderRepository repository;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository repository, OrderEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Order createOrder(CreateOrderCommand command) {
        var order = new Order(UUID.randomUUID().toString(), command.items());
        repository.save(order);
        eventPublisher.publish(new OrderCreatedEvent(order.id(), Instant.now()));
        return order;
    }
}
```

### Adapters

```java
// Driving adapter — REST controller
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrder;

    public OrderController(CreateOrderUseCase createOrder) {
        this.createOrder = createOrder;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
        var command = new CreateOrderCommand(request.customerId(), request.items());
        var order = createOrder.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }
}

// Driven adapter — JPA persistence
@Repository
class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepo springRepo;

    JpaOrderRepository(SpringDataOrderRepo springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public void save(Order order) {
        springRepo.save(OrderEntity.fromDomain(order));
    }

    @Override
    public Optional<Order> findById(String id) {
        return springRepo.findById(id).map(OrderEntity::toDomain);
    }
}
```

### Configuration — Wiring Adapters to Ports

```java
@Configuration
public class OrderConfig {

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderRepository repository,
            OrderEventPublisher eventPublisher) {
        return new OrderService(repository, eventPublisher);
    }
}
```

### Benefits

- Domain logic is testable without Spring, databases, or mocks of framework classes
- Adapters are swappable (switch from JPA to JDBC, REST to gRPC)
- Clear dependency direction: adapters depend on domain, never the reverse

---

## 3. CQRS

### Overview

Separate the read model (queries) from the write model (commands). Each side can have its own data model, storage, and scaling strategy.

```
                    ┌──────────────┐
                    │   Client     │
                    └──┬───────┬───┘
                       │       │
              Command  │       │  Query
                       ▼       ▼
              ┌────────┐   ┌────────┐
              │Command │   │ Query  │
              │Handler │   │Handler │
              └───┬────┘   └───┬────┘
                  │            │
              ┌───┴────┐   ┌──┴─────┐
              │ Write  │   │  Read  │
              │ Store  │──▶│  Store │
              └────────┘   └────────┘
                     sync/project
```

### Command Side

```java
// Commands are intent — what the user wants to do
public sealed interface OrderCommand {
    record PlaceOrder(String customerId, List<LineItem> items) implements OrderCommand {}
    record CancelOrder(String orderId, String reason) implements OrderCommand {}
}

// Command handler — validates and executes
@Service
public class OrderCommandHandler {

    private final OrderRepository writeRepo;
    private final ApplicationEventPublisher events;

    public OrderCommandHandler(OrderRepository writeRepo, ApplicationEventPublisher events) {
        this.writeRepo = writeRepo;
        this.events = events;
    }

    public String handle(OrderCommand.PlaceOrder cmd) {
        var order = new Order(UUID.randomUUID().toString(), cmd.items());
        writeRepo.save(order);
        events.publishEvent(new OrderPlacedEvent(order.id(), order.total(), Instant.now()));
        return order.id();
    }

    public void handle(OrderCommand.CancelOrder cmd) {
        var order = writeRepo.findById(cmd.orderId())
                .orElseThrow(() -> new OrderNotFoundException(cmd.orderId()));
        order.cancel(cmd.reason());
        writeRepo.save(order);
        events.publishEvent(new OrderCancelledEvent(cmd.orderId(), Instant.now()));
    }
}
```

### Query Side

```java
// Query model — optimized for reads, denormalized
public record OrderSummary(
        String orderId,
        String customerId,
        BigDecimal total,
        String status,
        Instant placedAt
) {}

// Query handler — reads from the read-optimized store
@Service
public class OrderQueryHandler {

    private final JdbcTemplate jdbc;

    public OrderQueryHandler(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<OrderSummary> findById(String orderId) {
        return jdbc.query(
                "SELECT order_id, customer_id, total, status, placed_at FROM order_summary WHERE order_id = ?",
                (rs, i) -> new OrderSummary(
                        rs.getString("order_id"),
                        rs.getString("customer_id"),
                        rs.getBigDecimal("total"),
                        rs.getString("status"),
                        rs.getTimestamp("placed_at").toInstant()
                ),
                orderId
        ).stream().findFirst();
    }

    public List<OrderSummary> findByCustomer(String customerId) {
        return jdbc.query(
                "SELECT order_id, customer_id, total, status, placed_at FROM order_summary WHERE customer_id = ? ORDER BY placed_at DESC",
                (rs, i) -> new OrderSummary(
                        rs.getString("order_id"),
                        rs.getString("customer_id"),
                        rs.getBigDecimal("total"),
                        rs.getString("status"),
                        rs.getTimestamp("placed_at").toInstant()
                ),
                customerId
        );
    }
}
```

### Projection — Syncing Write → Read

```java
@Component
public class OrderProjection {

    private final JdbcTemplate jdbc;

    public OrderProjection(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @EventListener
    public void on(OrderPlacedEvent event) {
        jdbc.update(
                "INSERT INTO order_summary (order_id, customer_id, total, status, placed_at) VALUES (?, ?, ?, ?, ?)",
                event.orderId(), event.customerId(), event.total(), "PLACED", Timestamp.from(event.timestamp())
        );
    }

    @EventListener
    public void on(OrderCancelledEvent event) {
        jdbc.update(
                "UPDATE order_summary SET status = ? WHERE order_id = ?",
                "CANCELLED", event.orderId()
        );
    }
}
```

### REST Controller — Separate Endpoints

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderCommandHandler commands;
    private final OrderQueryHandler queries;

    public OrderController(OrderCommandHandler commands, OrderQueryHandler queries) {
        this.commands = commands;
        this.queries = queries;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> placeOrder(@RequestBody PlaceOrderRequest req) {
        String id = commands.handle(new OrderCommand.PlaceOrder(req.customerId(), req.items()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("orderId", id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String id, @RequestBody CancelRequest req) {
        commands.handle(new OrderCommand.CancelOrder(id, req.reason()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderSummary> getOrder(@PathVariable String id) {
        return queries.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

---

## 4. Event Sourcing

### Overview

Instead of storing current state, store the sequence of events that led to the current state. State is derived by replaying events.

```
Event Store:
┌─────────────────────────────────────────────────────┐
│ OrderCreated │ ItemAdded │ ItemAdded │ OrderConfirmed│
│  (t=1)       │  (t=2)    │  (t=3)    │  (t=4)       │
└─────────────────────────────────────────────────────┘
                        │
                   replay ▼
              ┌──────────────────┐
              │  Current State:  │
              │  2 items, CONFIRMED│
              └──────────────────┘
```

### Event Definitions

```java
public sealed interface OrderEvent {

    String orderId();
    Instant occurredAt();

    record Created(String orderId, String customerId, Instant occurredAt)
            implements OrderEvent {}

    record ItemAdded(String orderId, String productId, int quantity, BigDecimal price, Instant occurredAt)
            implements OrderEvent {}

    record ItemRemoved(String orderId, String productId, Instant occurredAt)
            implements OrderEvent {}

    record Confirmed(String orderId, Instant occurredAt)
            implements OrderEvent {}

    record Cancelled(String orderId, String reason, Instant occurredAt)
            implements OrderEvent {}
}
```

### Aggregate — Rebuilt from Events

```java
public class OrderAggregate {

    private String id;
    private String customerId;
    private final Map<String, LineItem> items = new LinkedHashMap<>();
    private OrderStatus status;
    private final List<OrderEvent> uncommittedEvents = new ArrayList<>();

    // Rebuild from history
    public static OrderAggregate fromHistory(List<OrderEvent> history) {
        var aggregate = new OrderAggregate();
        history.forEach(aggregate::apply);
        return aggregate;
    }

    // Command: create order
    public static OrderAggregate create(String orderId, String customerId) {
        var aggregate = new OrderAggregate();
        aggregate.raise(new OrderEvent.Created(orderId, customerId, Instant.now()));
        return aggregate;
    }

    // Command: add item
    public void addItem(String productId, int quantity, BigDecimal price) {
        if (status != OrderStatus.CREATED) throw new IllegalStateException("Cannot modify " + status + " order");
        raise(new OrderEvent.ItemAdded(id, productId, quantity, price, Instant.now()));
    }

    // Command: confirm
    public void confirm() {
        if (items.isEmpty()) throw new IllegalStateException("Cannot confirm empty order");
        if (status != OrderStatus.CREATED) throw new IllegalStateException("Cannot confirm " + status + " order");
        raise(new OrderEvent.Confirmed(id, Instant.now()));
    }

    // Apply event to state (no side effects)
    private void apply(OrderEvent event) {
        switch (event) {
            case OrderEvent.Created e -> { id = e.orderId(); customerId = e.customerId(); status = OrderStatus.CREATED; }
            case OrderEvent.ItemAdded e -> items.put(e.productId(), new LineItem(e.productId(), e.quantity(), e.price()));
            case OrderEvent.ItemRemoved e -> items.remove(e.productId());
            case OrderEvent.Confirmed e -> status = OrderStatus.CONFIRMED;
            case OrderEvent.Cancelled e -> status = OrderStatus.CANCELLED;
        }
    }

    // Raise event: apply + track as uncommitted
    private void raise(OrderEvent event) {
        apply(event);
        uncommittedEvents.add(event);
    }

    public List<OrderEvent> uncommittedEvents() { return List.copyOf(uncommittedEvents); }
    public void markCommitted() { uncommittedEvents.clear(); }
}
```

### Event Store

```java
public interface EventStore {
    void append(String aggregateId, List<OrderEvent> events, long expectedVersion);
    List<OrderEvent> load(String aggregateId);
}

@Repository
public class JdbcEventStore implements EventStore {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public JdbcEventStore(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void append(String aggregateId, List<OrderEvent> events, long expectedVersion) {
        long version = expectedVersion;
        for (OrderEvent event : events) {
            jdbc.update(
                    "INSERT INTO event_store (aggregate_id, version, event_type, payload, occurred_at) VALUES (?, ?, ?, ?::jsonb, ?)",
                    aggregateId, ++version, event.getClass().getSimpleName(),
                    serialize(event), Timestamp.from(event.occurredAt())
            );
        }
    }

    @Override
    public List<OrderEvent> load(String aggregateId) {
        return jdbc.query(
                "SELECT event_type, payload FROM event_store WHERE aggregate_id = ? ORDER BY version",
                (rs, i) -> deserialize(rs.getString("event_type"), rs.getString("payload")),
                aggregateId
        );
    }

    private String serialize(OrderEvent event) {
        try { return mapper.writeValueAsString(event); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private OrderEvent deserialize(String type, String json) {
        try {
            return switch (type) {
                case "Created"     -> mapper.readValue(json, OrderEvent.Created.class);
                case "ItemAdded"   -> mapper.readValue(json, OrderEvent.ItemAdded.class);
                case "ItemRemoved" -> mapper.readValue(json, OrderEvent.ItemRemoved.class);
                case "Confirmed"   -> mapper.readValue(json, OrderEvent.Confirmed.class);
                case "Cancelled"   -> mapper.readValue(json, OrderEvent.Cancelled.class);
                default -> throw new IllegalArgumentException("Unknown event type: " + type);
            };
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
```

### Event Store Schema

```sql
CREATE TABLE event_store (
    id           BIGSERIAL PRIMARY KEY,
    aggregate_id VARCHAR(36)  NOT NULL,
    version      BIGINT       NOT NULL,
    event_type   VARCHAR(100) NOT NULL,
    payload      JSONB        NOT NULL,
    occurred_at  TIMESTAMP    NOT NULL,
    UNIQUE (aggregate_id, version)
);

CREATE INDEX idx_event_store_aggregate ON event_store (aggregate_id, version);
```

### Snapshots (Performance Optimization)

For aggregates with many events, periodically store a snapshot to avoid replaying the full history:

```java
public interface SnapshotStore {
    Optional<Snapshot<OrderAggregate>> load(String aggregateId);
    void save(String aggregateId, OrderAggregate state, long version);
}

// Loading with snapshot
public OrderAggregate loadAggregate(String id) {
    var snapshot = snapshotStore.load(id);
    List<OrderEvent> events;
    OrderAggregate aggregate;

    if (snapshot.isPresent()) {
        aggregate = snapshot.get().state();
        events = eventStore.loadAfterVersion(id, snapshot.get().version());
    } else {
        aggregate = new OrderAggregate();
        events = eventStore.load(id);
    }

    events.forEach(aggregate::apply);
    return aggregate;
}
```

---

## 5. Combining Patterns

These patterns compose naturally in enterprise systems:

```
┌─────────────────────────────────────────────────────────┐
│                    API Gateway                          │
└──────────┬──────────────────────────────┬───────────────┘
           │                              │
    ┌──────▼──────┐               ┌───────▼──────┐
    │ Order Service│               │ Inventory    │
    │ (Hexagonal) │               │ Service      │
    │             │               │ (Hexagonal)  │
    │ ┌─────────┐ │    Events     │              │
    │ │  CQRS   │ │──────────────▶│              │
    │ │+ Event  │ │               │              │
    │ │Sourcing │ │               │              │
    │ └─────────┘ │               │              │
    └─────────────┘               └──────────────┘
```

- **Microservices** define service boundaries
- **Hexagonal** structures each service internally
- **CQRS** separates read/write within a service
- **Event Sourcing** provides the write-side persistence for CQRS

### Typical Combination

| Pattern | Scope | Purpose |
|---------|-------|---------|
| Microservices | System | Service boundaries, independent deployment |
| Hexagonal | Service | Clean internal structure, testability |
| CQRS | Service | Optimize reads and writes independently |
| Event Sourcing | Aggregate | Full audit trail, temporal queries |

---

## 6. Decision Guide

### When to Use What

```
Start simple (monolith + layered architecture)
    │
    ├── Need independent deployment? ──▶ Microservices
    │
    ├── Need framework independence / testability? ──▶ Hexagonal
    │
    ├── Read/write performance mismatch? ──▶ CQRS
    │
    └── Need audit trail / temporal queries? ──▶ Event Sourcing
```

### Complexity Cost

| Pattern | Added Complexity | Justification Threshold |
|---------|-----------------|------------------------|
| Hexagonal | Low | Almost always worthwhile for non-trivial services |
| CQRS (same DB) | Low-Medium | When read models diverge from write models |
| Microservices | Medium-High | Multiple teams, independent scaling needs |
| CQRS (separate DBs) | High | Extreme read/write scaling differences |
| Event Sourcing | High | Regulatory audit, temporal queries, event-driven core |

### Anti-Patterns to Avoid

- **Distributed monolith**: Microservices that must deploy together defeat the purpose
- **CQRS everywhere**: Don't apply CQRS to simple CRUD — it adds overhead with no benefit
- **Event sourcing for simple state**: If you just need current state, a regular database is simpler
- **Hexagonal ceremony in scripts**: One-off tools don't need ports and adapters
- **Shared databases across microservices**: Couples services at the data layer

---

## Further Reading

- *Building Microservices* by Sam Newman
- *Implementing Domain-Driven Design* by Vaughn Vernon
- *Get Your Hands Dirty on Clean Architecture* by Tom Hombergs (hexagonal with Spring Boot)
- [Microsoft CQRS Pattern](https://learn.microsoft.com/en-us/azure/architecture/patterns/cqrs)
- [Martin Fowler — Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)


---

## See Also

- [Main README](../README.md) — Project overview and quick start
- [Best Practices](best-practices.md) — Code style and conventions
- [Extending the Template](EXTENDING.md) — Adding modules and endpoints
- [Tutorial](TUTORIAL.md) — New developer walkthrough
