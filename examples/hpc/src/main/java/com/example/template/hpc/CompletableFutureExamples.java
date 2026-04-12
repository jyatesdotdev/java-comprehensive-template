package com.example.template.hpc;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Demonstrates {@link CompletableFuture} patterns for async composition.
 *
 * <p>Key patterns covered:
 * <ul>
 *   <li>Async execution with custom executors</li>
 *   <li>Chaining: thenApply, thenCompose, thenCombine</li>
 *   <li>Fan-out/fan-in: allOf, anyOf</li>
 *   <li>Error handling: exceptionally, handle, whenComplete</li>
 *   <li>Timeouts (Java 9+)</li>
 * </ul>
 */
public final class CompletableFutureExamples {

    private CompletableFutureExamples() {}

    /**
     * Chain dependent async operations.
     * fetchUser → fetchOrders → computeTotal
     *
     * @param userId   the user ID to look up
     * @param executor the executor to run async stages on
     * @return a future completing with the user's total order amount
     */
    public static CompletableFuture<Double> computeUserOrderTotal(
            long userId, ExecutorService executor) {

        return CompletableFuture.supplyAsync(() -> fetchUser(userId), executor)
                .thenCompose(user -> CompletableFuture.supplyAsync(
                        () -> fetchOrders(user), executor))
                .thenApply(orders -> orders.stream()
                        .mapToDouble(Double::doubleValue)
                        .sum());
    }

    /**
     * Fan-out: run independent tasks in parallel, combine results.
     *
     * @param executor the executor to run async stages on
     * @return a future completing with the combined dashboard string
     */
    public static CompletableFuture<String> fetchDashboard(ExecutorService executor) {
        var profileFuture = CompletableFuture.supplyAsync(
                () -> "UserProfile", executor);
        var statsFuture = CompletableFuture.supplyAsync(
                () -> "Stats{orders=42}", executor);
        var notificationsFuture = CompletableFuture.supplyAsync(
                () -> "Notifications[3]", executor);

        return CompletableFuture.allOf(profileFuture, statsFuture, notificationsFuture)
                .thenApply(v -> String.join(" | ",
                        profileFuture.join(),
                        statsFuture.join(),
                        notificationsFuture.join()));
    }

    /**
     * Combine two independent futures into a single result.
     *
     * @param executor the executor to run async stages on
     * @return a future completing with a formatted price/stock string
     */
    public static CompletableFuture<String> combineResults(ExecutorService executor) {
        var priceFuture = CompletableFuture.supplyAsync(() -> 99.95, executor);
        var stockFuture = CompletableFuture.supplyAsync(() -> 150, executor);

        return priceFuture.thenCombine(stockFuture,
                (price, stock) -> "Price: $%.2f, Stock: %d".formatted(price, stock));
    }

    /**
     * Error handling with recovery. The exceptionally handler provides a fallback
     * value when the upstream computation fails.
     *
     * @param riskyOp  the supplier that may throw
     * @param fallback the fallback value on failure
     * @param executor the executor to run the operation on
     * @return a future completing with the result or the fallback
     */
    public static CompletableFuture<String> withFallback(
            Supplier<String> riskyOp, String fallback, ExecutorService executor) {

        return CompletableFuture.supplyAsync(riskyOp, executor)
                .exceptionally(ex -> fallback);
    }

    /**
     * Handle both success and failure in one callback.
     *
     * @param <T>       the operation result type
     * @param operation the supplier to execute
     * @param executor  the executor to run the operation on
     * @return a future completing with "OK: {result}" or "ERROR: {message}"
     */
    public static <T> CompletableFuture<String> withHandle(
            Supplier<T> operation, ExecutorService executor) {

        return CompletableFuture.supplyAsync(operation, executor)
                .handle((result, ex) -> {
                    if (ex != null) return "ERROR: " + ex.getMessage();
                    return "OK: " + result;
                });
    }

    /**
     * Timeout pattern — completes exceptionally if not done within the deadline.
     * Available since Java 9.
     *
     * @param <T>       the result type
     * @param operation the supplier to execute
     * @param timeout   the maximum duration to wait
     * @param executor  the executor to run the operation on
     * @return a future that times out with {@link java.util.concurrent.TimeoutException} if not completed in time
     */
    public static <T> CompletableFuture<T> withTimeout(
            Supplier<T> operation, Duration timeout, ExecutorService executor) {

        return CompletableFuture.supplyAsync(operation, executor)
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Collect results from a list of futures (fan-out/fan-in pattern).
     *
     * @param <T>     the element type
     * @param futures the futures to collect
     * @return a future completing with a list of all results
     */
    public static <T> CompletableFuture<List<T>> allAsList(
            List<CompletableFuture<T>> futures) {

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    // --- Simulated service calls ---

    private static String fetchUser(long userId) {
        simulateLatency(50);
        return "User-" + userId;
    }

    private static List<Double> fetchOrders(String user) {
        simulateLatency(80);
        return List.of(29.99, 49.50, 12.00);
    }

    private static void simulateLatency(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
