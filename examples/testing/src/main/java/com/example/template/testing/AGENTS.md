# AGENTS.md — `testing` main sources

Read `examples/testing/AGENTS.md` first — the tests are this module's real content;
these main classes exist to be tested.

- Keep them minimal and exemplary: `model/User` (record with compact-constructor
  validation — `Objects.requireNonNull` + format checks), `repository/UserRepository`
  (interface only — "code to interfaces for testability"), `service/UserService`
  (constructor-injected, throws `IllegalStateException`/`IllegalArgumentException`).
- **ArchUnit enforces this package's structure at build time**
  (`ArchitectureRulesTest`): services must not depend on services, repositories must
  not depend on services, no package cycles, and everything in `model` must be a
  record. Violating any of these fails `mvn test`, not just review.
- Don't grow features here — if a testing technique needs a richer target, add the
  smallest class that exercises it, in the layer-appropriate package.
