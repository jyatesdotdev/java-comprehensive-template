# AGENTS.md — `config/spotbugs/`

Read `config/AGENTS.md` first — it explains how this filter binds to every module.

- `spotbugs-exclude.xml` — exclusion filter for SpotBugs + FindSecBugs (effort=Max,
  threshold=Low, so findings are aggressive by default). Each `<Match>` block has a
  comment with its rationale.
- The big OR-block scoped to `com.example.template.*` suppresses style/false-positive
  patterns for example code — including the security finding **`PREDICTABLE_RANDOM`**
  (fine for simulations/demos). If this template is instantiated for production,
  narrow or remove that entry before writing security-sensitive randomness.

Rules: match on the narrowest scope possible (`<Class>` + `<Bug>` beats a package
match); never add a FindSecBugs pattern without stating in the comment how you
confirmed it's a false positive; prefer `@SuppressFBWarnings(justification = "...")`
at a single site over a filter entry. Test with `./mvnw spotbugs:check -pl <module>`.
