package com.example.template.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;

import com.example.template.simulation.DiscreteEventSimulation.MM1Queue;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link DiscreteEventSimulation} engine (deterministic) and the nested {@link
 * MM1Queue} example (statistical, with tolerances wide enough that flakes are effectively
 * impossible).
 */
@DisplayName("DiscreteEventSimulation")
class DiscreteEventSimulationTest {

  private static final String EVT = "evt";
  private static final Runnable NO_OP = () -> {};

  // ── Engine: ordering and clock ──────────────────────────────────────

  @Test
  @DisplayName("events execute in time order regardless of scheduling order")
  void eventsExecuteInTimeOrder() {
    var sim = new DiscreteEventSimulation();
    List<String> fired = new ArrayList<>();
    sim.schedule(5.0, EVT, () -> fired.add("t5"));
    sim.schedule(1.0, EVT, () -> fired.add("t1"));
    sim.schedule(3.0, EVT, () -> fired.add("t3"));

    sim.runUntil(10.0);

    assertThat(fired).containsExactly("t1", "t3", "t5");
    assertThat(sim.clock()).isEqualTo(5.0);
    assertThat(sim.eventsProcessed()).isEqualTo(3);
  }

  @Test
  @DisplayName("scheduling in the past throws IllegalArgumentException")
  void scheduleInPastThrows() {
    var sim = new DiscreteEventSimulation();
    sim.schedule(5.0, EVT, NO_OP);
    sim.runUntil(5.0); // advance the clock to 5.0

    assertThatThrownBy(() -> sim.schedule(4.9, EVT, NO_OP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("past");
    assertThatThrownBy(() -> sim.schedule(-0.001, EVT, NO_OP))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("scheduling exactly at the current clock is allowed")
  void scheduleAtCurrentClockAllowed() {
    var sim = new DiscreteEventSimulation();
    sim.schedule(5.0, EVT, NO_OP);
    sim.runUntil(5.0);

    assertThatNoException().isThrownBy(() -> sim.schedule(5.0, EVT, NO_OP));
    sim.runUntil(5.0);
    assertThat(sim.eventsProcessed()).isEqualTo(2);
  }

  @Test
  @DisplayName("runUntil advances the clock and stops at endTime")
  void runUntilStopsAtEndTime() {
    var sim = new DiscreteEventSimulation();
    sim.schedule(1.0, EVT, NO_OP);
    sim.schedule(2.0, EVT, NO_OP);
    sim.schedule(3.0, EVT, NO_OP);

    sim.runUntil(2.5);

    // Only the events at t=1 and t=2 fire; the clock rests on the last processed event.
    assertThat(sim.eventsProcessed()).isEqualTo(2);
    assertThat(sim.clock()).isEqualTo(2.0);

    sim.runUntil(3.0);
    assertThat(sim.eventsProcessed()).isEqualTo(3);
    assertThat(sim.clock()).isEqualTo(3.0);
  }

  @Test
  @DisplayName("runUntil on an empty queue leaves the clock unchanged")
  void runUntilEmptyQueue() {
    var sim = new DiscreteEventSimulation();

    sim.runUntil(100.0);

    assertThat(sim.clock()).isEqualTo(0.0);
    assertThat(sim.eventsProcessed()).isZero();
  }

  @Test
  @DisplayName("runEvents processes exactly n events")
  void runEventsProcessesExactlyN() {
    var sim = new DiscreteEventSimulation();
    for (int i = 1; i <= 5; i++) {
      sim.schedule(i, EVT, NO_OP);
    }

    sim.runEvents(3);

    assertThat(sim.eventsProcessed()).isEqualTo(3);
    assertThat(sim.clock()).isEqualTo(3.0);

    sim.runEvents(10); // only 2 remain
    assertThat(sim.eventsProcessed()).isEqualTo(5);
    assertThat(sim.clock()).isEqualTo(5.0);
  }

  @Test
  @DisplayName("scheduleDelay offsets from the current clock, not from schedule-call time")
  void scheduleDelayOffsetsFromCurrentClock() {
    var sim = new DiscreteEventSimulation();
    double[] firedAt = {-1.0};
    // At t=10 an event schedules a follow-up 2.5 time units later -> fires at 12.5.
    sim.schedule(10.0, EVT, () -> sim.scheduleDelay(2.5, EVT, () -> firedAt[0] = sim.clock()));

    sim.runUntil(20.0);

    assertThat(firedAt[0]).isEqualTo(12.5);
    assertThat(sim.clock()).isEqualTo(12.5);
  }

  // ── MM1Queue: statistical checks on a stable configuration ──────────

  @Test
  @DisplayName("stable M/M/1 (rho=0.8) has consistent stats and utilization near rho")
  void mm1StableQueueStatistics() {
    var sim = new DiscreteEventSimulation();
    var queue = new MM1Queue(sim, 0.8, 1.0);
    queue.scheduleArrival(0.0);

    sim.runUntil(50_000.0);

    assertThat(queue.rho()).isCloseTo(0.8, offset(1e-12));

    // Self-consistent counters: ~40k arrivals expected; every processed event is
    // exactly one arrival or one departure.
    assertThat(queue.arrivals()).isGreaterThan(10_000);
    assertThat(queue.departures()).isPositive();
    assertThat(queue.arrivals()).isGreaterThanOrEqualTo(queue.departures());
    assertThat(sim.eventsProcessed()).isEqualTo(queue.arrivals() + queue.departures());

    // busyTime() closes any in-progress service interval at the current clock, so
    // utilization is directly comparable to rho. It fluctuates around 0.8 with
    // stddev ~0.01 at T=50k, so a [0.6, 0.95] band is >10 sigma on both sides.
    double utilization = queue.busyTime() / sim.clock();
    assertThat(utilization).isBetween(0.6, 0.95);
  }
}
