package com.example.template.hpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** Unit tests for {@link VirtualThreadExamples}. */
@DisplayName("VirtualThreadExamples")
class VirtualThreadExamplesTest {

  @Test
  @Timeout(10)
  @DisplayName("startVirtualThread runs the task on a named virtual thread")
  void startVirtualThreadRunsTaskOnNamedVirtualThread() throws InterruptedException {
    var latch = new CountDownLatch(1);
    var ranOnVirtualThread = new AtomicBoolean();
    var threadName = new AtomicReference<String>();

    Thread thread =
        VirtualThreadExamples.startVirtualThread(
            () -> {
              ranOnVirtualThread.set(Thread.currentThread().isVirtual());
              threadName.set(Thread.currentThread().getName());
              latch.countDown();
            });

    assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    assertThat(thread.join(Duration.ofSeconds(5))).isTrue();
    assertThat(thread.isVirtual()).isTrue();
    assertThat(ranOnVirtualThread).isTrue();
    assertThat(threadName.get()).isEqualTo("vt-worker");
  }

  @Test
  @Timeout(10)
  @DisplayName("fetchAllUrls returns one response per URL in input order")
  void fetchAllUrlsReturnsResponsesInOrder() throws Exception {
    List<String> urls = List.of("https://a.example", "https://b.example", "https://c.example");

    List<String> responses = VirtualThreadExamples.fetchAllUrls(urls);

    assertThat(responses)
        .containsExactly(
            "Response from https://a.example",
            "Response from https://b.example",
            "Response from https://c.example");
  }

  @Test
  @Timeout(10)
  @DisplayName("massiveConcurrency completes every spawned task")
  void massiveConcurrencyCompletesAllTasks() throws InterruptedException {
    assertThat(VirtualThreadExamples.massiveConcurrency(1_000)).isEqualTo(1_000L);
  }

  @Test
  @DisplayName("structuredConcurrencyExample is a documented no-op and does not throw")
  void structuredConcurrencyExampleDoesNotThrow() {
    assertThatCode(VirtualThreadExamples::structuredConcurrencyExample).doesNotThrowAnyException();
  }
}
