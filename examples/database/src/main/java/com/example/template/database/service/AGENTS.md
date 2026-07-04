# AGENTS.md — `database.service`

Read `examples/database/AGENTS.md` for the persistence patterns.

- **The service layer is the transaction boundary** — not controllers, not
  repositories. Class-level `@Transactional(readOnly = true)`; each write method
  overrides with `@Transactional(rollbackFor = Exception.class)` plus explicit
  propagation/isolation only where the semantics demand it (and a comment saying why).
- Missing-row lookups throw `IllegalArgumentException` with a consistent
  `"Order not found: <id>"` message (`orElseThrow` on the repository's `Optional`);
  invalid state transitions throw `IllegalStateException`. Both documented with
  `@throws`.
- Constructor injection; SLF4J parameterized logging.
- Lazy associations are only safe inside these transactional methods — anything
  returned to a non-transactional caller must be eagerly fetched
  (`findByIdWithItems`).
- Every public method has Mockito-based unit tests in `src/test/.../service/`
  covering success and each exception path.
