# AGENTS.md — `config/spotbugs/`

Read `config/AGENTS.md` first — it explains how this filter binds to every module.

- `spotbugs-exclude.xml` — exclusion filter for SpotBugs + FindSecBugs (effort=Max,
  threshold=Low, so findings are aggressive by default). Each `<Match>` block has a
  comment with its rationale.
- Style/false-positive patterns may match `com.example.template.*`. Security-relevant
  patterns (`PREDICTABLE_RANDOM`, `EI_EXPOSE_REP`/`2`, nullable-return / ignored-return
  value) are **package-narrowed** to the demos that need them. Never broaden those
  back to the whole template package.

Rules: match on the narrowest scope possible (`<Class>` + `<Bug>` beats a package
match); never add a FindSecBugs pattern without stating in the comment how you
confirmed it's a false positive; prefer `@SuppressFBWarnings(justification = "...")`
at a single site over a filter entry. Test with `./mvnw spotbugs:check -pl <module>`.
