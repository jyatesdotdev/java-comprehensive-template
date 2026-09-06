# AGENTS.md — `restful-api.archunit`

Read `examples/restful-api/src/test/java/com/example/template/restfulapi/AGENTS.md`
first. These rules guard the layering students copy, not `examples/testing`.

- `@AnalyzeClasses(packages = "com.example.template.restfulapi")` with
  `DoNotIncludeTests`. Rules are `@ArchTest` fields (keep
  `PMD.TestClassWithoutTestCases`).
- Controllers must not depend on persistence packages. The module must not depend
  on sibling `com.example.template.*` packages. Package slices must be acyclic.
- Add new REST-layer conventions here as `@ArchTest` fields; do not widen
  `examples/testing` ArchUnit to scan this module.
