# AGENTS.md — `patterns.creational`

Read `examples/patterns/AGENTS.md` for the module rules.

`CreationalPatterns` — object *creation* patterns and their modern idioms:
Builder → record + step-builder interfaces; Factory Method → sealed `Shape` + static
`of()` switch; Singleton → **enum** (`AppConfig.INSTANCE`); Prototype → record
"withers"; Abstract Factory → `Map<String, Supplier<Widget>>` registry.

Local invariants:
- Factory methods validate ALL inputs and throw `IllegalArgumentException` — unknown
  type names AND missing varargs dimensions (`Shape.of` does both via the private
  `dim(...)` helper; keep that when adding shapes).
- `HttpRequest.Builder.build()` copies headers into an unmodifiable `LinkedHashMap`
  **on purpose** — `Map.copyOf` would discard insertion order; there's a comment,
  don't simplify it away.
- Withers return new records and never mutate the original; built objects are
  immutable (`List.copyOf` for collection components).
- Documented demo simplification: `AppConfig.get` returns `null` for absent keys per
  its javadoc.
