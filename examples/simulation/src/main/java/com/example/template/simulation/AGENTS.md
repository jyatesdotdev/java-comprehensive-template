# AGENTS.md — `simulation` package

Read `examples/simulation/AGENTS.md` first — it covers the engines, run commands, and
known quirks.

- Two `final` classes, each with a `main` demo: `MonteCarloSimulation` (generic
  `run(trials, parallel, DoubleSupplier)` with single-pass Welford mean/variance and a
  parallel-safe combiner) and `DiscreteEventSimulation` (priority-queue event loop +
  nested `MM1Queue`).
- Randomness is `ThreadLocalRandom` — required for the parallel paths, never a shared
  `Random`. There is no seeding; determinism comes from deterministic suppliers in
  tests, not seeds.
- Statistics are accumulated in one pass (Welford mean/variance **and** min/max) —
  don't buffer all samples or run a second `DoubleStream` of new draws. Results are
  immutable records.
- Engines validate input (`schedule` rejects past times with
  `IllegalArgumentException`); derived stats are exposed through accessors that
  self-correct (`busyTime()` closes any in-progress service interval — read through
  it, never the raw field).
- `System.out` demo output is allowed in THIS package only (Checkstyle suppression +
  `@SuppressWarnings("PMD.SystemPrintln")`).
