# AGENTS.md — `patterns` package root

Read `examples/patterns/AGENTS.md` first. One subpackage per GoF category, each with
its own AGENTS.md: `creational/`, `structural/`, `behavioral/`.

- Structure is fixed: ONE `final` holder class per category (private constructor),
  patterns as nested records/sealed interfaces/static classes, each under a
  `// ── Pattern Name ──` banner, each listed in the class javadoc.
- The module's thesis: prefer the language-level expression of a pattern (sealed +
  switch over visitor boilerplate, lambda over single-method subclass, enum over
  double-checked singleton). If you show a classical form, the javadoc must say why.
- JDK-only — no dependencies. New pattern = nested implementation + javadoc entry +
  a `@Nested` test group in the matching test class.
