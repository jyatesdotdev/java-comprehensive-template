# AGENTS.md — `app/` (core application)

Read the root `AGENTS.md` first. This file covers only what is specific to this module.

## What this module is

`template-app` — the single runnable production-style Spring Boot application in the
repo. It is deliberately minimal: `Application.java` (`@SpringBootApplication` entry
point), `application.yml` (`spring.application.name`, `server.port: 8080`), and two
tests. New *reference/demo* code belongs in `examples/`, not here; this module is where
real application features would go if the template were instantiated.

Dependencies: `spring-boot-starter-web`, `spring-boot-starter-actuator`,
`logback-classic`, `spring-boot-starter-test`. All versions from the parent BOM —
never add a `<version>` here.

## Commands

```bash
./mvnw -pl app spring-boot:run     # run on :8080 (actuator at /actuator/health)
./mvnw -pl app test                # unit tests
./mvnw -pl app verify              # tests + JaCoCo 80% line-coverage check
```

## The coverage gate — the one thing that will bite you

This is the **only module where JaCoCo is enforced** (80% line coverage, BUNDLE level,
checked at `verify`). Every `examples/*` module sets `jacoco.skip=true`; this one does
not. Because the module is tiny, a single uncovered class can drop the bundle below 80%
and fail the build.

- `ApplicationTest` is a `@SpringBootTest` context-load smoke test.
- `ApplicationMainTest` exists *purely* to cover the `main` method — it uses
  `Mockito.mockStatic(SpringApplication.class)` and verifies `SpringApplication.run`
  was called. Do not delete it as "pointless"; the coverage gate is why it exists.
- **Rule:** any class you add here ships with tests in the same change, or `verify` fails.

## Conventions in this module

- `Application` carries `@SuppressWarnings("PMD.UseUtilityClass")` — Spring entry
  points trigger that rule; keep the suppression, with its comment, on any new entry point.
- Feature code should follow the layered layout demonstrated in
  `examples/restful-api/` (controller → service interface → impl, DTO records at the
  boundary, `@RestControllerAdvice` for errors) and the testing styles in
  `examples/testing/`.
- Configuration goes in `src/main/resources/application.yml`; use Spring profiles for
  environment-specific values (see `examples/database/` for the dual-profile pattern).

## Docker note

The **root** `Dockerfile` builds this module (multi-stage: Temurin 21 JDK +
`apk add unzip` so `./mvnw` can honor `distributionSha256Sum` → Maven 3.9.9 →
layertools extract → JRE Alpine runtime with `wget`, non-root
`app` user, healthcheck on `/actuator/health` — actuator *is* a dependency here,
so that works). However, CI's `container-scan`/`docker` jobs build
`examples/restful-api/Dockerfile` instead, so changes to the root Dockerfile are
**not** exercised by CI. Test locally:

```bash
./mvnw -pl app -am package && docker build -t template-app .
```
