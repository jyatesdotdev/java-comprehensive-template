package com.example.template.systems;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Performance optimization patterns with JMH micro-benchmarks.
 *
 * <p>Run benchmarks:
 * <pre>{@code
 * mvn -pl examples/systems compile exec:java \
 *   -Dexec.mainClass="com.example.template.systems.PerformanceBenchmarks"
 * }</pre>
 *
 * <p>Key optimization principles:
 * <ol>
 *   <li>Measure first — never optimize without profiling data</li>
 *   <li>Prefer primitives over boxed types in hot paths</li>
 *   <li>Minimize allocations in tight loops</li>
 *   <li>Use StringBuilder for string concatenation in loops</li>
 *   <li>Choose the right data structure (ArrayList vs LinkedList, HashMap capacity)</li>
 *   <li>Leverage JIT: keep hot methods small, avoid megamorphic call sites</li>
 * </ol>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class PerformanceBenchmarks {

    private static final int SIZE = 10_000;

    // --- String concatenation ---

    /**
     * Benchmark: string concatenation using the {@code +} operator in a loop.
     *
     * @return the concatenated string
     */
    @Benchmark
    public String stringConcat() {
        String result = "";
        for (int i = 0; i < 100; i++) {
            result += i;
        }
        return result;
    }

    /**
     * Benchmark: string concatenation using {@link StringBuilder}.
     *
     * @return the concatenated string
     */
    @Benchmark
    public String stringBuilder() {
        var sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(i);
        }
        return sb.toString();
    }

    // --- Boxed vs primitive ---

    /**
     * Benchmark: summation using primitive {@code long}.
     *
     * @return the computed sum
     */
    @Benchmark
    public long primitiveSum() {
        long sum = 0;
        for (int i = 0; i < SIZE; i++) {
            sum += i;
        }
        return sum;
    }

    /**
     * Benchmark: summation using boxed {@link Long} (auto-boxing overhead).
     *
     * @return the computed sum
     */
    @Benchmark
    public Long boxedSum() {
        Long sum = 0L;
        for (int i = 0; i < SIZE; i++) {
            sum += i; // auto-boxing on every iteration
        }
        return sum;
    }

    // --- Collection choice ---

    /**
     * Benchmark: iteration over an {@link ArrayList}.
     *
     * @return the sum of all elements
     */
    @Benchmark
    public int arrayListIteration() {
        List<Integer> list = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) list.add(i);
        int sum = 0;
        for (int val : list) sum += val;
        return sum;
    }

    /**
     * Benchmark: iteration over a {@link LinkedList}.
     *
     * @return the sum of all elements
     */
    @Benchmark
    public int linkedListIteration() {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < SIZE; i++) list.add(i);
        int sum = 0;
        for (int val : list) sum += val;
        return sum;
    }

    // --- Stream vs loop ---

    /**
     * Benchmark: summation using {@link IntStream}.
     *
     * @return the computed sum
     */
    @Benchmark
    public long streamSum() {
        return IntStream.range(0, SIZE).asLongStream().sum();
    }

    /**
     * Benchmark: summation using a plain for-loop.
     *
     * @return the computed sum
     */
    @Benchmark
    public long forLoopSum() {
        long sum = 0;
        for (int i = 0; i < SIZE; i++) sum += i;
        return sum;
    }

    /**
     * Runs all JMH benchmarks in this class.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if the benchmark runner fails
     */
    public static void main(String[] args) throws Exception {
        var opts = new OptionsBuilder()
                .include(PerformanceBenchmarks.class.getSimpleName())
                .build();
        new Runner(opts).run();
    }
}
