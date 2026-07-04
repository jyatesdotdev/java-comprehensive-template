# AGENTS.md — database `entity` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `entity/AGENTS.md`.
Local: pure unit tests, no persistence. Lifecycle callbacks (`@PrePersist`/
`@PreUpdate`) are package-private so they can be invoked directly — that's why these
tests live in the same package. `OrderStatusTest` pins the constant set and order
because the column is STRING-mapped.
