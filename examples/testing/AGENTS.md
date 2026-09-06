# AGENTS.md — `examples/testing/`

Read the root and `examples/AGENTS.md` first. **This module is the testing reference
for the whole repository** — when adding tests anywhere, copy the styles here.

## Layout

Minimal production code as test targets, plus one showcase class per technique:

```
main:  model/User (record, compact-ctor validation)
       repository/UserRepository (interface — code to interfaces for testability)
       service/UserService (constructor-injected; the unit-test target)
test:  junit5/JUnit5FeaturesTest        — unit  (surefire)
       mockito/MockitoFeaturesTest      — unit  (surefire)
       archunit/ArchitectureRulesTest   — unit  (surefire)
       integration/PostgresContainerIT  — integration (failsafe, Docker)
       integration/RestAssuredIT        — integration (failsafe, Docker)
```

## The Test / IT split — how it works mechanically

- This module's pom configures **surefire to exclude `**/*IT.java`**.
- Failsafe is **not** always-on; it is added only by the parent's
  `integration-tests` profile (so root `./mvnw verify` does not need Docker).

```bash
./mvnw -pl examples/testing test                         # unit only, no Docker needed
./mvnw -pl examples/testing verify -Pintegration-tests   # + ITs (Docker REQUIRED)
./mvnw -pl examples/testing test -Dtest=JUnit5FeaturesTest
./mvnw -pl examples/testing test -Dgroups=slow           # @Tag filtering
```

Name unit tests `*Test`, integration tests `*IT` — the suffix *is* the routing.

## What each showcase demonstrates (copy from these)

- **`JUnit5FeaturesTest`**: lifecycle hooks, `@DisplayName`, AssertJ fluent assertions,
  `assertAll`, `assertThatThrownBy`, parameterized tests (`@ValueSource`, `@CsvSource`,
  `@MethodSource`), `@Nested` grouping, `assertTimeout`, `@EnabledOnOs`, `@Tag`.
- **`MockitoFeaturesTest`**: `@ExtendWith(MockitoExtension.class)`, `@Mock` /
  `@InjectMocks` / `@Captor`, stubbing + matchers, `thenAnswer`, `verify` /
  `verifyNoMoreInteractions` / `never()`, BDD style (`given`/`then`), spies with
  `doReturn`. The `mockito-junit-jupiter` dep enables mocking final classes/static
  methods (inline mock maker).
- **`ArchitectureRulesTest`**: ArchUnit `@AnalyzeClasses` + `@ArchTest` fields. Four
  rules are **enforced right now** on this module's packages: services don't depend on
  services, repositories don't depend on services, no package cycles, model classes
  must be records. Note the justified
  `@SuppressWarnings("PMD.TestClassWithoutTestCases")` — ArchUnit uses fields, not
  `@Test` methods.
- **`PostgresContainerIT`**: `@Testcontainers` + `@Container static
  PostgreSQLContainer<>("postgres:16-alpine")`, raw JDBC via `DriverManager` (hence the
  test-scope `postgresql` driver), per-test table truncation, unique-constraint
  violation testing.
- **`RestAssuredIT`**: `GenericContainer("kennethreitz/httpbin")` with an HTTP wait
  strategy, `RestAssured.baseURI/port` from the mapped port, JSON-path body assertions,
  header/status assertions. Justified suppressions for fluent-assertion style.

## Rules and gotchas

- **ITs require a running Docker daemon.** CI runs them as a gating
  `integration-tests` job (`-Pintegration-tests`).
- AssertJ (`assertThat`) is the assertion style everywhere — not Hamcrest, not bare
  JUnit assertions (REST Assured's embedded Hamcrest matchers are the exception).
- Test classes get relaxed Checkstyle rules (length/complexity/javadoc) via the shared
  suppressions; PMD still applies — use the narrow, commented suppressions shown here.
- README mentions Selenium/Playwright E2E — **reference snippets only**, not
  dependencies; don't try to run them.
- `jacoco.skip=true` here too (the README's coverage claim is stale).
- Pin container image tags (as done: `postgres:16-alpine`) — never `latest` for
  databases.
