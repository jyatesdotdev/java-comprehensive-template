# Java Comprehensive Enterprise Template

A production-ready reference template covering best practices, architecture patterns, and working examples for enterprise Java development (Java 17+).

---

## Table of Contents

- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Example Modules](#example-modules)
  - [RESTful API](#restful-api) — Spring Boot REST server, WebClient, JAX-RS clients
  - [Database](#database) — JPA/Hibernate, JDBC, transactions, connection pooling
  - [High Performance Computing](#high-performance-computing) — Parallel streams, CompletableFuture, virtual threads
  - [ETL & Batch Processing](#etl--batch-processing) — Apache Spark, Spring Batch, data pipelines
  - [Systems Programming](#systems-programming) — JNI, off-heap memory, JMH benchmarks, profiling
  - [Design Patterns](#design-patterns) — 15 GoF patterns with modern Java idioms
  - [Simulation](#simulation) — Monte Carlo, discrete event simulation
  - [Testing](#testing) — JUnit 5, Mockito, TestContainers, REST Assured, ArchUnit
- [Documentation Guides](#documentation-guides)
  - [Tutorial](#tutorial) — New developer walkthrough
  - [Toolchain](#toolchain) — Required tools and IDE setup
  - [Extending the Template](#extending-the-template) — Adding modules, endpoints, dependencies
  - [Best Practices](#best-practices)
  - [Architecture Patterns](#architecture-patterns)
  - [Third-Party Libraries](#third-party-libraries)
  - [Documentation Standards](#documentation-standards)
  - [Development Workflow & CI/CD](#development-workflow--cicd)
  - [Security Scanning](#security-scanning) — SpotBugs, OWASP, PMD, Checkstyle
- [Requirements](#requirements)
- [License](#license)

---

## Quick Start

```bash
# Build all modules
./mvnw clean verify

# Run the REST API example
./mvnw -pl examples/restful-api spring-boot:run

# Run unit tests only
./mvnw test

# Run integration tests (requires Docker)
./mvnw verify -P integration-tests

# Format code
./mvnw spotless:apply -P format
```

## Project Structure

```
├── pom.xml                              # Parent POM — dependency management & plugin config
├── .github/workflows/ci.yml             # GitHub Actions CI/CD pipeline
├── docs/
│   ├── TUTORIAL.md                      # New developer walkthrough (start here!)
│   ├── TOOLCHAIN.md                     # Required tools, install guides, IDE setup
│   ├── EXTENDING.md                     # Adding modules, endpoints, dependencies
│   ├── SECURITY_SCANNING.md             # SpotBugs, OWASP, PMD, Checkstyle config
│   ├── best-practices.md                # Code style, error handling, logging (Java 17+)
│   ├── architecture-patterns.md         # Microservices, hexagonal, CQRS, event sourcing
│   ├── third-party-libraries.md         # Library catalog with usage examples
│   ├── documentation-standards.md       # JavaDoc, OpenAPI/Swagger, README conventions
│   └── development-workflow.md          # CI/CD, adding features, Docker, quality gates
├── examples/
│   ├── restful-api/                     # REST server + 3 client implementations
│   ├── database/                        # JPA entities, repositories, JDBC, Flyway
│   ├── hpc/                             # Parallel streams, virtual threads, concurrency
│   ├── etl/                             # Spark, Spring Batch, pipeline abstraction
│   ├── systems/                         # JNI, off-heap memory, JMH, profiling
│   ├── patterns/                        # Creational, structural, behavioral patterns
│   ├── simulation/                      # Monte Carlo & discrete event simulation
│   └── testing/                         # JUnit 5, Mockito, TestContainers, REST Assured
```

## Example Modules

### RESTful API

**Path:** [`examples/restful-api/`](examples/restful-api/) · **[README](examples/restful-api/README.md)**

Spring Boot 3 REST server with full CRUD, plus three client implementations.

| Component | Description |
|-----------|-------------|
| `ProductController` | REST endpoints with validation, OpenAPI annotations |
| `ProductService` | Service interface + in-memory implementation |
| `GlobalExceptionHandler` | Centralized error handling with `ErrorResponse` DTOs |
| `ProductRestTemplateClient` | Synchronous client (RestTemplate + RestClient) |
| `ProductWebClientExample` | Reactive client (WebFlux WebClient) |
| `ProductJaxRsClient` | JAX-RS/Jersey client |
| `OpenApiConfig` | Swagger UI via springdoc-openapi |

Related docs: [Best Practices](docs/best-practices.md) (error handling), [Documentation Standards](docs/documentation-standards.md) (OpenAPI), [Architecture Patterns](docs/architecture-patterns.md) (microservices)

### Database

**Path:** [`examples/database/`](examples/database/) · **[README](examples/database/README.md)**

JPA/Hibernate entities with Spring Data repositories and raw JDBC access.

| Component | Description |
|-----------|-------------|
| `Order` / `OrderItem` | JPA entities with `@OneToMany`, optimistic locking, lifecycle callbacks |
| `OrderRepository` | Spring Data JPA — derived queries, JPQL, native SQL, bulk updates |
| `OrderService` | Transactional service — propagation, isolation, read-only, rollback rules |
| `JdbcOrderDao` | JdbcTemplate + NamedParameterJdbcTemplate, batch operations |
| `DataSourceConfig` | HikariCP connection pool configuration |
| `V1__create_orders.sql` | Flyway migration |
| `application.yml` | Dual-profile config (H2 dev / PostgreSQL prod) |

Related docs: [Best Practices](docs/best-practices.md) (resource management), [Third-Party Libraries](docs/third-party-libraries.md) (HikariCP, Flyway)

### High Performance Computing

**Path:** [`examples/hpc/`](examples/hpc/) · **[README](examples/hpc/README.md)**

Concurrency patterns using modern Java APIs.

| Component | Description |
|-----------|-------------|
| `ParallelStreamExamples` | Custom ForkJoinPool, `groupingByConcurrent`, parallel reduction |
| `CompletableFutureExamples` | Chaining, fan-out/fan-in, error handling, timeouts |
| `ConcurrentCollectionsExamples` | ConcurrentHashMap, BlockingQueue, LongAdder, Semaphore, StampedLock |
| `VirtualThreadExamples` | Java 21+ virtual threads (Maven profile exclusion on Java 17) |

Related docs: [Best Practices](docs/best-practices.md) (concurrency), [Architecture Patterns](docs/architecture-patterns.md) (async patterns)

### ETL & Batch Processing

**Path:** [`examples/etl/`](examples/etl/) · **[README](examples/etl/README.md)**

Three approaches to data processing: Spark, Spring Batch, and pure Java.

| Component | Description |
|-----------|-------------|
| `SparkEtlExample` | RDD word count, DataFrame CSV→Parquet, Spark SQL, typed Datasets |
| `CsvToJsonBatchConfig` | Spring Batch 5 chunk-oriented job with fault tolerance |
| `DataPipeline` | Pure-Java extract/filter/transform/load abstraction |
| `DataPipelineTest` | Unit tests for the pipeline abstraction |

Related docs: [Architecture Patterns](docs/architecture-patterns.md) (CQRS, event sourcing), [Third-Party Libraries](docs/third-party-libraries.md) (Spring Batch)

### Systems Programming

**Path:** [`examples/systems/`](examples/systems/) · **[README](examples/systems/README.md)**

Low-level Java: native interop, memory management, and performance measurement.

| Component | Description |
|-----------|-------------|
| `JniExample` | JNI native methods with Java fallback pattern |
| `OffHeapMemoryExample` | Direct ByteBuffer, Cleaner-based arrays, memory-mapped files |
| `PerformanceBenchmarks` | JMH benchmarks (string concat, boxing, collections, streams) |
| `ProfilingUtils` | JVM snapshots via MXBeans, execution timing |

Related docs: [Best Practices](docs/best-practices.md) (performance), [Development Workflow](docs/development-workflow.md) (profiling)

### Design Patterns

**Path:** [`examples/patterns/`](examples/patterns/) · **[README](examples/patterns/README.md)**

15 GoF patterns implemented with modern Java 17+ idioms (records, sealed interfaces, pattern matching).

| File | Patterns |
|------|----------|
| `CreationalPatterns` | Builder, Factory Method, Singleton, Prototype, Abstract Factory |
| `StructuralPatterns` | Adapter, Decorator, Proxy, Composite, Facade |
| `BehavioralPatterns` | Strategy, Observer, Command, Template Method, Chain of Responsibility |

Related docs: [Architecture Patterns](docs/architecture-patterns.md) (hexagonal, CQRS), [Best Practices](docs/best-practices.md) (code style)

### Simulation

**Path:** [`examples/simulation/`](examples/simulation/) · **[README](examples/simulation/README.md)**

Statistical and event-driven simulation frameworks.

| Component | Description |
|-----------|-------------|
| `MonteCarloSimulation` | Generic engine with Welford's online variance, Pi estimation, option pricing |
| `DiscreteEventSimulation` | Priority-queue DES engine with M/M/1 queue example |

Related docs: [HPC module](examples/hpc/README.md) (parallel execution), [Best Practices](docs/best-practices.md) (functional style)

### Testing

**Path:** [`examples/testing/`](examples/testing/) · **[README](examples/testing/README.md)**

Comprehensive testing strategy with examples for every layer.

| Component | Description |
|-----------|-------------|
| `JUnit5FeaturesTest` | Parameterized, nested, conditional tests, tags |
| `MockitoFeaturesTest` | Mocking, stubbing, captors, BDD style, spies |
| `PostgresContainerIT` | TestContainers PostgreSQL integration test |
| `RestAssuredIT` | REST Assured API testing with httpbin container |
| `ArchitectureRulesTest` | ArchUnit architecture enforcement |
| Domain classes | `User`, `UserRepository`, `UserService` as test targets |

Related docs: [Development Workflow](docs/development-workflow.md) (CI/CD, quality gates), [Documentation Standards](docs/documentation-standards.md) (test documentation)

## Documentation Guides

### Tutorial
**[docs/TUTORIAL.md](docs/TUTORIAL.md)** — New developer walkthrough: clone, build, run the REST API, explore with curl, run other examples, add a feature, run quality/security scans, Docker build. Start here if you're new to the project.

### Toolchain
**[docs/TOOLCHAIN.md](docs/TOOLCHAIN.md)** — Required tools (JDK 21, Maven wrapper, Docker), platform-specific install instructions (macOS, Linux, Windows), IDE setup (IntelliJ IDEA, VS Code), and quality tool reference.

### Extending the Template
**[docs/EXTENDING.md](docs/EXTENDING.md)** — Step-by-step guides for adding a new Maven module, REST endpoint, service, third-party dependency, and quality rule/suppression.

### Best Practices
**[docs/best-practices.md](docs/best-practices.md)** — 10 sections covering code style, naming conventions, error handling, logging, null safety, records & sealed classes, collections, concurrency, resource management, and general principles. All examples use Java 17+.

### Architecture Patterns
**[docs/architecture-patterns.md](docs/architecture-patterns.md)** — Microservices (Spring Boot, WebClient, Kafka, API Gateway, Resilience4j), hexagonal architecture (ports & adapters), CQRS (command/query separation), event sourcing (aggregate replay, event store, snapshots). Includes decision flowchart and anti-patterns.

### Third-Party Libraries
**[docs/third-party-libraries.md](docs/third-party-libraries.md)** — Catalog of recommended libraries: Spring ecosystem (6 starters), Apache Commons, Guava, Jackson, Lombok, MapStruct, Resilience4j, logging (SLF4J + Logback), database libs (HikariCP, Flyway, H2), and build plugins. Includes version reference table and selection decision guide.

### Documentation Standards
**[docs/documentation-standards.md](docs/documentation-standards.md)** — JavaDoc conventions (tag ordering, class/method/record examples), README templates, OpenAPI/Swagger integration (springdoc-openapi, code-first vs contract-first, OpenAPI Generator), and changelog format.

### Development Workflow & CI/CD
**[docs/development-workflow.md](docs/development-workflow.md)** — GitHub Actions 4-stage pipeline, Dockerfile (Temurin 21 JRE Alpine), adding modules/features guide, quality gates (SpotBugs, Checkstyle, JaCoCo, OWASP), Docker Compose, git branching, conventional commits, and release process.

### Security Scanning
**[docs/SECURITY_SCANNING.md](docs/SECURITY_SCANNING.md)** — Detailed configuration and usage for SpotBugs, Checkstyle, PMD, and OWASP Dependency-Check. Includes suppression guides, CI integration, and a maturity roadmap.

## Requirements

| Requirement | Version |
|-------------|---------|
| Java | 17+ (21 recommended for virtual threads) |
| Maven | 3.9+ (wrapper included) |
| Docker | Required for TestContainers and containerized deployment |

### Java Features Used

- Records and sealed classes (Java 17)
- Pattern matching for `instanceof` and `switch` (Java 17+)
- Text blocks (Java 17)
- Virtual threads (Java 21+, guarded by Maven profile)
- Sequenced collections (Java 21+, where noted)

## Coverage Checklist

| Requirement | Module / Doc | Status |
|-------------|-------------|--------|
| RESTful APIs (client + server) | `examples/restful-api/` | ✅ |
| Database (JPA, JDBC, pooling, transactions) | `examples/database/` | ✅ |
| High Performance Computing | `examples/hpc/` | ✅ |
| ETL / MapReduce | `examples/etl/` | ✅ |
| Systems Programming | `examples/systems/` | ✅ |
| Design Patterns (GoF) | `examples/patterns/` | ✅ |
| Simulation (Monte Carlo, DES) | `examples/simulation/` | ✅ |
| Testing (JUnit 5, TestContainers, REST Assured) | `examples/testing/` | ✅ |
| Best Practices Guide | `docs/best-practices.md` | ✅ |
| Architecture Patterns | `docs/architecture-patterns.md` | ✅ |
| Third-Party Libraries | `docs/third-party-libraries.md` | ✅ |
| Documentation Standards | `docs/documentation-standards.md` | ✅ |
| Development Workflow & CI/CD | `docs/development-workflow.md` | ✅ |
| Toolchain & IDE Setup | `docs/TOOLCHAIN.md` | ✅ |
| Extending the Template | `docs/EXTENDING.md` | ✅ |
| New Developer Tutorial | `docs/TUTORIAL.md` | ✅ |
| Security Scanning | `docs/SECURITY_SCANNING.md` | ✅ |
| Project Setup (Maven multi-module) | `pom.xml` | ✅ |
| CI/CD Pipeline | `.github/workflows/ci.yml` | ✅ |
| Docker | `examples/restful-api/Dockerfile` | ✅ |

## License

Apache License 2.0
