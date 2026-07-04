# AGENTS.md — `testing.model`

Rules: `../AGENTS.md` (testing main sources). Local invariant: **everything in this
package must be a record** — enforced at build time by an ArchUnit rule in
`archunit/ArchitectureRulesTest`. Validate invariants in compact constructors
(`Objects.requireNonNull`, format checks) like `User` does.
