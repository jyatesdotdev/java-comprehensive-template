# AGENTS.md — restful-api `service` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `service/AGENTS.md`.
Local: plain unit tests, no Spring context, no mocks needed (the in-memory impl IS
the storage). Behavior pins: `findAll` returns an immutable snapshot
(`UnsupportedOperationException` on mutation), `ResourceNotFoundException` on every
missing-id path, distinct generated ids.
