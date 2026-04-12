# Third-Party Libraries Guide

A curated reference of libraries used in this template, with usage examples and guidance on when to reach for each one. All versions are managed in the root `pom.xml` `<properties>` block.

---

## Table of Contents

1. [Spring Ecosystem](#1-spring-ecosystem)
2. [Apache Commons](#2-apache-commons)
3. [Google Guava](#3-google-guava)
4. [Jackson](#4-jackson)
5. [Lombok](#5-lombok)
6. [MapStruct](#6-mapstruct)
7. [Resilience4j](#7-resilience4j)
8. [Logging (SLF4J + Logback)](#8-logging-slf4j--logback)
9. [Database Libraries](#9-database-libraries)
10. [Build & Quality Plugins](#10-build--quality-plugins)
11. [Library Selection Decision Guide](#11-library-selection-decision-guide)

---

## 1. Spring Ecosystem

**Version**: Spring Boot `3.3.x` (manages transitive Spring Framework, Security, Data versions)

### Spring Boot Starter Web

REST controllers, embedded Tomcat, JSON serialization.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

```java
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService service;

    ItemController(ItemService service) { this.service = service; }

    @GetMapping("/{id}")
    ResponseEntity<Item> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
```

### Spring Boot Starter WebFlux

Reactive, non-blocking HTTP client and server.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

```java
// Reactive HTTP client
WebClient client = WebClient.create("https://api.example.com");
Mono<Item> item = client.get()
    .uri("/items/{id}", 42)
    .retrieve()
    .bodyToMono(Item.class);
```

**When to use**: Prefer WebFlux when you need non-blocking I/O or are building a gateway/proxy that handles many concurrent connections. Stick with `starter-web` for standard CRUD services.

### Spring Boot Starter Data JPA

JPA repositories, Hibernate auto-configuration, transaction management.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
```

### Spring Boot Starter Validation

Bean Validation (Jakarta Validation) integration.

```java
public record CreateItemRequest(
    @NotBlank @Size(max = 100) String name,
    @Positive BigDecimal price,
    @NotNull Category category
) {}
```

### Spring Boot Starter Security

Authentication, authorization, CSRF, CORS.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

### Spring Boot Starter Actuator

Health checks, metrics, info endpoints for production monitoring.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
```

---

## 2. Apache Commons

Battle-tested utility libraries that fill gaps in the JDK.

### Commons Lang3 (`3.14.0`)

String manipulation, reflection helpers, builder utilities.

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
```

```java
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

// Null-safe string operations
StringUtils.isBlank(null);          // true
StringUtils.isBlank("  ");          // true
StringUtils.defaultIfBlank(s, "N/A");
StringUtils.abbreviate(longText, 50);

// Tuples (when a full class is overkill)
Pair<String, Integer> result = Pair.of("count", 42);
```

**When to use vs JDK**: Prefer JDK `String.isBlank()` for simple checks. Use Commons Lang when you need null-safe operations or utilities like `StringUtils.substringBetween()`, `WordUtils`, or `RandomStringUtils`.

### Commons IO (`2.16.1`)

File/stream utilities, file filters, file monitoring.

```xml
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
</dependency>
```

```java
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FilenameUtils;

// Read file to string
String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

// Copy stream (auto-closes)
IOUtils.copy(inputStream, outputStream);

// Safe filename operations
FilenameUtils.getExtension("report.pdf");   // "pdf"
FilenameUtils.getBaseName("/tmp/report.pdf"); // "report"
```

**When to use vs JDK**: Java's `Files.readString()` and `Path` API cover most cases since Java 11. Use Commons IO for `FileUtils.listFiles()` with filters, `IOUtils.toByteArray()`, or `FileAlterationMonitor` for file watching.

### Commons Collections4 (`4.4`)

Enhanced collection types: multi-maps, bidirectional maps, ordered maps.

```java
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

// One key → many values
MultiValuedMap<String, String> tagMap = new ArrayListValuedHashMap<>();
tagMap.put("java", "Spring");
tagMap.put("java", "Quarkus");
tagMap.get("java"); // [Spring, Quarkus]

// Bidirectional lookup
BidiMap<String, String> codeToCountry = new DualHashBidiMap<>();
codeToCountry.put("US", "United States");
codeToCountry.getKey("United States"); // "US"
```

---

## 3. Google Guava (`33.1.0-jre`)

Google's core Java library — caching, collections, hashing, concurrency utilities.

```xml
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
</dependency>
```

### Immutable Collections

```java
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

// Prefer JDK List.of() / Map.of() for simple cases.
// Guava shines for builders and larger collections:
ImmutableMap<String, Integer> scores = ImmutableMap.<String, Integer>builder()
    .put("alice", 95)
    .put("bob", 87)
    .buildOrThrow(); // throws on duplicate keys
```

### Caching

```java
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

LoadingCache<String, User> userCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(Duration.ofMinutes(10))
    .recordStats()
    .build(CacheLoader.from(userService::findByUsername));

User user = userCache.getUnchecked("alice"); // loads on miss
```

**Note**: For Spring applications, consider Spring's `@Cacheable` with Caffeine (Guava cache's successor) instead:

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

### Preconditions & Strings

```java
import com.google.common.base.Preconditions;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

Preconditions.checkArgument(age > 0, "Age must be positive, got: %s", age);
Preconditions.checkNotNull(name, "name is required");

// Null-skipping joiner
Joiner.on(", ").skipNulls().join("a", null, "b"); // "a, b"

// Trimming splitter
Splitter.on(',').trimResults().omitEmptyStrings().split("a, b, , c"); // [a, b, c]
```

### Multimap & Table

```java
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

// Multimap: one key → many values
var multimap = ArrayListMultimap.<String, String>create();
multimap.put("fruit", "apple");
multimap.put("fruit", "banana");

// Table: row × column → value
Table<String, String, Double> grades = HashBasedTable.create();
grades.put("Alice", "Math", 95.0);
grades.put("Alice", "English", 88.0);
grades.row("Alice"); // {Math=95.0, English=88.0}
```

---

## 4. Jackson (`2.17.x`)

The standard JSON (and XML, YAML, CSV) serialization library for Java.

Spring Boot auto-configures Jackson, but you can customize it:

### Basic Usage

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

ObjectMapper mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

// Serialize
String json = mapper.writeValueAsString(order);

// Deserialize
Order order = mapper.readValue(json, Order.class);

// Generic types
List<Order> orders = mapper.readValue(json, new TypeReference<>() {});
```

### Annotations

```java
public record OrderDto(
    @JsonProperty("order_id") Long id,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate orderDate,
    @JsonIgnore String internalNote,
    @JsonInclude(JsonInclude.Include.NON_NULL) String optionalField
) {}
```

### Spring Boot Customization

```java
@Configuration
public class JacksonConfig {

    @Bean
    Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .featuresToEnable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .mixIn(ThirdPartyClass.class, ThirdPartyMixin.class);
    }
}
```

---

## 5. Lombok

Compile-time code generation to reduce boilerplate. **Use sparingly** — Java records and modern IDE features reduce the need.

### Maven Setup

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

```xml
<!-- Required for annotation processing -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### Recommended Annotations

```java
@Slf4j                    // private static final Logger log = ...
@RequiredArgsConstructor  // constructor for final fields (DI-friendly)
@Builder                  // fluent builder pattern
@Value                    // immutable class (prefer records for DTOs)
public class OrderProcessor {
    private final OrderRepository repository;
    private final NotificationService notifications;

    public void process(Order order) {
        log.info("Processing order {}", order.getId());
        // ...
    }
}
```

### When to Use Records vs Lombok

| Need | Use |
|------|-----|
| Simple data carrier / DTO | `record` |
| Mutable JPA entity | Lombok `@Data` or `@Getter`/`@Setter` |
| Builder pattern | Lombok `@Builder` or manual builder |
| Immutable with many fields | Lombok `@Value` + `@Builder` |
| Logging field | Lombok `@Slf4j` |

**Caution**: Avoid `@Data` on JPA entities — its `equals`/`hashCode` can cause issues with lazy-loaded proxies. Use `@Getter`/`@Setter` and implement `equals`/`hashCode` on the business key.

---

## 6. MapStruct (`1.5.5.Final`)

Compile-time bean mapping — generates type-safe, zero-reflection mapping code.

### Maven Setup

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>
```

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
            </path>
            <!-- If using Lombok too, add lombok-mapstruct-binding -->
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### Usage

```java
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "totalAmount", expression = "java(order.calculateTotal())")
    OrderDto toDto(Order order);

    List<OrderDto> toDtoList(List<Order> orders);

    @InheritInverseConfiguration
    @Mapping(target = "id", ignore = true)
    Order toEntity(OrderDto dto);
}
```

MapStruct generates the implementation at compile time — no reflection, no runtime overhead. Inject it like any Spring bean:

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderMapper mapper;

    public OrderDto getOrder(Long id) {
        return mapper.toDto(repository.findById(id).orElseThrow());
    }
}
```

---

## 7. Resilience4j

Fault tolerance library for microservices — circuit breaker, retry, rate limiter, bulkhead.

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>
```

### Circuit Breaker

```java
@Service
public class PaymentService {

    @CircuitBreaker(name = "payment", fallbackMethod = "paymentFallback")
    @Retry(name = "payment")
    public PaymentResult charge(PaymentRequest request) {
        return paymentGateway.process(request);
    }

    private PaymentResult paymentFallback(PaymentRequest request, Throwable t) {
        log.warn("Payment circuit open, queuing for retry: {}", t.getMessage());
        return PaymentResult.queued(request.id());
    }
}
```

```yaml
resilience4j:
  circuitbreaker:
    instances:
      payment:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
  retry:
    instances:
      payment:
        max-attempts: 3
        wait-duration: 500ms
```

---

## 8. Logging (SLF4J + Logback)

SLF4J (`2.0.x`) is the facade; Logback (`1.5.x`) is the implementation. Spring Boot auto-configures both.

### Usage

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    // Or use Lombok: @Slf4j

    public void process(Order order) {
        log.info("Processing order id={} items={}", order.getId(), order.getItems().size());
        log.debug("Order details: {}", order); // toString() only called if DEBUG enabled
    }
}
```

### Structured Logging (Logstash Encoder)

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    <root level="INFO">
        <appender-ref ref="JSON"/>
    </root>
</configuration>
```

---

## 9. Database Libraries

### HikariCP (Connection Pooling)

Bundled with Spring Boot. Configure via `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
```

### Flyway (Database Migrations)

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

Migrations go in `src/main/resources/db/migration/`:

```
V1__create_orders.sql
V2__add_order_status_index.sql
V3__add_audit_columns.sql
```

### H2 (In-Memory Testing)

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 10. Build & Quality Plugins

| Plugin | Purpose | Activation |
|--------|---------|------------|
| `maven-compiler-plugin` | Compile with `-parameters` for Spring | Always |
| `maven-surefire-plugin` | Unit tests (`*Test.java`) | `mvn test` |
| `maven-failsafe-plugin` | Integration tests (`*IT.java`) | `mvn verify -Pintegration-tests` |
| `jacoco-maven-plugin` | Code coverage reports | `mvn verify` → `target/site/jacoco/` |
| `spotless-maven-plugin` | Google Java Format | `mvn spotless:apply -Pformat` |
| `spotbugs-maven-plugin` | Static bug detection | `mvn spotbugs:check` |

### Adding SpotBugs

```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>${spotbugs-plugin.version}</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Medium</threshold>
    </configuration>
</plugin>
```

---

## 11. Library Selection Decision Guide

### "Which HTTP client should I use?"

| Scenario | Library |
|----------|---------|
| Spring MVC app, simple calls | `RestClient` (Spring 6.1+) |
| Reactive / non-blocking | `WebClient` |
| Non-Spring project | Java `HttpClient` (JDK 11+) |
| JAX-RS ecosystem | Jersey Client |

### "Do I need Guava?"

Modern Java (17+) covers many former Guava use cases:
- `List.of()`, `Map.of()` → immutable collections
- `Objects.requireNonNull()` → null checks
- `String.isBlank()` → blank checks
- `Stream.toList()` → collecting

**Still useful in Guava**: `LoadingCache` (or use Caffeine), `Multimap`, `Table`, `RateLimiter`, `Splitter`/`Joiner` with advanced options.

### "Records vs Lombok?"

Use records for DTOs, API responses, value objects. Use Lombok for mutable JPA entities and classes needing `@Builder` or `@Slf4j`.

### "MapStruct vs manual mapping?"

Use MapStruct when you have many entity↔DTO conversions with consistent patterns. For 1-2 simple mappings, a static factory method is fine.

---

## Quick Reference: All Managed Dependencies

| Library | Property | Version |
|---------|----------|---------|
| Spring Boot | `spring-boot.version` | 3.3.5 |
| Hibernate | `hibernate.version` | 6.4.4.Final |
| Flyway | `flyway.version` | 10.10.0 |
| HikariCP | `hikaricp.version` | 5.1.0 |
| H2 | `h2.version` | 2.2.224 |
| PostgreSQL Driver | `postgresql.version` | 42.7.3 |
| Apache Spark | `spark.version` | 3.5.1 |
| Commons Lang3 | `commons-lang3.version` | 3.14.0 |
| Commons IO | `commons-io.version` | 2.16.1 |
| Commons Collections4 | `commons-collections4.version` | 4.4 |
| Guava | `guava.version` | 33.1.0-jre |
| Jackson | `jackson.version` | 2.17.0 |
| MapStruct | `mapstruct.version` | 1.5.5.Final |
| SLF4J | `slf4j.version` | 2.0.12 |
| Logback | `logback.version` | 1.5.3 |
| JUnit 5 | `junit-jupiter.version` | 5.10.2 |
| Mockito | `mockito.version` | 5.11.0 |
| AssertJ | `assertj.version` | 3.25.3 |
| TestContainers | `testcontainers.version` | 1.19.7 |
| REST Assured | `rest-assured.version` | 5.4.0 |
| ArchUnit | `archunit.version` | 1.2.1 |

All versions are centralized in the root `pom.xml` `<properties>` block. Child modules inherit versions through `<dependencyManagement>` — never hardcode versions in submodules.


---

## See Also

- [Main README](../README.md) — Project overview and quick start
- [Best Practices](best-practices.md) — Code style and conventions
- [Extending the Template](EXTENDING.md) — Adding a third-party dependency
- [Security Scanning](SECURITY_SCANNING.md) — OWASP dependency scanning
