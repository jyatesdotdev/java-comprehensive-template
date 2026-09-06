# Template hardening TODO

Tracked findings from the project analysis. Work them **in order**. Check the box
and add a one-line `Done:` note when finished. Do not skip an item without moving
it to **Won't do** with a reason.

**Definition of done for the whole list**

1. Every box below is `[x]` or listed under Won't do.
2. `./mvnw spotless:apply -Pformat`
3. `./mvnw clean verify -DskipITs`
4. `./mvnw verify -Psecurity-scan-quick -DskipTests`
5. New public API has Javadoc; new behavior has tests.
6. Versions live in the root `pom.xml` (no new child pins).
7. Docs/`AGENTS.md` updated in the same change as behavior.

**Hard rules:** Java 21, `./mvnw` only, constructor injection, no `System.out`,
no star imports, never return `null` (use `Optional` or empty collections),
nearest `AGENTS.md` wins, no Spring Security module, do not fill empty `app/`
with domain features.

---

## Milestone 1 — CI, packaging, supply chain

- [x] **M1.1** CI runs on PRs to `develop`.
  - Done: `pull_request.branches: [main, develop]`.
  - File: `.github/workflows/ci.yml`
  - Today: `pull_request.branches: [main]` while gitflow is `feature/*` → `develop`.
  - Change: `pull_request.branches: [main, develop]`.
  - Update `.github/AGENTS.md` trigger sentence.

- [x] **M1.2** Honest security / IT gates.
  - Done: ITs gating; OWASP/Trivy/Snyk stay advisory; SECURITY_SCANNING table updated.
  - Keep OWASP + Trivy + Snyk as **non-blocking** (NVD/token flakiness, Spark CVEs).
  - Make `integration-tests` **gating**: remove `continue-on-error` and the TODO
    once ITs are pinned (M4.1) and the job command is correct (M1.3).
  - Fix `docs/SECURITY_SCANNING.md` Jobs table: `dependency-scan` and
    `container-scan` must say they do **not** block deploy. Snyk already says No.

- [x] **M1.3** One IT command everywhere.
  - Done: Failsafe only via `-Pintegration-tests`; `-DskipUTs` removed.
  - `examples/testing/pom.xml`: remove Failsafe from always-on `<plugins>` so
    root `./mvnw clean verify` does **not** need Docker.
  - Parent profile `integration-tests` already adds Failsafe — keep that as the
    only activation path.
  - CI job: `./mvnw -B verify -pl examples/testing -Pintegration-tests -Dsurefire.skip=true`
  - Align root `AGENTS.md`, `examples/testing/AGENTS.md`, IT javadoc, README,
    tutorial, `docs/development-workflow.md`. Delete the fake `-DskipUTs` flag.

- [x] **M1.4** Delete fake JaCoCo aggregate.
  - Done: quality-gates no longer runs report-aggregate/check; tutorial points at `app/`.
  - CI `quality-gates`: remove `jacoco:report-aggregate` and the follow-up
    `jacoco:check` (coverage already runs in `build` via module `verify`).
  - Do **not** enable JaCoCo on `examples/*` (would fail 80% BUNDLE).
  - Keep `app` 80% gate and `ApplicationMainTest`.
  - Docs/tutorial: stop pointing at `examples/restful-api/target/site/jacoco`.
    Point at `app/target/site/jacoco/index.html`.
  - Update `.github/AGENTS.md` (no aggregate).

- [x] **M1.5** Spotless is a real CI gate.
  - Done: `spotless:check` in quality-gates; docs no longer claim `verify -Pformat`.
  - Add `./mvnw -B spotless:check` to `quality-gates` (before or with scans).
  - Do not bind `apply` to `verify`. Local format remains `./mvnw spotless:apply -Pformat`.
  - Fix `docs/best-practices.md` and `docs/TOOLCHAIN.md` (`verify -Pformat` is a no-op).

- [x] **M1.6** Add `LICENSE` (Apache License 2.0) at repo root. README already claims it.
  - Done: Apache-2.0 LICENSE, Copyright 2024-2026 Example Authors.
  - Copyright holder: `Example Authors` (no real personal name). Year 2024–2026.

- [x] **M1.7** Pin GitHub Actions to commit SHAs (do **not** upgrade majors).
  - Done: all workflow `uses:` pinned to listed SHAs with version comments.
  - `actions/checkout@v4.2.2` → `11bd71901bbe5b1630ceea73d27597364c9af683`
  - `actions/setup-java@v4.7.1` → `c5195efecf7bdfc987ee8bae7a71cb8b11521c00`
  - `actions/upload-artifact@v4.6.2` → `ea165f8d65b6e75b540449e92b4886f43607fa02`
  - `github/codeql-action/upload-sarif@v3.28.0` → `8c78abb9b62512e3c45dea6559ffd924ed8549c8`
  - `aquasecurity/trivy-action@v0.35.0` → `57a97c7e7821a5776cebc9bb87c984fa69cba8f1`
  - `dorny/test-reporter@v1.9.1` → `6c357194179c694acfcad2100dbf27c5b9b0d5e0`
  - `snyk/actions/maven@master` → `12140f4059e244892ae643824a95459a102120dd`
  - Keep `uses: org/repo@<sha>` with a comment `# vX.Y.Z` on the same line.

- [x] **M1.8** Pin Maven wrapper checksum.
  - Done: `distributionSha256Sum=4ec3f26fb1a692473aea0235c300bd20f0f9fe741947c82c1234cefd76ac3a3c`.
  - Compute SHA-256 of `https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip`
  - Set `distributionSha256Sum=` in `.mvn/wrapper/maven-wrapper.properties`.

- [x] **M1.9** Root `Dockerfile` uses `./mvnw`, not `apk add maven`.
  - Done: wrapper-based app image; restful-api Dockerfile installs wget + start-period.
  - Copy `mvnw`, `mvnw.cmd`, and `.mvn/` into the build stage; `chmod +x mvnw`.
  - Still build `-pl app`.
  - `examples/restful-api/Dockerfile`: install `wget` (or busybox wget) as root
    before `USER app`; add `start-period=30s` like the root image.

- [x] **M1.10** Move child version pins into root `dependencyManagement`.
  - Done: jersey/springdoc/jmh in parent DM; children version-less.
  - Jersey `3.1.5` (client, hk2, media-json-jackson) and springdoc `2.5.0`
    from `examples/restful-api/pom.xml`.
  - JMH `1.37` from `examples/systems/pom.xml`.
  - Children declare those deps **version-less**.
  - Add properties in root pom.

- [x] **M1.11** Delete unused root version properties **or** wire them through
  `dependencyManagement`. Today they do nothing (Boot BOM wins):
  `hibernate.version`, `flyway.version`, `hikaricp.version`, `h2.version`,
  `postgresql.version`, `jackson.version`, `slf4j.version`, `logback.version`,
  `junit-jupiter.version`, `mockito.version`, `assertj.version`.
  - Prefer **delete** and document “Boot BOM pins these”.
  - Keep used properties (`java.version`, `spring-boot.version`, `spark.version`,
    `guava.version`, `byte-buddy.version`, plugin versions, etc.).
  - Done: unused Boot-BOM properties deleted; docs say BOM pins them.

- [x] **M1.12** Maven Enforcer + Dependabot.
  - Done: enforcer on validate (Java 21, Maven ≥ 3.9.0); weekly Dependabot.
  - `maven-enforcer-plugin`: require Java 21 and Maven ≥ 3.9.0; bind on `validate`
    in the parent so every module inherits it.
  - `.github/dependabot.yml`: weekly `maven` (root `pom.xml`) and `github-actions`.

- [x] **M1.13** Narrow SpotBugs package-wide excludes.
  - Done: PREDICTABLE_RANDOM / EI_EXPOSE / NP_ / RV_ narrowed off the whole template package.
  - `config/spotbugs/spotbugs-exclude.xml`: `PREDICTABLE_RANDOM`, `EI_EXPOSE_REP`,
    `EI_EXPOSE_REP2`, `NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE`,
    `RV_RETURN_VALUE_IGNORED_BAD_PRACTICE` must **not** apply to all
    `com.example.template.*`.
  - Keep them only where demo code needs them (`examples/simulation`,
    `examples/hpc`, maybe `examples/patterns`). If a scan then fails, fix the
    code or add a **class-level** exclusion with a comment — never a repo-wide
    package match for security-relevant rules.

---

## Milestone 2 — Correctness (copy-paste hazards)

- [x] **M2.1** `GlobalExceptionHandler` must not turn client errors into 500.
  - Done: extends `ResponseEntityExceptionHandler`; bad UUID/malformed JSON → 400; service failure → 500.
  - File: `examples/restful-api/.../exception/GlobalExceptionHandler.java`
  - Extend `ResponseEntityExceptionHandler` (or delete the `@ExceptionHandler(Exception.class)`
    catch-all and let Boot map 400/405/415).
  - Keep 404 + validation 400 domain handlers.
  - Tests in `ProductControllerTest`: bad UUID path, malformed JSON → 400 not 500.
  - Optional: one test where service throws `RuntimeException` → 500 if a catch-all remains.

- [x] **M2.2** HTTP clients: timeouts + non-2xx handling on **every** method.
  - Done: RestTemplateBuilder timeouts; onStatus/status checks on all methods; package constructors for tests.
  - `ProductRestTemplateClient`: do not `new RestTemplate()`. Use
    `RestTemplateBuilder` (timeouts) or `HttpComponentsClientHttpRequestFactory`.
    Map 404 → `ResourceNotFoundException`; other 4xx/5xx → `ClientException`.
  - `ProductWebClientExample`: `HttpClient` response timeout + `onStatus` on list/create/update/delete.
  - `ProductJaxRsClient`: status checks on `list` / `create` / `update` like get/delete.
  - Tests without `ReflectionTestUtils.setField` (constructor or package seams).
  - Update `examples/restful-api/src/main/java/.../client/AGENTS.md`.

- [x] **M2.3** Poison-pill producer must not hang consumers on interrupt.
  - Done: `startProducer` returns Thread; nested put/offer of poison pill; interrupt test.
  - `ConcurrentCollectionsExamples.startProducer`: after restoring interrupt,
    offer/put `POISON_PILL` (best-effort, nested try). Add a test.

- [x] **M2.4** `SparkEtlExample.csvTransformExample` must not return a `Dataset`
  - Done: takes caller-owned `SparkSession`; Spark surefire test skipped (javax.servlet missing).
  after closing `SparkSession`. Collect/write inside the try-with-resources and
  return an in-memory result, **or** take `SparkSession` as a parameter (like
  `sparkSqlExample`). Add a small `local[*]` unit test for `wordCountRdd` if
  practical; if Spark is too heavy, fix the API and leave the documented Spark
  gap but do not return a closed Dataset.

- [x] **M2.5** `DataPipeline.load`: pass `List.copyOf(batch)` (or a fresh list)
  - Done: `deliver()` copies the batch; test stores the collection without copying.
  to the loader before `clear()`. Extend `DataPipelineTest` so a loader that
  stores the collection still sees data after the next batch.

- [x] **M2.6** `CsvToJsonBatchConfig` Javadoc snippet: drop `@EnableBatchProcessing`
  - Done: snippet uses `JobLauncher` + `csvToJsonJob` bean; warns against `@EnableBatchProcessing`.
  (Boot 3.3 auto-config). Show `JobLauncher` + the job bean instead.

- [x] **M2.7** Monte Carlo min/max must be the **same** trials as mean/variance
  - Done: Welford state includes min/max; second `DoubleStream` removed.
  (extend Welford state). Update `MonteCarloSimulationTest`.

- [x] **M2.8** `Order.getItems()` returns an unmodifiable list. Callers use
  - Done: `Collections.unmodifiableList`; test asserts add throws.
  `addItem`. Update tests if they mutated the live list.

- [x] **M2.9** `Notification` record: `tags = List.copyOf(tags)` in compact constructor.
  - Done: compact constructor copies tags; test mutates the caller list.
  Update creational tests if needed.

- [x] **M2.10** PUT `/api/v1/products/{id}` OpenAPI `@ApiResponses` must include 400
  - Done: PUT documents 400 `ErrorResponse` like POST.
  like POST (`ProductController`).

- [x] **M2.11** `findHighValue` must not return `null` — return `Optional<String>`.
  - Done: returns `Optional`; tests use contains/isEmpty.
  Update `ConcurrentCollectionsExamplesTest`.

- [x] **M2.12** `OrderRepository` bulk `@Modifying`: `clearAutomatically = true`,
  - Done: clear/flush automatic + `updatedAt`; test no longer `entityManager.clear()`.
  `flushAutomatically = true`, and set `updatedAt`. Adjust the test that currently
  has to `entityManager.clear()`.

- [x] **M2.13** Interrupted sleep in `CompletableFutureExamples` /
  - Done: sleep paths throw `CompletionException`; `orTimeout` javadoc notes no cancel.
  `VirtualThreadExamples` must not fall through and succeed after restoring the
  flag (rethrow `CompletionException` / restore+throw). Document that
  `orTimeout` does not cancel the supplier (comment + test already counts down).

- [x] **M2.14** Flyway 10 + Postgres: add `flyway-database-postgresql` (version-less)
  - Done: version-less dep in `examples/database/pom.xml`.
  to `examples/database/pom.xml`.

- [x] **M2.15** `RestApiApplication` smoke test (`@SpringBootTest` contextLoads)
  - Done: `RestApiApplicationTest` asserts the context bean.
  so the Docker-built app has a context test. Do not add a security module.

---

## Milestone 3 — Docs, counts, teaching completeness

- [x] **M3.1** Tutorial API uses UUIDs, not `/api/v1/products/1`.
  - Done: tutorial captures create-response UUID via `jq -r .id`.
  - `docs/TUTORIAL.md` “Explore the API”: use the create-response id or a sample UUID.

- [x] **M3.2** Java requirement is **21**, not “17+”.
  - Done: README/docs/module READMEs require 21; `java17` profile labeled optional overlay.
  - `README.md`, `docs/best-practices.md` title, `docs/documentation-standards.md`,
    module READMEs that say Java 17+ as the **requirement**.
  - Language features that originated in 17 can still be described as such.
  - `examples/hpc`: javadoc/README “update root POM to 21” — root is already 21.
    Keep the `java17` profile only as a clearly labeled optional overlay, or delete
    it if it is dead weight.

- [x] **M3.3** Fix remaining command/version drift:
  - Done: counts 246 unit `@Test` + 8 IT methods; SpotBugs Low/4.9.8.3; `./mvnw`; sample compose; `.vscode/extensions.json`.
  - Mockito table in `docs/third-party-libraries.md` (do not claim a dead property).
  - `docs/development-workflow.md` CI diagram (include OWASP/Trivy/Snyk as
    non-blocking); SpotBugs 4.9.8.3 / threshold Low; “full CI equivalent” must
    include `-Psecurity-scan-quick`.
  - Database test counts: 38 `@Test` methods, not 47. Recalculate repo-wide total.
  - `examples/AGENTS.md` “8 `*IT`”: say **8 IT methods in 2 classes**.
  - `docs/EXTENDING.md` sample module pom includes `<jacoco.skip>true</jacoco.skip>`
    and mentions README + AGENTS.md.
  - Prefer `./mvnw` over `mvn` in docs (`docs/SECURITY_SCANNING.md`,
    `docs/best-practices.md`, testing README, architecture-patterns).
  - `examples/restful-api/AGENTS.md`: `RestClientConfig` lives under `client/`, not `config/`.
  - `docs/architecture-patterns.md`: include `app` / `com.example.template` in the package table.
  - `docs/TOOLCHAIN.md`: `.vscode/extensions.json` — add the file **or** stop recommending it.
  - `docs/development-workflow.md` docker-compose YAML: label as a **sample**, do not
    pretend the file exists (do not add a full compose stack).

- [x] **M3.4** `docs/third-party-libraries.md` overclaim.
  - Done: Lombok/Security/Resilience4j/Prometheus marked not shipped; MapStruct managed but unused.
  - Lombok, Resilience4j, Spring Security/OAuth2, MapStruct processor, Prometheus
    YAML are **not** in this repo. Reword to “common enterprise choices, not
    shipped here” unless a matching dependency exists.
  - MapStruct is in parent DM but unused — say that, or remove the unused DM entry.

- [x] **M3.5** ArchUnit should protect the REST layering students copy.
  - Done: `examples/restful-api` ArchUnit + version-less `archunit-junit5`.
  - Add a small ArchUnit test in `examples/restful-api` (controller must not
    depend on persistence; stay inside `com.example.template.restfulapi`).
  - Add `archunit-junit5` version-less in that module (already in parent DM).

- [x] **M3.6** Testing reference nits (small):
  - Done: `@Tag("slow")` uses `Assumptions.abort`; csvSource asserts email.
  - `JUnit5FeaturesTest` `@Tag("slow")` should not `assertThat(true).isTrue()` —
    use `Assumptions.abort` or a real cheap assertion / delete the dummy.
  - Parameterized `csvSourceParams` should assert `email` too.
  - `app` tests may keep JUnit assertions (smoke); no need to rewrite to AssertJ.

---

## Won't do (tracked so they are not “forgotten”)

- **Empty `app/`** — by design (`app/AGENTS.md`). Not a starter-app rewrite.
- **Spring Security / OAuth2 / Resilience4j / Kafka modules** — docs overclaim only
  (M3.4). Do not add new example modules in this pass.
- **Off-heap Cleaner no-op, Batch `return null` to filter, `AppConfig.get` null,
  in-place `Product` mutation** — documented teaching choices.
- **Replace H2 `@DataJpaTest` with Testcontainers** — keep H2 as the fast slice.
- **Upgrade OWASP Dependency-Check 9.1.0 → 12.x** — out of scope; would churn NVD.
- **Raise Checkstyle `MissingJavadocMethod` to warning** — too large; public types
  already have Javadoc.
- **Remove PMD `AvoidCatchingGenericException` exclude** — conflicts with a 500
  fallback if one remains.
- **Push images / SBOM / provenance on the `docker` job** — no registry configured.
- **Enable JaCoCo on all eight examples** — coverage theater fix is M1.4, not 80%
  on Spark/JNI demos.

---

- [x] **Follow-up** Pin gating ITs (was missing as its own box; ITs now fail the pipeline).
  - Done: `RestAssuredIT` uses `mccutchen/go-httpbin:2.18.1` (port 8080), not `httpbin:latest`.
    Both IT classes use `disabledWithoutDocker = true`.

---

## Progress log

- Created this file from the analysis report.
- M1.1–M1.13 implemented; `spotless:apply` + compile + `verify -Psecurity-scan-quick -DskipTests` green.
- M2.1–M2.15 implemented.
- M3.1–M3.6 implemented.
- Follow-up: pinned go-httpbin for gating ITs.
- Review pass: stronger GET-400 assertion; simulation/config AGENTS; docker waits on ITs; README not-shipped caveat.
- Correctness review follow-up: Alpine `unzip` for wrapper SHA; `surefire.skip`; JOIN FETCH test after `entityManager.clear()`.
