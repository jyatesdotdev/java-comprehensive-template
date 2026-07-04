# AGENTS.md — `examples/simulation/`

Read the root and `examples/AGENTS.md` first. Two simulation engines, each a final
class with a `main` demo.

## Layout

| Class | Shows |
|-------|-------|
| `MonteCarloSimulation` | Generic engine: `run(long trials, boolean parallel, DoubleSupplier trial)` over `DoubleStream`, **single-pass Welford online mean/variance** with a parallel-safe combiner; `Result` record with a 95% confidence interval; example trials: π estimation and European call option pricing (geometric Brownian motion) |
| `DiscreteEventSimulation` | Priority-queue event loop (`Event` record implements `Comparable` on time), `schedule`/`scheduleDelay`/`runUntil`/`runEvents`; nested `MM1Queue` — Poisson arrivals + exponential service via inverse-transform sampling, tracks utilization/wait times, warns on ρ ≥ 1 |

## Rules this module encodes

- **`ThreadLocalRandom` for anything that may run parallel** — never share a `Random`
  across threads. (SpotBugs' `PREDICTABLE_RANDOM` is suppressed repo-wide for example
  code; that's acceptable for simulations, but use `SecureRandom` if you ever write
  security-relevant randomness.)
- Statistics in one pass: extend the Welford accumulator pattern rather than buffering
  all samples (the engine caps its secondary min/max pass at 10,000 samples on purpose).
- Results are immutable records; engines validate inputs (`schedule` throws
  `IllegalArgumentException` for past times).
- `System.out` demo output is **allowed here** (Checkstyle suppression for
  `.*simulation[/\\].*` + `@SuppressWarnings("PMD.SystemPrintln")`); that allowance is
  package-local.

## Commands

```bash
./mvnw -pl examples/simulation compile
./mvnw -pl examples/simulation exec:java -Dexec.mainClass=com.example.template.simulation.MonteCarloSimulation
./mvnw -pl examples/simulation exec:java -Dexec.mainClass=com.example.template.simulation.DiscreteEventSimulation
```

`exec:java` relies on plugin-prefix auto-resolution (exec-maven-plugin is not declared
anywhere in the repo) — fine for demos, declare it if you need reproducibility.

## Gotchas

- **`MM1Queue` constructor order is `(sim, arrivalRate λ, serviceRate μ)`** and the
  system is only stable when λ < μ (a constructor warning fires otherwise).
- `MM1Queue.busyTime()` closes any in-progress service interval at the current clock —
  the raw accumulator alone would under-report utilization if the run stops
  mid-service. Read utilization through the accessor, never the field.
- Tests (15): `MonteCarloSimulationTest` uses seed-free statistical assertions with
  tolerances sized to >10σ (effectively cannot flake) plus deterministic Welford checks
  via a cycling supplier; `DiscreteEventSimulationTest` is fully deterministic. Match
  those styles — with `ThreadLocalRandom` there is no seeding, so any statistical
  assertion must carry a generous, justified tolerance.
- Engine quirks to know: `MonteCarloSimulation.run` draws min/max from a *separate*
  re-sample of up to 10,000 trials (a stateful supplier gets extra invocations), and
  `trials < 1` is unguarded; equal-time events have unspecified ordering.
- `DiscreteEventSimulation` carries a broad justified suppression list
  ("Simulation example code") — keep new suppressions equally narrow and commented.
- No dependencies beyond the parent; keep it JDK-only. `jacoco.skip=true`.
