package com.example.template.hpc;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * Virtual threads (Project Loom) — requires Java 21+.
 *
 * <p><strong>NOTE:</strong> This file will not compile on Java 17. It is included as a
 * reference for projects targeting Java 21+. To enable, update the root POM's
 * {@code java.version} property to 21.
 *
 * <p>Virtual threads are lightweight threads managed by the JVM, ideal for I/O-bound
 * workloads. They allow millions of concurrent tasks without the overhead of platform threads.
 *
 * <h3>When to use virtual threads</h3>
 * <ul>
 *   <li>I/O-bound tasks: HTTP calls, database queries, file I/O</li>
 *   <li>High-concurrency servers (thread-per-request model)</li>
 * </ul>
 *
 * <h3>When NOT to use virtual threads</h3>
 * <ul>
 *   <li>CPU-bound computation — use parallel streams or ForkJoinPool instead</li>
 *   <li>Tasks holding native resources or using {@code synchronized} heavily (pinning)</li>
 * </ul>
 */
public final class VirtualThreadExamples {

    private VirtualThreadExamples() {}

    /**
     * Basic virtual thread creation.
     *
     * @param task the runnable to execute on a virtual thread
     * @return the started virtual thread
     */
    public static Thread startVirtualThread(Runnable task) {
        return Thread.ofVirtual()
                .name("vt-worker")
                .start(task);
    }

    /**
     * ExecutorService backed by virtual threads — the recommended approach for
     * structured concurrency in server applications.
     *
     * <p>Each submitted task runs on its own virtual thread. The executor creates
     * a new virtual thread per task (no pooling needed — they're cheap).
     *
     * @param urls the URLs to fetch concurrently
     * @return a list of response strings, one per URL
     * @throws Exception if any fetch fails or the executor cannot complete
     */
    public static List<String> fetchAllUrls(List<String> urls) throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = urls.stream()
                    .map(url -> executor.submit(() -> simulateHttpGet(url)))
                    .toList();

            // Collect results — blocks on each future but the virtual threads
            // unmount from carrier threads during I/O waits
            return futures.stream()
                    .map(VirtualThreadExamples::getUnchecked)
                    .toList();
        }
    }

    /**
     * Demonstrates massive concurrency — 100k virtual threads running concurrently.
     * This would be impractical with platform threads.
     *
     * @param taskCount the number of virtual threads to spawn
     * @return the total number of completed tasks
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public static long massiveConcurrency(int taskCount) throws InterruptedException {
        var counter = new java.util.concurrent.atomic.LongAdder();
        var latch = new CountDownLatch(taskCount);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, taskCount).forEach(i ->
                    executor.submit(() -> {
                        simulateIo();
                        counter.increment();
                        latch.countDown();
                    }));
            latch.await();
        }
        return counter.sum();
    }

    /**
     * Structured concurrency (Java 21 preview via {@code StructuredTaskScope}).
     *
     * <p>This is pseudo-code showing the pattern. Actual usage requires:
     * {@code --enable-preview} and import of {@code java.util.concurrent.StructuredTaskScope}.
     *
     * <pre>{@code
     * try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
     *     Subtask<String> user = scope.fork(() -> fetchUser(id));
     *     Subtask<List<Order>> orders = scope.fork(() -> fetchOrders(id));
     *     scope.join().throwIfFailed();
     *     return new UserDashboard(user.get(), orders.get());
     * }
     * }</pre>
     */
    public static void structuredConcurrencyExample() {
        // See Javadoc above for the pattern.
        // StructuredTaskScope is a preview API in Java 21.
    }

    // --- Helpers ---

    private static String simulateHttpGet(String url) {
        simulateIo();
        return "Response from " + url;
    }

    private static void simulateIo() {
        try { Thread.sleep(10); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static <T> T getUnchecked(Future<T> future) {
        try { return future.get(); } catch (Exception e) {
            throw new CompletionException(e);
        }
    }
}
