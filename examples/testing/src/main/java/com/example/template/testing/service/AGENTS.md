# AGENTS.md — `testing.service`

Rules: `../AGENTS.md` (testing main sources). Local invariants: plain classes with
**constructor injection** (no Spring annotations needed — that's what makes them
trivially unit-testable), unchecked exceptions for violations
(`IllegalStateException` duplicate, `IllegalArgumentException` missing). ArchUnit
forbids service→service dependencies.
