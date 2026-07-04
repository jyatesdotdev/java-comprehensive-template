# AGENTS.md — `docs/` (human-oriented guides)

Read the root `AGENTS.md` first.

## What this directory is

Nine prose guides for human developers. As an agent you will mostly *reference* them
and occasionally *update* them. They contain some stale facts (verified list below) —
**`pom.xml`, `config/`, and the source code always win over these docs.**

## Which doc to read for what

| Doc | Use it when you need |
|-----|----------------------|
| `TUTORIAL.md` | The new-developer walkthrough (build → run REST API → add a feature → scans → Docker) |
| `TOOLCHAIN.md` | Tool install matrix (JDK/Maven/Docker/Git), IDE setup, plugin version table |
| `EXTENDING.md` | **Step-by-step recipes**: add a Maven module, REST endpoint, service, dependency, quality suppression |
| `best-practices.md` | The full coding standard (10 sections: style, naming, errors, logging, null safety, records/sealed, collections, concurrency, resources, principles) |
| `architecture-patterns.md` | Template layering + microservices/hexagonal/CQRS/event-sourcing patterns and when (not) to use them |
| `third-party-libraries.md` | Library selection guidance (Spring starters, Commons vs Guava vs JDK, Jackson, MapStruct, Lombok policy) |
| `documentation-standards.md` | Javadoc conventions (required scope, tag order), README templates, OpenAPI setup, changelog format |
| `development-workflow.md` | Branching, Conventional Commits, release process (`versions:set` → tag → deploy), CI stages |
| `SECURITY_SCANNING.md` | Per-tool suppression syntax, scan profiles, CVSS thresholds, CI security jobs |

## Doc-accuracy policy

A 2026-07-04 audit found and fixed seven doc-vs-reality drifts (config paths claimed
"at project root" instead of `config/<tool>/`, Java 17 instead of 21, `/api/products`
instead of `/api/v1/products`, a 70% coverage claim instead of 80%, an omitted `app`
module, `google_checks.xml` instead of the custom checkstyle config, and an OWASP
version drift). The lesson stands: **these docs drift**. Before repeating any
build-related claim from them (versions, paths, thresholds, commands), verify it
against `pom.xml`, `config/`, or the source. If you find a drift, fix the doc in the
same change and note it here if it suggests a pattern.

## Rules for editing docs

- When you change build behavior (pom, config, CI, Dockerfile), update the affected
  doc(s) **and** the relevant AGENTS.md in the same change.
- Commands in docs must be copy-pasteable from the repo root and use `./mvnw`.
- Follow `documentation-standards.md` for structure; keep the root `README.md`'s
  module/doc tables in sync when adding a module or doc.
- Cross-link with relative paths (`[EXTENDING](EXTENDING.md)`), and update the root
  `README.md` table of contents if you add or rename a doc.
