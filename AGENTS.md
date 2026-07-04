# AGENTS.md — Java Enterprise Template

Guide for AI agents (and humans) working in this repository. Read this file first.
AGENTS.md files exist at every level — repo root, each top-level directory, each
module, and each Java package (main and test). **The nearest AGENTS.md above the file
you are editing carries the most specific rules; read it, plus the module guide it
points to, before changing anything.** When you add a package or module, add an
AGENTS.md with it (short: local rules only, pointer up to the module guide).

## What this project is

A Maven **multi-module** reference template for enterprise Java. It is teaching
infrastructure: the `app` module is a minimal runnable Spring Boot service, and the
eight `examples/*` modules are curated reference implementations of common enterprise
concerns (REST, database, concurrency, ETL, systems programming, design patterns,
simulation, testing). Code quality is enforced by an aggressive static-analysis stack.

- **Java 21** (`<java.version>21</java.version>` in the root `pom.xml` — some docs still
  say 17; the pom is the source of truth).
- **Maven 3.9.9** via wrapper. Always use `./mvnw`, never a system `mvn`.
- **Spring Boot 3.3.5** managed through the parent BOM.
- All dependency and plugin versions live in the **root `pom.xml`**. Child modules
  never hardcode versions (exceptions exist and are called out in module guides).

## Repository map

| Path | What it is | Local guide |
|------|-----------|-------------|
| `pom.xml` | Parent POM: all versions, plugin config, quality profiles | this file |
| `app/` | The runnable Spring Boot application (only module with enforced coverage) | `app/AGENTS.md` |
| `config/` | Shared Checkstyle/PMD/SpotBugs/OWASP configuration — affects **every** module | `config/AGENTS.md` |
| `docs/` | Human-oriented guides (tutorial, best practices, extending) | `docs/AGENTS.md` |
| `examples/` | Eight reference modules | `examples/AGENTS.md` + one per module |
| `.github/workflows/ci.yml` | CI pipeline (build → ITs / quality gates / scans → docker) | — |
| `Dockerfile` | Multi-stage image for `app` (note: CI builds `examples/restful-api/Dockerfile`, not this one) | — |

## Commands (the golden path)

Run everything from the repository root.

```bash
./mvnw clean verify                        # full build + unit tests + JaCoCo
./mvnw -pl <module> test                   # one module's unit tests (add -am if you changed its deps)
./mvnw spotless:apply -Pformat             # auto-format (google-java-format) — run BEFORE quality checks
./mvnw verify -Psecurity-scan-quick -DskipTests   # Checkstyle + PMD + SpotBugs — what CI gates on
./mvnw verify -Psecurity-scan              # same + OWASP dependency check (slow; wants -DnvdApiKey=...)
./mvnw verify -pl examples/testing -P integration-tests   # *IT tests (requires Docker running)
./mvnw -pl examples/restful-api spring-boot:run    # run the REST example on :8080
./mvnw -pl app spring-boot:run             # run the core app on :8080
```

**Critical:** static analysis is *not* enforced by a plain `./mvnw verify`. Checkstyle,
PMD, and SpotBugs only bind to the `verify` phase under `-Psecurity-scan` or
`-Psecurity-scan-quick`. Your change is not done until
`./mvnw verify -Psecurity-scan-quick -DskipTests` passes.

## Quality gates

| Tool | Config | Enforcement |
|------|--------|-------------|
| Spotless | google-java-format + remove unused imports | `spotless:check`; apply with `-Pformat` |
| Checkstyle 10.14.2 | `config/checkstyle/checkstyle.xml` | fails on **warning**; scans test sources too |
| PMD 3.22.0 | `config/pmd/pmd-ruleset.xml` (security, errorprone, bestpractices, multithreading, design, performance) | fails on violation; scans tests |
| SpotBugs + FindSecBugs | `config/spotbugs/spotbugs-exclude.xml`, effort=Max, threshold=Low | fails under scan profiles |
| OWASP Dependency-Check | `config/owasp/owasp-suppressions.xml` | fails on CVSS ≥ 7 (`-Psecurity-scan` only) |
| JaCoCo | root pom | **80% line coverage**, BUNDLE level — but every `examples/*` module sets `jacoco.skip=true`, so in practice only `app` is gated |

CI (`.github/workflows/ci.yml`): the true gates are the `build` job
(`clean verify -DskipITs`) and `quality-gates` (`-Psecurity-scan-quick`). The
integration-test, OWASP, Trivy, and Snyk jobs run with `continue-on-error: true`.

## Hard rules (these fail the build)

- **No `System.out.println` / `System.err`** — use SLF4J (`private static final Logger log = LoggerFactory.getLogger(X.class);` with `{}` placeholders). Exception: `examples/systems` and `examples/simulation` have a Checkstyle suppression for demo output.
- **No `Runtime.getRuntime().exec(...)`** — Checkstyle severity *error*.
- **No star imports**, no unused/redundant imports, no `sun.*` imports.
- **No tabs, no trailing whitespace**, newline at end of file.
- Method ≤ **60 lines**, ≤ **7 parameters**, cyclomatic complexity ≤ **15**, file ≤ **500 lines**, nested ifs ≤ 3, nested try ≤ 2.
- `equals` and `hashCode` together; `default` in every `switch` statement; no string comparison with `==`; braces required.
- Naming: `PascalCase` types, `camelCase` methods/fields, `UPPER_SNAKE_CASE` constants (SLF4J `log`/`logger` allowed), lowercase packages.

## Java coding standards (condensed from `docs/best-practices.md`)

- **Records** for DTOs, events, results; validate in compact constructors. **Sealed interfaces** for closed hierarchies + exhaustive `switch` pattern matching. Text blocks for multi-line strings/SQL.
- **Constructor injection** only — never field `@Autowired`. Program to interfaces (`ProductService` / `InMemoryProductService`); don't name implementations `*Impl`.
- **Never return `null`** — return `Optional<T>` or an empty collection. `Optional` for return types only, not fields/parameters.
- Exceptions: unchecked domain exceptions (`ResourceNotFoundException extends RuntimeException`), centralized handling in `@RestControllerAdvice`, never swallow (log at minimum), never catch bare `Exception` in business logic, don't log-and-rethrow.
- Immutability by default: `List.of`/`List.copyOf`, record "withers" for copy-on-modify. Concurrency: virtual threads for I/O-bound fan-out, `CompletableFuture` for pipelines, concurrent collections over synchronized wrappers, always restore the interrupt flag on `InterruptedException`.
- try-with-resources for everything `AutoCloseable`.
- **Javadoc is required on all public types and methods**, tag order: `@param` → `@return` → `@throws` → `@see` → `@since` → `@deprecated`. First sentence = plain-text summary ending with a period. See `docs/documentation-standards.md`.

## Testing rules

- `*Test.java` = unit tests (Surefire, run by `mvnw test`). `*IT.java` = integration tests (Failsafe, run only with `-P integration-tests`, need Docker for Testcontainers).
- Stack: JUnit 5 + AssertJ (`assertThat...`) + Mockito. Copy the styles demonstrated in `examples/testing/` — it is the reference module for how to test here.
- Checkstyle/PMD scan test code, but `*Test`/`*IT` files get relaxed rules (method length, complexity, javadoc) via `config/checkstyle/checkstyle-suppressions.xml`.

## Suppressing a static-analysis finding

Never disable a rule globally to silence one finding. In order of preference:

1. Fix the code.
2. Inline, narrowly, **with a justifying comment**:
   `@SuppressWarnings("PMD.RuleName") // Example code: <reason>` or `// NOPMD - <reason>`.
3. File-pattern suppression in the matching file under `config/` (see `config/AGENTS.md`).
4. OWASP suppressions must include `<notes>` with the reason and a review date.

## Adding things

- **New module**: create `examples/<name>/pom.xml` with the parent
  (`relativePath ../../pom.xml`, artifactId `template-<name>`, add `<jacoco.skip>true</jacoco.skip>`
  if it's example code), register it in the root `<modules>` block, add a README and an
  AGENTS.md. Full walkthrough: `docs/EXTENDING.md`. Verify with `./mvnw compile -pl examples/<name>`.
- **New dependency**: add a version property + `<dependencyManagement>` entry in the
  **root** pom, declare it version-less in the child. Then run
  `./mvnw verify -Psecurity-scan -pl <module>` to OWASP-check it.
- **New REST endpoint**: follow the layering in `examples/restful-api/AGENTS.md`
  (DTO records with Jakarta Validation → service interface → `@Service` impl → controller
  with OpenAPI annotations; never expose domain entities).

## Git conventions

- Branches: `feature/<name>`, `hotfix/<name>` off `develop`; `main` is release-only.
- Conventional Commits: `feat(scope): ...`, `fix: ...`, `docs: ...`, `test: ...`, `chore: ...`.

## Definition of done — check before you finish

1. `./mvnw spotless:apply -Pformat` run, then `./mvnw clean verify` green.
2. `./mvnw verify -Psecurity-scan-quick -DskipTests` green.
3. New public API has Javadoc; new behavior has tests (unit `*Test`, integration `*IT`).
4. If you touched `app/`, coverage still ≥ 80% (`./mvnw -pl app verify`).
5. Versions live in the root pom; module READMEs and the relevant AGENTS.md updated if behavior changed.
6. No new suppressions without a justifying comment.

## Known traps

- Editing anything in `config/` changes rules for **every module**.
- The 80% JaCoCo gate is bundle-wide in `app` — adding an uncovered class there breaks `verify` (that's why `ApplicationMainTest` exists just to cover `main`).
- CI builds and Trivy-scans `examples/restful-api/Dockerfile`, **not** the root `Dockerfile`; root-Dockerfile changes are not CI-verified.
- `mvn exec:java` uses exec-maven-plugin pinned in the parent's `pluginManagement` (`exec-maven-plugin.version`); keep it pinned there.
- Docs drift. A 2026-07 audit fixed seven doc-vs-build mismatches; verify any build-related claim from `docs/` against `pom.xml` and `config/` before acting on it (see `docs/AGENTS.md`).
- On machines where the default JDK is not 21, export `JAVA_HOME` pointing at a JDK 21 before running `./mvnw` — the build (including Spotless) fails cryptically on older JDKs. Testcontainers ITs need a discoverable Docker daemon; with colima, set `DOCKER_HOST=unix://$HOME/.colima/default/docker.sock`.
