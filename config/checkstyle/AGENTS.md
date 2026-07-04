# AGENTS.md — `config/checkstyle/`

Read `config/AGENTS.md` first — it explains how these files bind to every module.

- `checkstyle.xml` — the rule set (Google-style base + security regexes). Fails the
  build on *warning* severity. Key hard limits live here: method ≤ 60 lines,
  ≤ 7 params, cyclomatic ≤ 15, file ≤ 500 lines, no `System.out` (use SLF4J),
  `Runtime.exec` = error.
- `checkstyle-suppressions.xml` — file-pattern suppressions only. Existing patterns
  already relax `*Test`/`*IT` files and allow `System.out` in `systems`/`simulation`.

Rules: prefer inline `@SuppressWarnings("checkstyle:RuleName")` in one file over a new
pattern here; every new `<suppress>` gets an XML comment saying what and why; patterns
must be as narrow as possible (match a file name, not a whole tree). Validate with
`./mvnw checkstyle:check -pl <module>` then the full
`./mvnw verify -Psecurity-scan-quick -DskipTests`. The Checkstyle engine version is
pinned in the root pom (10.14.2) — syntax must be valid for that version.
