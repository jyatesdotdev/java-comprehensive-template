# AGENTS.md — `.github/` (CI pipeline)

Read the root `AGENTS.md` first. This directory holds `workflows/ci.yml`, the single CI pipeline.

## Pipeline shape (triggers: push to `main`/`develop`, PRs to `main`)

- **Hard gates:** `build` (`./mvnw -B clean verify -DskipITs`) and `quality-gates`
  (`./mvnw -B verify -Psecurity-scan-quick -DskipTests` + JaCoCo aggregate/check).
  If you change quality rules or add code, these are the jobs that must stay green.
- **Soft jobs (`continue-on-error: true`):** `integration-tests` (failsafe on
  `examples/testing`), `dependency-scan` (OWASP), `container-scan` (Trivy),
  `snyk-scan`. They report but never fail the pipeline. The integration-tests flag
  carries a TODO to remove it once consistently green — if you remove it, run the ITs
  locally with Docker first.
- **`docker` job:** main-branch only; builds `examples/restful-api/Dockerfile`
  (NOT the root `Dockerfile` — root-Dockerfile changes are never CI-tested).

## Rules for editing

- Everything runs on Temurin **21** with Maven caching — keep `JAVA_VERSION` in sync
  with the root pom's `java.version`.
- Config knobs live in repo settings, not the file: `vars.OWASP_CVSS_THRESHOLD`
  (default 7), `secrets.NVD_API_KEY`, `secrets.SNYK_TOKEN`. Don't hardcode values.
- Any new Maven invocation must use `./mvnw -B` and an existing profile; if you need
  new behavior, add a profile in the root pom rather than inlining plugin config here.
- SARIF uploads feed the GitHub Security tab — keep the `category` values distinct
  per scanner.
