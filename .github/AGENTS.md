# AGENTS.md — `.github/` (CI pipeline)

Read the root `AGENTS.md` first. This directory holds `workflows/ci.yml`, the single CI
pipeline, and `dependabot.yml`.

## Pipeline shape (triggers: push to `main`/`develop`, PRs to `main`/`develop`)

- **Hard gates:** `build` (`./mvnw -B clean verify -DskipITs`), `quality-gates`
  (`./mvnw -B spotless:check` then `./mvnw -B verify -Psecurity-scan-quick -DskipTests`),
  and `integration-tests` (`./mvnw -B verify -pl examples/testing -Pintegration-tests
  -Dsurefire.skip=true`). JaCoCo 80% is enforced in `build` on `app` only — there is
  no aggregate report step.
- **Soft jobs (`continue-on-error: true`):** `dependency-scan` (OWASP),
  `container-scan` (Trivy), `snyk-scan`. They report but never fail the pipeline.
- **`docker` job:** main-branch only; builds `examples/restful-api/Dockerfile`
  (NOT the root `Dockerfile` — root-Dockerfile changes are never CI-tested).

## Rules for editing

- Everything runs on Temurin **21** with Maven caching — keep `JAVA_VERSION` in sync
  with the root pom's `java.version`.
- Pin Actions to commit SHAs with a `# vX.Y.Z` comment; do not float on major tags
  or `@master`.
- Config knobs live in repo settings, not the file: `vars.OWASP_CVSS_THRESHOLD`
  (default 7), `secrets.NVD_API_KEY`, `secrets.SNYK_TOKEN`. Don't hardcode values.
- Any new Maven invocation must use `./mvnw -B` and an existing profile; if you need
  new behavior, add a profile in the root pom rather than inlining plugin config here.
- SARIF uploads feed the GitHub Security tab — keep the `category` values distinct
  per scanner.
- `dependabot.yml` covers weekly `maven` (root) and `github-actions` updates.
  Ignore semver-major (Spring Boot 4, Spark 4, Actions v7, …) and unused parent-DM
  libraries. Cap open PRs at 3; group Actions patch/minor into one PR.
