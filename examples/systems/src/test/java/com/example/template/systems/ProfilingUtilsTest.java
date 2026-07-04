package com.example.template.systems;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProfilingUtils} JVM snapshots and timing helpers. */
class ProfilingUtilsTest {

  @Test
  void snapshot_returnsSaneJvmMetrics() {
    ProfilingUtils.JvmSnapshot snap = ProfilingUtils.snapshot();

    assertThat(snap.heapUsedMb()).isNotNegative();
    assertThat(snap.nonHeapUsedMb()).isNotNegative();
    assertThat(snap.threadCount()).isGreaterThanOrEqualTo(1);
  }

  @Test
  void snapshot_toStringContainsKeyMetrics() {
    String text = ProfilingUtils.snapshot().toString();

    assertThat(text).contains("Heap:").contains("Threads:").contains("GC:");
  }

  @Test
  void timeExecution_runsTaskAndReturnsPlausibleDuration() {
    AtomicInteger runs = new AtomicInteger();

    Duration elapsed = ProfilingUtils.timeExecution(runs::incrementAndGet);

    assertThat(runs).hasValue(1);
    assertThat(elapsed).isGreaterThanOrEqualTo(Duration.ZERO).isLessThan(Duration.ofMinutes(1));
  }

  @Test
  void profileTask_runsTaskExactlyOnce() {
    AtomicInteger runs = new AtomicInteger();

    ProfilingUtils.profileTask("unit-test-task", runs::incrementAndGet);

    assertThat(runs).hasValue(1);
  }
}
