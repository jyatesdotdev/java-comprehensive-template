# AGENTS.md — `simulation` tests

Read `examples/simulation/AGENTS.md` first.

- Two styles, use the right one:
  1. **Deterministic** — drive the DES engine with hand-scheduled events; feed Monte
     Carlo a deterministic `DoubleSupplier` (e.g., an `AtomicInteger`-cycled fixed
     sample) so Welford results are exact to 1e-9.
  2. **Statistical** — no seeding exists (`ThreadLocalRandom`), so every stochastic
     assertion carries a tolerance sized to **>10σ** with a comment justifying the math
     (e.g., π ± 0.05 at 1M trials, SE ≈ 0.00164). If you can't justify the band,
     the test is a flake waiting to happen.
- Watch the engine quirk: `MonteCarloSimulation.run` re-samples up to 10,000 extra
  trials for min/max — cycling suppliers make that pass see the same values;
  stateful ones get extra invocations.
- Keep the module's test run under ~30 seconds. PMD applies: assertions everywhere,
  no `System.out`.
