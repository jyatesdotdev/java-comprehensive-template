# AGENTS.md — `testing.repository`

Rules: `../AGENTS.md` (testing main sources). Local invariant: **interfaces only** —
this package exists to demonstrate "code to interfaces for testability" (mocked in
`mockito/`, ArchUnit forbids depending on services). No implementations here; tests
supply mocks or fakes.
