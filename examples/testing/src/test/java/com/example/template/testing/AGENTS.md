# AGENTS.md — `testing` test tree (the repo's testing reference)

Read `examples/testing/AGENTS.md` first. Each subpackage showcases one technique and
has its own AGENTS.md: `junit5/`, `mockito/`, `archunit/` (unit — surefire),
`integration/` (Testcontainers — failsafe, Docker required).

- The suffix IS the routing: `*Test` → surefire (`./mvnw test`); `*IT` → failsafe
  (`./mvnw verify -Pintegration-tests`). This module's pom excludes `**/*IT.java`
  from surefire — name files accordingly or they run in the wrong phase (or not at all).
- House style shown here and expected repo-wide: JUnit 5 + AssertJ `assertThat`
  (Hamcrest only inside REST Assured chains), `@DisplayName` on classes and tests,
  `@Nested` for grouping, parameterized tests over copy-paste.
- When another module needs a testing pattern, it copies from here — so changes to
  these classes are changes to the reference. Keep them exemplary and commented.
