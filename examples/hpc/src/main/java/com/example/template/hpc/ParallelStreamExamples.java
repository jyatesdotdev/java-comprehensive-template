package com.example.template.hpc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Demonstrates parallel stream patterns and best practices.
 *
 * <p>Key guidelines:
 * <ul>
 *   <li>Use parallel streams for CPU-bound work on large datasets</li>
 *   <li>Avoid parallel streams for I/O-bound or small collections</li>
 *   <li>Use custom ForkJoinPool to isolate parallelism from the common pool</li>
 *   <li>Ensure operations are stateless, non-interfering, and associative</li>
 * </ul>
 */
public final class ParallelStreamExamples {

    private ParallelStreamExamples() {}

    /**
     * Parallel reduction — sum of squares for a large range.
     *
     * @param limit the upper bound of the range (inclusive)
     * @return the sum of squares from 1 to {@code limit}
     */
    public static long sumOfSquares(int limit) {
        return IntStream.rangeClosed(1, limit)
                .parallel()
                .mapToLong(i -> (long) i * i)
                .sum();
    }

    /**
     * Run a parallel stream on a custom ForkJoinPool to avoid starving the common pool.
     * This is critical in server applications where the common pool is shared.
     *
     * @param <T>         the element type
     * @param items       the items to group
     * @param parallelism the parallelism level for the custom pool
     * @return a map grouping items by their simple class name
     * @throws Exception if the parallel computation fails
     */
    public static <T> Map<String, List<T>> groupByClassParallel(
            List<T> items, int parallelism) throws Exception {

        try (var pool = new ForkJoinPool(parallelism)) {
            return pool.submit(() ->
                    items.parallelStream()
                            .collect(Collectors.groupingBy(
                                    item -> item.getClass().getSimpleName()))
            ).get();
        }
    }

    /**
     * Thread-safe collection with parallel stream using {@code groupingByConcurrent}.
     * More efficient than {@code groupingBy} in parallel because it uses a shared
     * ConcurrentMap instead of merging per-thread maps.
     *
     * @param numbers the numbers to group
     * @param modulo  the divisor for grouping
     * @return a concurrent map of remainder to list of numbers
     */
    public static ConcurrentMap<Integer, List<Integer>> groupByModulo(
            List<Integer> numbers, int modulo) {

        return numbers.parallelStream()
                .collect(Collectors.groupingByConcurrent(n -> n % modulo));
    }

    /**
     * Demonstrates ordered vs unordered parallel processing.
     * {@code unordered()} can improve performance by removing ordering constraints.
     *
     * @param count the number of primes to find
     * @return a list of the first {@code count} primes (encounter order preserved)
     */
    public static List<Integer> firstNPrimes(int count) {
        return IntStream.iterate(2, i -> i + 1)
                .parallel()
                .filter(ParallelStreamExamples::isPrime)
                .limit(count)
                .boxed()
                .toList(); // encounter order preserved despite parallel
    }

    /**
     * Unordered variant of {@link #firstNPrimes(int)} — allows {@code limit()} to
     * short-circuit faster at the cost of non-deterministic ordering.
     *
     * @param count the number of primes to find
     * @return a list of {@code count} primes (order not guaranteed)
     */
    public static List<Integer> firstNPrimesUnordered(int count) {
        return IntStream.iterate(2, i -> i + 1)
                .parallel()
                .unordered() // allows limit() to short-circuit faster
                .filter(ParallelStreamExamples::isPrime)
                .limit(count)
                .boxed()
                .toList();
    }

    private static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
