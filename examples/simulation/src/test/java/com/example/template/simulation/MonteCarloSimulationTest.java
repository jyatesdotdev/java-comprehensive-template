package com.example.template.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.core.api.Assertions.within;

import com.example.template.simulation.MonteCarloSimulation.Result;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Statistical tests for {@link MonteCarloSimulation}.
 *
 * <p>The engine uses {@code ThreadLocalRandom} (no seeding), so stochastic assertions use
 * tolerances that are dozens of standard errors wide — the flake probability is negligible (far
 * below 1e-6). Deterministic suppliers are used wherever exact checks are possible.
 */
@DisplayName("MonteCarloSimulation")
class MonteCarloSimulationTest {

  private static final long PI_TRIALS = 1_000_000;

  /**
   * Standard error of the pi estimator at 1M trials: the per-trial stddev is sqrt(pi * (4 - pi)) ~=
   * 1.642, so SE ~= 0.00164. A tolerance of 0.05 is ~30 sigma.
   */
  private static final double PI_TOLERANCE = 0.05;

  // ── Pi estimation ───────────────────────────────────────────────────

  @Test
  @DisplayName("pi estimate converges (sequential)")
  void piEstimateSequential() {
    Result result = MonteCarloSimulation.run(PI_TRIALS, false, MonteCarloSimulation::piTrial);

    assertThat(result.trials()).isEqualTo(PI_TRIALS);
    assertThat(result.mean()).isCloseTo(Math.PI, offset(PI_TOLERANCE));
    // Per-trial stddev of the {0, 4} Bernoulli-style trial: sqrt(pi * (4 - pi)) ~= 1.6422
    assertThat(result.stddev()).isCloseTo(1.6422, offset(PI_TOLERANCE));
    assertThat(result.confidenceInterval95()).isPositive().isLessThan(0.01);
  }

  @Test
  @DisplayName("pi estimate converges (parallel)")
  void piEstimateParallel() {
    Result result = MonteCarloSimulation.run(PI_TRIALS, true, MonteCarloSimulation::piTrial);

    assertThat(result.trials()).isEqualTo(PI_TRIALS);
    assertThat(result.mean()).isCloseTo(Math.PI, offset(PI_TOLERANCE));
    assertThat(result.stddev()).isCloseTo(1.6422, offset(PI_TOLERANCE));
  }

  // ── Deterministic suppliers ─────────────────────────────────────────

  @Test
  @DisplayName("constant trial has the constant mean and zero variance (sequential)")
  void constantTrialSequential() {
    Result result = MonteCarloSimulation.run(10_000, false, () -> 2.5);

    assertThat(result.mean()).isCloseTo(2.5, offset(1e-12));
    assertThat(result.stddev()).isCloseTo(0.0, offset(1e-12));
    assertThat(result.min()).isEqualTo(2.5);
    assertThat(result.max()).isEqualTo(2.5);
    assertThat(result.confidenceInterval95()).isCloseTo(0.0, offset(1e-12));
  }

  @Test
  @DisplayName("constant trial has zero variance through the parallel combiner")
  void constantTrialParallel() {
    Result result = MonteCarloSimulation.run(100_000, true, () -> 2.5);

    assertThat(result.mean()).isCloseTo(2.5, offset(1e-9));
    assertThat(result.stddev()).isCloseTo(0.0, offset(1e-9));
  }

  @Test
  @DisplayName("Welford single-pass mean/variance matches a hand-computed sample")
  void welfordMatchesHandComputedSample() {
    // Sample: 2, 4, 4, 4, 5, 5, 7, 9 -> mean = 5, sum of squared deviations = 32,
    // sample variance = 32 / 7, sample stddev = sqrt(32 / 7) ~= 2.13809.
    // The supplier cycles, so the engine's second min/max pass (8 more draws)
    // sees the same 8 values again: min = 2, max = 9.
    DoubleSupplier supplier = cycling(2, 4, 4, 4, 5, 5, 7, 9);

    Result result = MonteCarloSimulation.run(8, false, supplier);

    assertThat(result.trials()).isEqualTo(8);
    assertThat(result.mean()).isCloseTo(5.0, offset(1e-9));
    assertThat(result.stddev()).isCloseTo(Math.sqrt(32.0 / 7.0), offset(1e-9));
    assertThat(result.min()).isEqualTo(2.0);
    assertThat(result.max()).isEqualTo(9.0);
  }

  @Test
  @DisplayName("confidenceInterval95 implements 1.96 * stddev / sqrt(trials)")
  void confidenceInterval95Formula() {
    // Deterministic record construction: 1.96 * 3.0 / sqrt(400) = 0.294.
    Result result = new Result(400, 10.0, 3.0, 0.0, 20.0);

    assertThat(result.confidenceInterval95()).isCloseTo(1.96 * 3.0 / 20.0, offset(1e-12));
    assertThat(result.confidenceInterval95()).isCloseTo(0.294, offset(1e-12));
  }

  // ── European call option ────────────────────────────────────────────

  @Test
  @DisplayName("European call price is positive and near the Black-Scholes closed form")
  void europeanCallPriceNearBlackScholes() {
    // Black-Scholes for S=100, K=105, r=0.05, sigma=0.2, T=1:
    //   d1 = (ln(100/105) + (0.05 + 0.02)) / 0.2 = 0.10605, d2 = d1 - 0.2 = -0.09395
    //   C  = 100 * N(d1) - 105 * e^(-0.05) * N(d2) ~= 8.02
    // Payoff stddev is ~13, so SE at 1M trials is ~0.013; a 1.5 band is >100 sigma.
    DoubleSupplier trial = MonteCarloSimulation.europeanCallTrial(100, 105, 0.05, 0.2, 1.0);

    Result result = MonteCarloSimulation.run(PI_TRIALS, true, trial);

    assertThat(result.mean()).isPositive();
    assertThat(result.mean()).isCloseTo(8.02, within(1.5));
    // A call payoff is bounded below by zero, and some paths must finish out of the money.
    assertThat(result.min()).isGreaterThanOrEqualTo(0.0);
    assertThat(result.max()).isPositive();
  }

  // ── Helpers ─────────────────────────────────────────────────────────

  private static DoubleSupplier cycling(double... values) {
    AtomicInteger index = new AtomicInteger();
    return () -> values[index.getAndIncrement() % values.length];
  }
}
