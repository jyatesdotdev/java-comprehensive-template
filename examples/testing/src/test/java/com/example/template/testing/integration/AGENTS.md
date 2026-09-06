# AGENTS.md — `testing.integration` (Testcontainers reference)

Read the test-tree AGENTS.md one level up first. **Docker required**; these run only
under `./mvnw verify -Pintegration-tests` (failsafe, `*IT` suffix).

- `PostgresContainerIT` — `@Testcontainers` + `@Container static final
  PostgreSQLContainer<>("postgres:16-alpine")` (static = one container for the class);
  raw JDBC via `DriverManager` + the container's `getJdbcUrl()`; per-test
  `@BeforeEach` creates/truncates the table so tests stay independent.
- `RestAssuredIT` — `GenericContainer` (`mccutchen/go-httpbin:2.18.1`, port 8080)
  with an HTTP `Wait` strategy; `RestAssured.baseURI/port` set from `getHost()` /
  `getMappedPort()` in `@BeforeAll`, `RestAssured.reset()` in `@AfterAll`.
- Rules: pin image tags (`postgres:16-alpine`, `mccutchen/go-httpbin:2.18.1`), never `latest`; always use mapped
  ports, never assume defaults; every container has a wait strategy — "it usually
  boots fast enough" is a flake generator; isolate state per test.
- CI runs this as a gating `integration-tests` job. With colima set
  `DOCKER_HOST=unix://$HOME/.colima/default/docker.sock`.
- Selenium/Playwright mentioned in the README are reference snippets only — not
  dependencies, don't try to run them.
