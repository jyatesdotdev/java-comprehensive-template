# AGENTS.md — `testing.archunit` (architecture enforcement)

Read the test-tree AGENTS.md one level up first.

`ArchitectureRulesTest` — `@AnalyzeClasses(packages = "com.example.template.testing",
importOptions = DoNotIncludeTests)` with `@ArchTest static final ArchRule` fields.
Four rules are ENFORCED (they fail `mvn test`, not just review): services don't
depend on services, repositories don't depend on services, no package cycles, and
`model` classes must be records.

- Rules are `@ArchTest` **fields**, not `@Test` methods — hence the justified
  `@SuppressWarnings("PMD.TestClassWithoutTestCases")`; keep it.
- Every rule carries a `.because("...")` clause — a rule nobody can explain gets
  deleted in review.
- Scope is this module's packages. To guard another module's architecture, add an
  ArchUnit test THERE (dependency is parent-managed, add `archunit-junit5` test-scope)
  rather than widening this one's package scan.
- New structural conventions (layer rules, naming rules) belong here as new
  `@ArchTest` fields — that turns a convention into a build failure, which is the
  whole point.
