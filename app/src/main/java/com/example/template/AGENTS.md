# AGENTS.md — `app` main sources (`com.example.template`)

Read `app/AGENTS.md` first (coverage gate!) and the root `AGENTS.md`.

- This package holds only `Application.java`. New features go in subpackages following
  the standard layout demonstrated by `examples/restful-api`: `controller/`,
  `service/`, `dto/`, `domain/`, `exception/`, `config/` — controllers depend on
  service *interfaces*, DTOs at the HTTP boundary, constructor injection everywhere.
- **Every class you add here needs tests in the same change** — this is the only
  module with the 80% JaCoCo line-coverage gate enforced at `verify`.
- Keep `@SuppressWarnings("PMD.UseUtilityClass")` (with its comment) on the Spring
  entry point; that rule false-positives on `@SpringBootApplication` classes.
