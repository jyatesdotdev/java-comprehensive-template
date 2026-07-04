# AGENTS.md — patterns `creational` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `creational/AGENTS.md`.
Local: `CreationalPatternsTest`, one `@Nested` group per pattern. Behavior pins:
header assertions are order-aware (the builder preserves insertion order);
`Shape.of` throws `IllegalArgumentException` for unknown types AND missing
dimensions; withers never mutate the original; the widget registry rejects unknown
keys and returns fresh instances.
