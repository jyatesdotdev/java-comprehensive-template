# New Developer Tutorial

A hands-on walkthrough from cloning the project to building a Docker image. Estimated time: 30 minutes.

> **Prerequisites**: JDK 21+, Docker, Git. See [Toolchain](TOOLCHAIN.md) for installation instructions.

---

## Table of Contents

1. [Clone & Verify Setup](#1-clone--verify-setup)
2. [Build the Project](#2-build-the-project)
3. [Run the REST API](#3-run-the-rest-api)
4. [Explore the API](#4-explore-the-api)
5. [Run Other Examples](#5-run-other-examples)
6. [Add a Feature](#6-add-a-feature)
7. [Run Quality & Security Scans](#7-run-quality--security-scans)
8. [Docker Build & Run](#8-docker-build--run)
9. [Next Steps](#9-next-steps)

---

## 1. Clone & Verify Setup

```bash
git clone <repository-url>
cd java-enterprise-template
```

Verify your tools:

```bash
java -version          # 17+ required, 21 recommended
./mvnw --version       # Maven wrapper — no global install needed
docker --version       # Required for integration tests and Docker builds
```

If any command fails, see [Toolchain — Installation](TOOLCHAIN.md#installation).

---

## 2. Build the Project

```bash
./mvnw clean verify
```

This compiles all 8 example modules, runs unit tests, and generates coverage reports. On first run, Maven downloads dependencies — expect 3–5 minutes. Subsequent builds are faster.

What just happened:

| Phase | What it does |
|-------|-------------|
| `compile` | Compiles all modules (Java 17 source level) |
| `test` | Runs unit tests via Surefire |
| `verify` | Runs JaCoCo coverage checks |

To run only unit tests (faster):

```bash
./mvnw test
```

To build a single module:

```bash
./mvnw -pl examples/restful-api -am test
```

The `-am` flag ("also make") builds any parent/dependency modules needed.

---

## 3. Run the REST API

```bash
./mvnw -pl examples/restful-api spring-boot:run
```

You should see:

```
Started RestApiApplication in X.XX seconds
```

The server is now running at `http://localhost:8080`.

Open Swagger UI in your browser: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 4. Explore the API

With the server running (from step 3), open a new terminal:

```bash
# Create a product
curl -s -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{"name": "Widget", "price": 9.99}' | jq .

# List all products
curl -s http://localhost:8080/api/products | jq .

# Get a specific product (use the ID from the create response)
curl -s http://localhost:8080/api/products/1 | jq .

# Update a product
curl -s -X PUT http://localhost:8080/api/products/1 \
  -H 'Content-Type: application/json' \
  -d '{"name": "Super Widget", "price": 19.99}' | jq .

# Delete a product
curl -s -X DELETE http://localhost:8080/api/products/1
```

Stop the server with `Ctrl+C` when done.

---

## 5. Run Other Examples

Most modules are libraries, not servers. Run their tests to see them in action:

```bash
# Design patterns — 15 GoF patterns with modern Java
./mvnw -pl examples/patterns test

# Database — JPA, JDBC, transactions (uses H2 in-memory)
./mvnw -pl examples/database test

# HPC — parallel streams, CompletableFuture, concurrency
./mvnw -pl examples/hpc test

# Simulation — Monte Carlo, discrete event simulation
./mvnw -pl examples/simulation test

# Testing — JUnit 5 features, Mockito, ArchUnit
./mvnw -pl examples/testing test
```

For integration tests (requires Docker running):

```bash
./mvnw -pl examples/testing verify -P integration-tests
```

Each module has its own README with details — see [examples/](../examples/).

---

## 6. Add a Feature

Let's add a health-check endpoint to the REST API to see the full workflow.

### 6a. Create the controller

Create `examples/restful-api/src/main/java/com/example/template/restfulapi/controller/HealthController.java`:

```java
package com.example.template.restfulapi.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health endpoint returning application status and server time.
 */
@RestController
public class HealthController {

    /**
     * Returns a health status response.
     *
     * @return map containing status and current timestamp
     */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()));
    }
}
```

### 6b. Build and test

```bash
# Compile and run tests for the module
./mvnw -pl examples/restful-api test

# Start the server and test your endpoint
./mvnw -pl examples/restful-api spring-boot:run

# In another terminal:
curl -s http://localhost:8080/api/health | jq .
```

Expected response:

```json
{
  "status": "UP",
  "timestamp": "2026-04-11T23:00:00Z"
}
```

### 6c. Run the full build

Always run the full build before committing:

```bash
./mvnw clean verify
```

For more on extending the project (new modules, services, dependencies), see [Extending the Template](EXTENDING.md).

---

## 7. Run Quality & Security Scans

### Static analysis (SpotBugs + Checkstyle + PMD)

```bash
./mvnw verify -Psecurity-scan-quick
```

This runs bug detection, code style checks, and static analysis across all modules. Fix any violations before committing.

### OWASP dependency vulnerability scan

```bash
./mvnw verify -Psecurity-scan
```

This checks all dependencies against the National Vulnerability Database. First run downloads the CVE database (~10 minutes); subsequent runs are incremental.

### Code formatting

```bash
# Check formatting
./mvnw spotless:check -Pformat

# Auto-fix formatting
./mvnw spotless:apply -Pformat
```

### Coverage report

After `./mvnw verify`, open the JaCoCo report:

```bash
open examples/restful-api/target/site/jacoco/index.html
```

For detailed scan configuration and suppression guides, see [Security Scanning](SECURITY_SCANNING.md) and [Toolchain — Quality Tools](TOOLCHAIN.md#quality-tools).

---

## 8. Docker Build & Run

```bash
# Package the REST API JAR (skip tests — we already ran them)
./mvnw package -pl examples/restful-api -am -DskipTests

# Build the Docker image
docker build -t java-template-api:latest \
  -f examples/restful-api/Dockerfile examples/restful-api

# Run the container
docker run -p 8080:8080 java-template-api:latest

# Test it
curl -s http://localhost:8080/api/products | jq .
```

The image uses `eclipse-temurin:21-jre-alpine`, runs as a non-root user, and includes a health check on `/actuator/health`.

Stop the container with `Ctrl+C` or `docker stop <container-id>`.

---

## 9. Next Steps

| Want to... | Read |
|-----------|------|
| Add a new module or endpoint | [Extending the Template](EXTENDING.md) |
| Understand the architecture | [Architecture Patterns](architecture-patterns.md) |
| Learn coding conventions | [Best Practices](best-practices.md) |
| Set up your IDE | [Toolchain — IDE Setup](TOOLCHAIN.md#ide-setup) |
| Configure CI/CD | [Development Workflow](development-workflow.md) |
| Write Javadoc and READMEs | [Documentation Standards](documentation-standards.md) |
| Run security scans in CI | [Security Scanning](SECURITY_SCANNING.md) |

---

*See also: [README](../README.md) · [Toolchain](TOOLCHAIN.md) · [Extending](EXTENDING.md) · [Development Workflow](development-workflow.md)*
