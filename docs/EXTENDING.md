# Extending the Template

How to add new modules, endpoints, services, dependencies, and quality rules to this project.

> See also: [Architecture Patterns](architecture-patterns.md) · [Best Practices](best-practices.md) · [Toolchain](TOOLCHAIN.md)

---

## Adding a New Maven Module

The project uses a multi-module layout under `examples/`. Each module has its own `pom.xml` that inherits from the root.

### 1. Create the directory structure

```
examples/my-module/
├── pom.xml
└── src/
    ├── main/java/com/example/template/mymodule/
    └── test/java/com/example/template/mymodule/
```

### 2. Create the module `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example.template</groupId>
        <artifactId>java-enterprise-template</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>template-my-module</artifactId>
    <name>My Module</name>
    <description>Brief description of what this module demonstrates</description>

    <dependencies>
        <!-- Add module-specific dependencies here.
             Common deps (SLF4J, JUnit, AssertJ, Mockito) are inherited from the parent. -->
    </dependencies>
</project>
```

### 3. Register in the root `pom.xml`

Add the module to the `<modules>` block:

```xml
<modules>
    <!-- existing modules -->
    <module>examples/my-module</module>
</modules>
```

### 4. Verify

```bash
./mvnw compile -pl examples/my-module
```

---

## Adding a New REST Endpoint

This follows the pattern in `examples/restful-api/`. The layers are: **Controller → Service → Domain**, with DTOs at the boundary.

### 1. Define the domain model

```java
package com.example.template.restfulapi.domain;

public class Order {
    private UUID id;
    private String customerName;
    private BigDecimal total;

    // constructor, getters, setters
}
```

### 2. Create request/response DTOs

```java
package com.example.template.restfulapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderRequest(
    @NotBlank String customerName,
    @Positive BigDecimal total
) {}
```

```java
package com.example.template.restfulapi.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderResponse(UUID id, String customerName, BigDecimal total) {}
```

### 3. Define the service interface

```java
package com.example.template.restfulapi.service;

public interface OrderService {
    List<Order> findAll();
    Order findById(UUID id);
    Order create(OrderRequest request);
}
```

### 4. Implement the service

```java
package com.example.template.restfulapi.service;

import org.springframework.stereotype.Service;

@Service
public class InMemoryOrderService implements OrderService {
    private final Map<UUID, Order> store = new ConcurrentHashMap<>();

    @Override
    public List<Order> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Order findById(UUID id) {
        var order = store.get(id);
        if (order == null) {
            throw new ResourceNotFoundException("Order", id);
        }
        return order;
    }

    @Override
    public Order create(OrderRequest request) {
        var order = new Order(UUID.randomUUID(), request.customerName(), request.total());
        store.put(order.getId(), order);
        return order;
    }
}
```

### 5. Add the controller

```java
package com.example.template.restfulapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order operations")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> list() {
        return orderService.findAll().stream()
            .map(o -> new OrderResponse(o.getId(), o.getCustomerName(), o.getTotal()))
            .toList();
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        var order = orderService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/orders/" + order.getId()))
            .body(new OrderResponse(order.getId(), order.getCustomerName(), order.getTotal()));
    }
}
```

Key conventions:
- Controllers never expose domain objects directly — use DTOs
- Use `@Valid` on request bodies for bean validation
- Return `201 Created` with a `Location` header for POST
- Add OpenAPI annotations (`@Operation`, `@ApiResponse`, `@Tag`) for Swagger docs

---

## Adding a New Service

Services that don't need a REST layer follow the same interface + implementation pattern.

```java
// Interface
package com.example.template.mymodule.service;

public interface NotificationService {
    void send(String recipient, String message);
}
```

```java
// Implementation — Spring discovers this via component scanning
package com.example.template.mymodule.service;

import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    @Override
    public void send(String recipient, String message) {
        // implementation
    }
}
```

Inject via constructor (no `@Autowired` needed when there's a single constructor):

```java
public class OrderController {
    private final OrderService orderService;
    private final NotificationService notificationService;

    public OrderController(OrderService orderService, NotificationService notificationService) {
        this.orderService = orderService;
        this.notificationService = notificationService;
    }
}
```

---

## Adding a Third-Party Dependency

### Managed dependency (version in root POM)

This is the preferred approach — it keeps versions consistent across all modules.

**1. Add to `<dependencyManagement>` in the root `pom.xml`:**

```xml
<properties>
    <caffeine.version>3.1.8</caffeine.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
            <version>${caffeine.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**2. Use in the child module (no version needed):**

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

### Module-local dependency

For dependencies only one module needs, declare the version directly in the child `pom.xml`:

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>niche-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

### After adding a dependency

Run the OWASP dependency check to verify no known vulnerabilities:

```bash
./mvnw verify -Psecurity-scan -pl examples/my-module
```

---

## Adding a Quality Rule or Suppression

The project uses four static analysis tools. Each has a config file at the project root.

### Checkstyle

| File | Purpose |
|------|---------|
| `checkstyle.xml` | Rule definitions |
| `checkstyle-suppressions.xml` | Suppressions for specific files/rules |

**Add a suppression** (e.g., allow long methods in a specific file):

```xml
<!-- checkstyle-suppressions.xml -->
<suppress files="MyLegacyClass\.java" checks="MethodLength"/>
```

**Suppress inline** with a comment:

```java
@SuppressWarnings("checkstyle:MagicNumber")
private static final int TIMEOUT = 30;
```

### PMD

| File | Purpose |
|------|---------|
| `pmd-ruleset.xml` | Rule inclusions/exclusions |

**Exclude a rule globally:**

```xml
<rule ref="category/java/bestpractices.xml">
    <exclude name="AvoidReassigningParameters"/>
</rule>
```

**Suppress inline:**

```java
@SuppressWarnings("PMD.ShortVariable")
int x = computeValue();
```

### SpotBugs

| File | Purpose |
|------|---------|
| `spotbugs-exclude.xml` | Exclusion filter |

**Suppress a specific bug pattern on a class:**

```xml
<Match>
    <Class name="com.example.template.mymodule.MyClass"/>
    <Bug pattern="EI_EXPOSE_REP"/>
</Match>
```

### OWASP Dependency Check

| File | Purpose |
|------|---------|
| `owasp-suppressions.xml` | CVE false-positive suppressions |

**Suppress a false-positive CVE:**

```xml
<suppress>
    <notes><![CDATA[False positive — we don't use the affected feature. Reviewed 2024-03-15.]]></notes>
    <gav regex="true">^com\.example:my-library:.*$</gav>
    <cve>CVE-2024-XXXXX</cve>
</suppress>
```

Always include a `<notes>` element explaining why the suppression is safe and when it was reviewed.

### Running quality checks

```bash
# Full scan (SpotBugs + Checkstyle + PMD + OWASP)
./mvnw verify -Psecurity-scan

# Quick scan (no OWASP — faster for local dev)
./mvnw verify -Psecurity-scan-quick

# Single tool
./mvnw checkstyle:check
./mvnw pmd:check
./mvnw spotbugs:check
```

---

## Related Documentation

- [Architecture Patterns](architecture-patterns.md) — project structure and layer design
- [Best Practices](best-practices.md) — coding standards and conventions
- [Security Scanning](SECURITY_SCANNING.md) — detailed security tool configuration
- [Third-Party Libraries](third-party-libraries.md) — dependency catalog and selection criteria
- [Toolchain](TOOLCHAIN.md) — tool installation and IDE setup
- [Development Workflow](development-workflow.md) — branching, CI, and release process
