package com.example.template.hpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** Unit tests for {@link ConcurrentCollectionsExamples}. */
@DisplayName("ConcurrentCollectionsExamples")
class ConcurrentCollectionsExamplesTest {

  @Test
  @DisplayName("wordCount counts each word's occurrences")
  void wordCountCountsOccurrences() {
    List<String> words = List.of("alpha", "beta", "alpha", "gamma", "beta");

    ConcurrentHashMap<String, Long> counts = ConcurrentCollectionsExamples.wordCount(words);

    assertThat(counts)
        .containsExactlyInAnyOrderEntriesOf(Map.of("alpha", 2L, "beta", 2L, "gamma", 1L));
  }

  @Test
  @DisplayName("wordCount returns an empty map for an empty input")
  void wordCountReturnsEmptyMapForEmptyInput() {
    assertThat(ConcurrentCollectionsExamples.wordCount(List.of())).isEmpty();
  }

  @Test
  @DisplayName("findHighValue returns the key whose value meets the threshold")
  void findHighValueReturnsMatchingKey() {
    var map = new ConcurrentHashMap<String, Long>();
    map.put("low", 1L);
    map.put("high", 10L);

    assertThat(ConcurrentCollectionsExamples.findHighValue(map, 5L)).isEqualTo("high");
  }

  @Test
  @DisplayName("findHighValue returns null when nothing matches")
  void findHighValueReturnsNullWhenNoMatch() {
    var map = new ConcurrentHashMap<String, Long>();
    map.put("low", 1L);

    assertThat(ConcurrentCollectionsExamples.findHighValue(map, 100L)).isNull();
  }

  @Test
  @Timeout(10)
  @DisplayName("producer/consumer delivers all items in order and stops at the poison pill")
  void producerConsumerDeliversAllItemsInOrder() throws InterruptedException {
    BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);
    List<String> items = List.of("one", "two", "three", "four", "five");

    ConcurrentCollectionsExamples.startProducer(queue, items);
    List<String> consumed = ConcurrentCollectionsExamples.consumeAll(queue);

    assertThat(consumed).containsExactly("one", "two", "three", "four", "five");
    assertThatThrownBy(() -> consumed.add("nope"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @Timeout(10)
  @DisplayName("consumeAll returns an empty list when only the poison pill is queued")
  void consumeAllReturnsEmptyListForPoisonPillOnly() throws InterruptedException {
    BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
    queue.put(ConcurrentCollectionsExamples.POISON_PILL);

    assertThat(ConcurrentCollectionsExamples.consumeAll(queue)).isEmpty();
  }

  @Test
  @Timeout(10)
  @DisplayName("concurrentSum totals all increments across threads")
  void concurrentSumTotalsAllIncrements() throws InterruptedException {
    assertThat(ConcurrentCollectionsExamples.concurrentSum(8, 1_000)).isEqualTo(8_000L);
  }

  // ── RateLimiter ────────────────────────────────────────────────────

  @Test
  @DisplayName("RateLimiter.execute returns the task's result")
  void rateLimiterReturnsTaskResult() throws Exception {
    var limiter = new ConcurrentCollectionsExamples.RateLimiter(1);

    assertThat(limiter.execute(() -> "done")).isEqualTo("done");
  }

  @Test
  @DisplayName("RateLimiter.execute propagates the task's exception and releases the permit")
  void rateLimiterPropagatesTaskException() throws Exception {
    var limiter = new ConcurrentCollectionsExamples.RateLimiter(1);

    assertThatThrownBy(
            () ->
                limiter.execute(
                    () -> {
                      throw new IllegalStateException("task failed");
                    }))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("task failed");

    // Permit was released in the finally block — the limiter is usable again.
    assertThat(limiter.execute(() -> "recovered")).isEqualTo("recovered");
  }

  @Test
  @Timeout(10)
  @DisplayName("RateLimiter allows exactly maxConcurrent tasks inside at once")
  void rateLimiterCapsConcurrentExecutions() throws Exception {
    var limiter = new ConcurrentCollectionsExamples.RateLimiter(2);
    var barrier = new CyclicBarrier(2); // proves two tasks are inside together
    var active = new AtomicInteger();
    var maxActive = new AtomicInteger();

    Callable<Integer> guardedTask =
        () ->
            limiter.execute(
                () -> {
                  int now = active.incrementAndGet();
                  maxActive.accumulateAndGet(now, Math::max);
                  barrier.await(5, TimeUnit.SECONDS);
                  active.decrementAndGet();
                  return now;
                });

    try (ExecutorService pool = Executors.newFixedThreadPool(4)) {
      List<Future<Integer>> futures =
          IntStream.range(0, 4).mapToObj(i -> pool.submit(guardedTask)).toList();
      for (Future<Integer> future : futures) {
        assertThat(future.get(5, TimeUnit.SECONDS)).isBetween(1, 2);
      }
    }

    assertThat(maxActive.get()).isEqualTo(2);
  }

  // ── Point (StampedLock) ────────────────────────────────────────────

  @Test
  @DisplayName("Point starts at the origin")
  void pointStartsAtOrigin() {
    var point = new ConcurrentCollectionsExamples.Point();

    assertThat(point.distanceFromOrigin()).isZero();
  }

  @Test
  @DisplayName("Point.move updates the distance from the origin")
  void pointMoveUpdatesDistance() {
    var point = new ConcurrentCollectionsExamples.Point();

    point.move(3.0, 4.0);

    assertThat(point.distanceFromOrigin()).isEqualTo(5.0);
  }

  @Test
  @DisplayName("Point.move accumulates displacements")
  void pointMovesAccumulate() {
    var point = new ConcurrentCollectionsExamples.Point();

    point.move(1.0, 2.0);
    point.move(2.0, 1.0);

    assertThat(point.distanceFromOrigin()).isEqualTo(Math.sqrt(18.0));
  }
}
