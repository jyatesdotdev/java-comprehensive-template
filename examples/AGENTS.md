# AGENTS.md — `examples/` (reference modules)

Read the root `AGENTS.md` first. Each module below also has its own `AGENTS.md`.

## What this directory is

Eight self-contained Maven modules, each demonstrating one enterprise concern with
production-grade *style* but demo-grade *scope*. They are teaching material: heavy
Javadoc, modern Java 21 idioms, and deliberate patterns to copy. They are **not**
deployable services (the runnable app is `../app`; `restful-api` is the one example
that also runs as a server).

## Module index

| Module | Demonstrates | Runnable? | Unit tests | Needs Docker? |
|--------|--------------|-----------|------------|---------------|
| `restful-api/` | Spring Boot REST server + 3 HTTP client styles, OpenAPI, global error handling | `spring-boot:run` on :8080 | 45 (controller, service, mapper, DTO, clients, context) | only for its Dockerfile |
| `database/` | JPA/Hibernate entities, Spring Data, transactions, raw JDBC, Flyway, HikariCP | Spring Boot app (H2 in-memory) | 39 (entities, service, `@DataJpaTest`, `@JdbcTest`) | no (H2) |
| `hpc/` | Parallel streams, CompletableFuture, concurrent collections, virtual threads | no (static utility methods) | 36 | no |
| `etl/` | Spark RDD/DataFrame/SQL, Spring Batch chunk job, pure-Java pipeline | no main class | 18 (pipeline + batch beans; Spark untested) | no |
| `systems/` | JNI with fallback, off-heap memory, JMH benchmarks, MXBean profiling | `PerformanceBenchmarks#main` | 14 | no |
| `patterns/` | 15 GoF patterns in modern Java (records, sealed, switch matching) | no | 59 (one `@Nested` class per pattern) | no |
| `simulation/` | Monte Carlo (Welford's variance), discrete-event simulation (M/M/1) | both classes have `main` | 15 (statistical tolerance + deterministic engine) | no |
| `testing/` | **The reference for how to test**: JUnit 5, Mockito, Testcontainers, REST Assured, ArchUnit | tests are the content | 18 unit + 8 IT methods in 2 classes | yes, for `*IT` |

## Traits shared by every example module

- Parent `com.example.template:java-enterprise-template` via `<relativePath>../../pom.xml</relativePath>`; artifactId `template-<name>`.
- **`<jacoco.skip>true</jacoco.skip>`** — no coverage gate here (unlike `app/`).
- Checkstyle/PMD/SpotBugs still apply in full (under the scan profiles), including to
  test sources. Example-code rule friction is handled with *narrow, commented*
  `@SuppressWarnings("PMD.X") // Example code: reason` annotations — follow that style.
- Common test deps (JUnit 5, AssertJ, Mockito, SLF4J) are inherited; only
  module-specific deps are declared, version-less (managed by the root pom).
  Jersey, springdoc, and JMH versions live in root `dependencyManagement`.
- Empty `src/{main,test}/{java,resources}` dirs are kept with `.gitkeep` — leave them.
- Every module has a `README.md` (human walkthrough) and an `AGENTS.md` (agent rules).

## Code style expected in example code

- One focused class (or one class per sub-topic) with a class Javadoc that lists what
  it *Demonstrates* in a `<ul>`, plus `<pre>{@code ...}` usage snippets.
- Non-instantiable demo holders are `final` with a private constructor and static
  methods. Spring-configured classes follow normal bean conventions.
- Modern idioms first: records, sealed interfaces + exhaustive switch, text blocks,
  `var`, functional interfaces with `default` composition methods.
- SLF4J for real logging; `System.out` is allowed **only** in `systems/` and
  `simulation/` (suppressed in `config/checkstyle/checkstyle-suppressions.xml`).

## Adding a new example module

1. `examples/<name>/` with `pom.xml` (parent as above, `template-<name>`,
   `<jacoco.skip>true</jacoco.skip>`), `src/main/java/com/example/template/<name>/`,
   `src/test/java/...`.
2. Register `<module>examples/<name></module>` in the **root** `pom.xml`.
3. Add `README.md` (follow an existing one), `AGENTS.md` (follow a sibling), and a row
   in this file's module index plus the root `README.md` tables.
4. Verify: `./mvnw compile -pl examples/<name>` then
   `./mvnw verify -Psecurity-scan-quick -DskipTests -pl examples/<name>`.

Full recipe: `docs/EXTENDING.md`.

## Testing expectations

Every module now ships a real unit-test suite (counts in the table above; 246 unit
`@Test` methods repo-wide including `app`, plus 8 IT methods). Keep it that way: **new behavior lands with tests in the same
change**, in the styles demonstrated by `examples/testing/` and each module's existing
suite. Known intentional gap: Spark code in `etl/` is untested (JVM-heavy); the
surefire `--add-opens` argLine is already in place if you add a `local[*]` test.
`jacoco.skip=true` remains set per module — don't remove it without enough coverage to
clear the parent's 80% bundle gate.
