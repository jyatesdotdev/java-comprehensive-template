package com.example.template.simulation;

import java.util.DoubleSummaryStatistics;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import java.util.stream.DoubleStream;

/**
 * Generic Monte Carlo simulation engine.
 *
 * <p>Runs N independent trials of a stochastic experiment and aggregates results.
 * Supports both sequential and parallel execution.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * var result = MonteCarloSimulation.run(1_000_000, true, MonteCarloSimulation::piTrial);
 * System.out.printf("Pi ≈ %.6f (stddev=%.6f)%n", result.mean(), result.stddev());
 * }</pre>
 */
@SuppressWarnings("PMD.SystemPrintln") // Simulation example code
public final class MonteCarloSimulation {

    /**
     * Aggregated result of a Monte Carlo run.
     *
     * @param trials number of trials executed
     * @param mean   sample mean of trial outcomes
     * @param stddev sample standard deviation
     * @param min    minimum observed value (from a sample)
     * @param max    maximum observed value (from a sample)
     */
    public record Result(long trials, double mean, double stddev, double min, double max) {
        /**
         * Computes the 95% confidence interval half-width for the mean.
         *
         * @return margin of error at 95% confidence
         */
        public double confidenceInterval95() {
            return 1.96 * stddev / Math.sqrt(trials);
        }
    }

    /**
     * Run a Monte Carlo simulation.
     *
     * @param trials   number of independent trials
     * @param parallel whether to use parallel streams
     * @param trial    supplier that returns one trial's outcome
     * @return aggregated statistics
     */
    public static Result run(long trials, boolean parallel, DoubleSupplier trial) {
        DoubleStream stream = DoubleStream.generate(trial).limit(trials);
        if (parallel) {
            stream = stream.parallel();
        }

        // Collect mean and variance in a single pass using Welford's algorithm
        double[] state = stream.collect(
                () -> new double[3], // [count, mean, M2]
                (s, x) -> {
                    s[0]++;
                    double delta = x - s[1];
                    s[1] += delta / s[0];
                    s[2] += delta * (x - s[1]);
                },
                (a, b) -> {
                    double count = a[0] + b[0];
                    double delta = b[1] - a[1];
                    a[1] = (a[0] * a[1] + b[0] * b[1]) / count;
                    a[2] += b[2] + delta * delta * a[0] * b[0] / count;
                    a[0] = count;
                }
        );

        double mean = state[1];
        double variance = state[0] > 1 ? state[2] / (state[0] - 1) : 0.0;

        // Second pass for min/max (cheap relative to the trial computation)
        DoubleSummaryStatistics stats = DoubleStream.generate(trial)
                .limit(Math.min(trials, 10_000))
                .summaryStatistics();

        return new Result(trials, mean, Math.sqrt(variance), stats.getMin(), stats.getMax());
    }

    // ── Example Trials ──────────────────────────────────────────────────

    /**
     * Pi estimation: returns 4.0 if random point falls inside unit circle, else 0.0.
     * Mean converges to π.
     *
     * @return 4.0 if the point is inside the unit circle, 0.0 otherwise
     */
    public static double piTrial() {
        var rng = ThreadLocalRandom.current();
        double x = rng.nextDouble();
        double y = rng.nextDouble();
        return (x * x + y * y <= 1.0) ? 4.0 : 0.0;
    }

    /**
     * European call option pricing via Monte Carlo.
     *
     * <p>Uses geometric Brownian motion to simulate the stock price at expiry,
     * then computes the discounted payoff.
     *
     * @param spot       current stock price
     * @param strike     option strike price
     * @param riskFree   annual risk-free rate (e.g. 0.05)
     * @param volatility annual volatility (e.g. 0.2)
     * @param timeToExp  time to expiration in years
     * @return a trial supplier for use with {@link #run}
     */
    public static DoubleSupplier europeanCallTrial(double spot, double strike,
                                                    double riskFree, double volatility,
                                                    double timeToExp) {
        double drift = (riskFree - 0.5 * volatility * volatility) * timeToExp;
        double diffusion = volatility * Math.sqrt(timeToExp);
        double discount = Math.exp(-riskFree * timeToExp);

        return () -> {
            double z = ThreadLocalRandom.current().nextGaussian();
            double priceAtExpiry = spot * Math.exp(drift + diffusion * z);
            return discount * Math.max(priceAtExpiry - strike, 0.0);
        };
    }

    /**
     * Demo entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println("=== Monte Carlo: Pi Estimation ===");
        var piResult = run(2_000_000, true, MonteCarloSimulation::piTrial);
        System.out.printf("  Pi ≈ %.6f  (95%% CI: ±%.6f, %d trials)%n",
                piResult.mean(), piResult.confidenceInterval95(), piResult.trials());

        System.out.println("\n=== Monte Carlo: European Call Option ===");
        var optionTrial = europeanCallTrial(100, 105, 0.05, 0.2, 1.0);
        var optionResult = run(1_000_000, true, optionTrial);
        System.out.printf("  Price ≈ $%.4f  (95%% CI: ±$%.4f)%n",
                optionResult.mean(), optionResult.confidenceInterval95());
    }

    private MonteCarloSimulation() {}
}
