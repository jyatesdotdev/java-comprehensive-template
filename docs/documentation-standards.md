# Documentation Standards

Conventions for JavaDoc, README files, and API documentation in Java projects.

---

## Table of Contents

1. [JavaDoc Conventions](#javadoc-conventions)
2. [README Templates](#readme-templates)
3. [OpenAPI / Swagger Integration](#openapi--swagger-integration)
4. [Changelog & Release Notes](#changelog--release-notes)

---

## JavaDoc Conventions

### When to Write JavaDoc

| Element | Required? | Notes |
|---------|-----------|-------|
| Public class / interface | **Yes** | Describe purpose and thread-safety |
| Public method | **Yes** | Describe contract, params, return, exceptions |
| Package (`package-info.java`) | **Yes** | One-sentence summary of the package |
| Private / package-private | Optional | Add when logic is non-obvious |
| Record components | **Yes** | Document via the record-level JavaDoc |
| Constants | **Yes** | Explain meaning, not just the value |

### Standard Tags (in order)

```java
/**
 * Summary sentence — ends with a period.
 *
 * <p>Optional extended description. Use {@code code} for inline code
 * and {@link ClassName} for cross-references.
 *
 * @param name  description (for each parameter)
 * @param <T>   description (for each type parameter)
 * @return description of the return value
 * @throws IllegalArgumentException if name is blank
 * @see OtherClass#relatedMethod()
 * @since 1.2.0
 * @deprecated Use {@link #newMethod()} instead.
 */
```

### Class-Level Example

```java
/**
 * Thread-safe in-memory cache with TTL-based expiration.
 *
 * <p>Entries are evicted lazily on access. For size-bounded caching,
 * prefer Guava's {@link com.google.common.cache.Cache}.
 *
 * <p>Usage:
 * <pre>{@code
 * var cache = new TtlCache<String, User>(Duration.ofMinutes(5));
 * cache.put("key", user);
 * Optional<User> hit = cache.get("key");
 * }</pre>
 *
 * @param <K> the key type
 * @param <V> the value type
 * @since 1.0.0
 */
public class TtlCache<K, V> { }
```

### Method-Level Example

```java
/**
 * Finds products matching the given filter criteria.
 *
 * <p>Returns an empty list when no products match — never {@code null}.
 *
 * @param filter  non-null filter; use {@link ProductFilter#all()} for no filtering
 * @param pageable pagination and sorting parameters
 * @return page of matching products, never {@code null}
 * @throws IllegalArgumentException if filter is {@code null}
 */
public Page<Product> search(ProductFilter filter, Pageable pageable) { }
```

### Record Example

```java
/**
 * Immutable product response returned by the REST API.
 *
 * @param id          unique product identifier
 * @param name        display name
 * @param price       unit price, always positive
 * @param createdAt   creation timestamp (UTC)
 */
public record ProductResponse(UUID id, String name, BigDecimal price, Instant createdAt) { }
```

### Package-Info Example

Create `src/main/java/com/example/template/restfulapi/package-info.java`:

```java
/**
 * RESTful API layer — controllers, DTOs, and exception handling.
 *
 * <p>Controllers delegate to the service layer and never expose domain entities.
 */
package com.example.template.restfulapi;
```

### Common Mistakes to Avoid

- **Restating the method name**: `/** Gets the name. */` adds nothing — describe the contract instead.
- **Missing `@throws`**: Document every checked exception and significant unchecked ones.
- **Stale docs**: Update JavaDoc when you change method behavior. Treat it as part of the code change.
- **HTML in summaries**: Keep the first sentence plain text; use `<p>` only in extended descriptions.

### Maven JavaDoc Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.6.3</version>
    <configuration>
        <doclint>all,-missing</doclint>
        <source>17</source>
        <quiet>true</quiet>
    </configuration>
    <executions>
        <execution>
            <id>attach-javadocs</id>
            <goals><goal>jar</goal></goals>
        </execution>
    </executions>
</plugin>
```

Generate docs: `mvn javadoc:javadoc` → output in `target/site/apidocs/`.

---

## README Templates

### Project Root README

```markdown
# Project Name

One-line description of what this project does.

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker (for integration tests)

## Quick Start

```bash
git clone https://github.com/org/project.git
cd project
mvn clean verify
```

## Project Structure

```
project/
├── module-api/          # REST API layer
├── module-core/         # Domain logic
├── module-persistence/  # Database access
└── docs/                # Architecture & guides
```

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | HTTP listen port |
| `spring.datasource.url` | H2 in-memory | JDBC connection URL |

## Running

```bash
# Development
mvn spring-boot:run -pl module-api

# Production
java -jar module-api/target/module-api-1.0.0.jar --spring.profiles.active=prod
```

## Testing

```bash
mvn test                          # Unit tests
mvn verify -P integration-tests   # Integration tests (requires Docker)
```

## API Documentation

After starting the application, visit:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Contributing

See the [Development Workflow](development-workflow.md) for branching, commit conventions, and CI/CD guidelines.

## See Also

- [Main README](../README.md) — Project overview and quick start
- [Best Practices](best-practices.md) — Code style and naming conventions
- [Development Workflow](development-workflow.md) — CI/CD and quality gates
- [Tutorial](TUTORIAL.md) — New developer walkthrough

## License

[Apache 2.0](LICENSE)
```

### Module README

```markdown
# Module Name

What this module provides and its role in the project.

## Dependencies

This module depends on:
- `module-core` — domain model and business rules

## Key Classes

| Class | Purpose |
|-------|---------|
| `ProductController` | REST endpoints for product CRUD |
| `ProductService` | Business logic interface |

## Usage

```java
// Example of using this module's API
var service = context.getBean(ProductService.class);
var product = service.findById(id);
```

## Configuration

Module-specific properties in `application.yml`.
```

---

## OpenAPI / Swagger Integration

### Dependency (springdoc-openapi)

Add to the REST module's `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

This auto-configures:
- **Swagger UI** at `/swagger-ui.html`
- **OpenAPI 3.0 JSON** at `/v3/api-docs`
- **OpenAPI 3.0 YAML** at `/v3/api-docs.yaml`

### Application Configuration

```yaml
# application.yml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha
  default-produces-media-type: application/json
  default-consumes-media-type: application/json
```

### Global API Info (OpenApiConfig.java)

```java
package com.example.template.restfulapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product API")
                        .version("1.0.0")
                        .description("CRUD API for product management")
                        .contact(new Contact().name("Team").email("team@example.com"))
                        .license(new License().name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

### Annotating Controllers

```java
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product CRUD operations")
public class ProductController {

    @Operation(
        summary = "Create a product",
        description = "Creates a new product and returns it with a generated ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        // ...
    }
}
```

### Annotating DTOs

```java
@Schema(description = "Request payload for creating or updating a product")
public record ProductRequest(
        @Schema(description = "Product name", example = "Widget", requiredMode = REQUIRED)
        @NotBlank String name,

        @Schema(description = "Product description", example = "A useful widget")
        String description,

        @Schema(description = "Unit price", example = "29.99", requiredMode = REQUIRED)
        @NotNull @Positive BigDecimal price
) {}
```

### Generating Static OpenAPI Spec at Build Time

Use the `springdoc-openapi-maven-plugin` to export the spec during the build:

```xml
<plugin>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-maven-plugin</artifactId>
    <version>1.4</version>
    <executions>
        <execution>
            <id>generate-openapi</id>
            <phase>integration-test</phase>
            <goals><goal>generate</goal></goals>
        </execution>
    </executions>
    <configuration>
        <apiDocsUrl>http://localhost:8080/v3/api-docs.yaml</apiDocsUrl>
        <outputFileName>openapi.yaml</outputFileName>
        <outputDir>${project.build.directory}</outputDir>
    </configuration>
</plugin>
```

### Code-First vs. Contract-First

| Approach | When to Use | Tooling |
|----------|-------------|---------|
| **Code-first** | Internal APIs, rapid prototyping | springdoc-openapi (annotations → spec) |
| **Contract-first** | Public APIs, multi-team | OpenAPI Generator (spec → code) |

For contract-first, write `openapi.yaml` manually, then generate server stubs:

```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.4.0</version>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
            <configuration>
                <inputSpec>${project.basedir}/src/main/resources/openapi.yaml</inputSpec>
                <generatorName>spring</generatorName>
                <configOptions>
                    <interfaceOnly>true</interfaceOnly>
                    <useSpringBoot3>true</useSpringBoot3>
                    <useJakartaEe>true</useJakartaEe>
                </configOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## Changelog & Release Notes

### CHANGELOG.md Format (Keep a Changelog)

```markdown
# Changelog

All notable changes to this project will be documented in this file.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
adhering to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- OpenAPI documentation for Product API

## [1.1.0] - 2026-03-15

### Added
- Batch product import endpoint

### Changed
- Upgraded Spring Boot to 3.3.5

### Fixed
- Price rounding error on currency conversion

## [1.0.0] - 2026-01-10

### Added
- Initial product CRUD API
- JPA persistence with PostgreSQL
- Docker Compose development setup
```

### Categories

Use these section headers consistently:
- **Added** — new features
- **Changed** — changes to existing functionality
- **Deprecated** — features to be removed
- **Removed** — removed features
- **Fixed** — bug fixes
- **Security** — vulnerability fixes

---

## Summary Checklist

- [ ] Every public class and method has JavaDoc
- [ ] `package-info.java` exists for each package
- [ ] Root README has quick start, structure, config, and run instructions
- [ ] Each module has its own README
- [ ] OpenAPI annotations on all REST controllers and DTOs
- [ ] Swagger UI accessible in development
- [ ] CHANGELOG.md maintained with each release
- [ ] `mvn javadoc:javadoc` runs without warnings
