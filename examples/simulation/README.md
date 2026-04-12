# Simulation Examples

Monte Carlo and discrete event simulation patterns in pure Java 17+.

## Contents

| Class | Pattern | Key Concepts |
|-------|---------|-------------|
| `MonteCarloSimulation` | Monte Carlo method | Parallel streams, Welford's online variance, confidence intervals, option pricing |
| `DiscreteEventSimulation` | Discrete Event Simulation | Priority-queue event loop, M/M/1 queuing model, exponential distributions |

## Monte Carlo Simulation

A generic engine that runs N independent stochastic trials and aggregates results using Welford's online algorithm for numerically stable mean/variance computation.

### Examples included

- **Pi estimation** — random points in the unit square; ratio inside the unit circle converges to π/4
- **European call option pricing** — geometric Brownian motion with discounted payoff (Black-Scholes Monte Carlo)

```java
// Estimate Pi with 2M parallel trials
var result = MonteCarloSimulation.run(2_000_000, true, MonteCarloSimulation::piTrial);
System.out.printf("Pi ≈ %.6f (95%% CI: ±%.6f)%n",
        result.mean(), result.confidenceInterval95());

// Price a European call option
var trial = MonteCarloSimulation.europeanCallTrial(
        100,   // spot price
        105,   // strike
        0.05,  // risk-free rate
        0.2,   // volatility
        1.0    // time to expiry (years)
);
var price = MonteCarloSimulation.run(1_000_000, true, trial);
```

### Design decisions

- `DoubleSupplier` as the trial interface — composable, works with lambdas and method references
- `ThreadLocalRandom` for thread-safe parallel execution without contention
- Single-pass Welford's algorithm avoids storing all trial results in memory

## Discrete Event Simulation

A priority-queue-based DES engine where events are `(time, name, action)` tuples processed in chronological order.

### M/M/1 Queue example

Models a single-server queue with Poisson arrivals (rate λ) and exponential service times (rate μ):

```java
var sim = new DiscreteEventSimulation();
var queue = new DiscreteEventSimulation.MM1Queue(sim, 0.8, 1.0); // λ=0.8, μ=1.0
queue.scheduleArrival(0.0);
sim.runUntil(100_000);
queue.printStats();
```

Outputs server utilization, average system time, and max queue length — values converge to the theoretical M/M/1 results as simulation time increases.

### Extending the engine

Schedule custom events by providing a time and a `Runnable`:

```java
var sim = new DiscreteEventSimulation();
sim.schedule(0.0, "init", () -> {
    System.out.println("Simulation started at t=" + sim.clock());
    sim.scheduleDelay(5.0, "check", () ->
        System.out.println("Checkpoint at t=" + sim.clock()));
});
sim.runUntil(100);
```

## How to Run

```bash
# From project root
./mvnw -pl examples/simulation compile

# Run Monte Carlo demo
./mvnw -pl examples/simulation exec:java -Dexec.mainClass=com.example.template.simulation.MonteCarloSimulation

# Run DES demo
./mvnw -pl examples/simulation exec:java -Dexec.mainClass=com.example.template.simulation.DiscreteEventSimulation
```

## When to Use Each Approach

| Technique | Best For | Not Ideal For |
|-----------|----------|---------------|
| Monte Carlo | Estimating expected values, risk analysis, integration | Deterministic problems, real-time systems |
| Discrete Event Simulation | Queuing systems, process modeling, resource allocation | Continuous dynamics (use ODE solvers instead) |

## Related Documentation

- [Main README](../../README.md) — Project overview and quick start
- [HPC Module](../hpc/README.md) — Parallel execution for Monte Carlo
- [Best Practices](../../docs/best-practices.md) — Functional style, code conventions
- [Tutorial](../../docs/TUTORIAL.md) — New developer walkthrough
