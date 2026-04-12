package com.example.template.hpc;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.StampedLock;

/**
 * Demonstrates concurrent collections and threading primitives.
 *
 * <p>Covers:
 * <ul>
 *   <li>{@link ConcurrentHashMap} — compute, merge, search, reduce</li>
 *   <li>{@link CopyOnWriteArrayList} — read-heavy, write-rare scenarios</li>
 *   <li>{@link BlockingQueue} — producer-consumer pattern</li>
 *   <li>Atomic variables and accumulators</li>
 *   <li>Synchronization primitives: CountDownLatch, Semaphore, Phaser</li>
 *   <li>Lock strategies: ReentrantReadWriteLock, StampedLock</li>
 * </ul>
 */
@SuppressWarnings("PMD.SignatureDeclareThrowsException") // Example code
public final class ConcurrentCollectionsExamples {

    private ConcurrentCollectionsExamples() { }

    // ── ConcurrentHashMap ──────────────────────────────────────────────

    /**
     * Atomic word-count using ConcurrentHashMap.merge.
     *
     * @param words the words to count
     * @return map of word to occurrence count
     */
    public static ConcurrentHashMap<String, Long> wordCount(List<String> words) {
        var counts = new ConcurrentHashMap<String, Long>();
        words.parallelStream()
                .forEach(w -> counts.merge(w, 1L, Long::sum));
        return counts;
    }

    /**
     * Bulk search: find first entry matching a condition (parallelism threshold = 1).
     *
     * @param map       the concurrent map to search
     * @param threshold minimum value to match
     * @return the first key whose value meets the threshold, or {@code null} if none
     */
    public static String findHighValue(ConcurrentHashMap<String, Long> map, long threshold) {
        return map.search(1, (key, value) -> value >= threshold ? key : null);
    }

    // ── Producer-Consumer with BlockingQueue ───────────────────────────

    /**
     * Simple producer-consumer using a bounded {@link ArrayBlockingQueue}.
     * The poison pill pattern signals the consumer to stop.
     */
    public static final String POISON_PILL = "__DONE__";

    /**
     * Starts a producer thread that enqueues items followed by a poison pill.
     *
     * @param queue the blocking queue to produce into
     * @param items the items to enqueue
     */
    public static void startProducer(BlockingQueue<String> queue, List<String> items) {
        Thread.ofPlatform().name("producer").start(() -> {
            try {
                for (String item : items) {
                    queue.put(item);
                }
                queue.put(POISON_PILL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Consumes all items from the queue until the poison pill is received.
     *
     * @param queue the blocking queue to consume from
     * @return an unmodifiable list of consumed items
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public static List<String> consumeAll(BlockingQueue<String> queue) throws InterruptedException {
        var results = new CopyOnWriteArrayList<String>();
        while (true) {
            String item = queue.take();
            if (POISON_PILL.equals(item)) {
                break;
            }
            results.add(item);
        }
        return List.copyOf(results);
    }

    // ── Atomic Accumulators ────────────────────────────────────────────

    /**
     * {@link LongAdder} outperforms {@link AtomicLong} under high contention
     * because it spreads updates across cells and sums lazily.
     *
     * @param threadCount         number of threads to spawn
     * @param incrementsPerThread number of increments each thread performs
     * @return the total sum after all threads complete
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public static long concurrentSum(int threadCount, int incrementsPerThread)
            throws InterruptedException {

        var adder = new LongAdder();
        var latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread.ofPlatform().start(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    adder.increment();
                }
                latch.countDown();
            });
        }
        latch.await();
        return adder.sum();
    }

    // ── Semaphore — rate limiting / resource pooling ───────────────────

    /**
     * Limits concurrent access to a resource (e.g., connection pool, API rate limit).
     */
    public static class RateLimiter {
        private final Semaphore semaphore;

        /**
         * Creates a rate limiter with the specified concurrency limit.
         *
         * @param maxConcurrent maximum number of concurrent executions
         */
        public RateLimiter(int maxConcurrent) {
            this.semaphore = new Semaphore(maxConcurrent);
        }

        /**
         * Executes the task after acquiring a permit.
         *
         * @param <T>  the result type
         * @param task the callable to execute
         * @return the result of the callable
         * @throws Exception if the task throws or the thread is interrupted
         */
        public <T> T execute(Callable<T> task) throws Exception {
            semaphore.acquire();
            try {
                return task.call();
            } finally {
                semaphore.release();
            }
        }
    }

    // ── StampedLock — optimistic reads ─────────────────────────────────

    /**
     * A point class demonstrating {@link StampedLock} optimistic read pattern.
     * Optimistic reads avoid acquiring a lock, falling back to a read lock
     * only if the data was modified during the read.
     */
    public static class Point {
        private double x;
        private double y;
        private final StampedLock lock = new StampedLock();

        /**
         * Moves the point by the given deltas under a write lock.
         *
         * @param deltaX the x-axis displacement
         * @param deltaY the y-axis displacement
         */
        public void move(double deltaX, double deltaY) {
            long stamp = lock.writeLock();
            try {
                x += deltaX;
                y += deltaY;
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        /**
         * Computes the distance from the origin using an optimistic read.
         *
         * @return the Euclidean distance from (0, 0)
         */
        public double distanceFromOrigin() {
            long stamp = lock.tryOptimisticRead();
            double currentX = x;
            double currentY = y;
            if (!lock.validate(stamp)) {
                // Fallback to read lock
                stamp = lock.readLock();
                try {
                    currentX = x;
                    currentY = y;
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            return Math.sqrt(currentX * currentX + currentY * currentY);
        }
    }
}
