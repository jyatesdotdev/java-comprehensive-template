package com.example.template.hpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CompletableFutureExamples}. */
@DisplayName("CompletableFutureExamples")
class CompletableFutureExamplesTest {

  private static ExecutorService executor;

  @BeforeAll
  static void startExecutor() {
    executor = Executors.newVirtualThreadPerTaskExecutor();
  }

  @AfterAll
  static void stopExecutor() {
    executor.shutdownNow();
  }

  @Test
  @DisplayName("computeUserOrderTotal sums the user's orders")
  void computeUserOrderTotalSumsOrders() throws Exception {
    CompletableFuture<Double> future =
        CompletableFutureExamples.computeUserOrderTotal(42L, executor);

    double total = future.get(5, TimeUnit.SECONDS);

    assertThat(total).isCloseTo(91.49, within(1e-9));
  }

  @Test
  @DisplayName("fetchDashboard joins all three sections")
  void fetchDashboardCombinesAllSections() {
    CompletableFuture<String> future = CompletableFutureExamples.fetchDashboard(executor);

    assertThat(future)
        .succeedsWithin(Duration.ofSeconds(5))
        .isEqualTo("UserProfile | Stats{orders=42} | Notifications[3]");
  }

  @Test
  @DisplayName("combineResults merges price and stock into one string")
  void combineResultsMergesPriceAndStock() {
    CompletableFuture<String> future = CompletableFutureExamples.combineResults(executor);

    // Locale.ROOT formatting keeps the output identical on every machine.
    assertThat(future).succeedsWithin(Duration.ofSeconds(5)).isEqualTo("Price: $99.95, Stock: 150");
  }

  @Test
  @DisplayName("withFallback returns the primary value on success")
  void withFallbackReturnsPrimaryValueOnSuccess() {
    CompletableFuture<String> future =
        CompletableFutureExamples.withFallback(() -> "primary", "fallback", executor);

    assertThat(future).succeedsWithin(Duration.ofSeconds(5)).isEqualTo("primary");
  }

  @Test
  @DisplayName("withFallback returns the fallback when the operation throws")
  void withFallbackReturnsFallbackOnFailure() {
    CompletableFuture<String> future =
        CompletableFutureExamples.withFallback(
            () -> {
              throw new IllegalStateException("service down");
            },
            "fallback",
            executor);

    assertThat(future).succeedsWithin(Duration.ofSeconds(5)).isEqualTo("fallback");
  }

  @Test
  @DisplayName("withHandle wraps a successful result as OK")
  void withHandleWrapsSuccessAsOk() {
    CompletableFuture<String> future = CompletableFutureExamples.withHandle(() -> 42, executor);

    assertThat(future).succeedsWithin(Duration.ofSeconds(5)).isEqualTo("OK: 42");
  }

  @Test
  @DisplayName("withHandle converts a failure into an ERROR message")
  void withHandleConvertsFailureToErrorMessage() throws Exception {
    CompletableFuture<String> future =
        CompletableFutureExamples.withHandle(
            () -> {
              throw new IllegalStateException("boom");
            },
            executor);

    String result = future.get(5, TimeUnit.SECONDS);

    assertThat(result).startsWith("ERROR:").contains("boom");
  }

  @Test
  @DisplayName("withTimeout completes normally when the operation is fast enough")
  void withTimeoutCompletesWhenOperationIsFast() {
    CompletableFuture<String> future =
        CompletableFutureExamples.withTimeout(() -> "fast", Duration.ofSeconds(5), executor);

    assertThat(future).succeedsWithin(Duration.ofSeconds(5)).isEqualTo("fast");
  }

  @Test
  @DisplayName("withTimeout fails with TimeoutException when the operation never completes")
  void withTimeoutFailsWhenOperationHangs() {
    CountDownLatch release = new CountDownLatch(1);
    try {
      CompletableFuture<String> future =
          CompletableFutureExamples.withTimeout(
              () -> awaitThenReturn(release), Duration.ofMillis(50), executor);

      assertThat(future)
          .failsWithin(Duration.ofSeconds(5))
          .withThrowableOfType(ExecutionException.class)
          .withCauseInstanceOf(TimeoutException.class);
    } finally {
      release.countDown(); // unblock the still-running supplier
    }
  }

  @Test
  @DisplayName("allAsList preserves the order of the input futures")
  void allAsListCollectsResultsInOrder() {
    List<CompletableFuture<String>> futures =
        List.of(
            CompletableFuture.completedFuture("first"),
            CompletableFuture.supplyAsync(() -> "second", executor),
            CompletableFuture.completedFuture("third"));

    CompletableFuture<List<String>> all = CompletableFutureExamples.allAsList(futures);

    assertThat(all)
        .succeedsWithin(Duration.ofSeconds(5))
        .isEqualTo(List.of("first", "second", "third"));
  }

  @Test
  @DisplayName("allAsList propagates the failure of any input future")
  void allAsListPropagatesFailure() {
    List<CompletableFuture<String>> futures =
        List.of(
            CompletableFuture.completedFuture("ok"),
            CompletableFuture.failedFuture(new IllegalStateException("kaboom")));

    CompletableFuture<List<String>> all = CompletableFutureExamples.allAsList(futures);

    assertThatThrownBy(all::join)
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(IllegalStateException.class)
        .hasRootCauseMessage("kaboom");
  }

  private static String awaitThenReturn(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return "never observed";
  }
}
