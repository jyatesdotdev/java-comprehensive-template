package com.example.template.simulation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Discrete Event Simulation (DES) engine.
 *
 * <p>Events are scheduled on a priority queue ordered by simulation time.
 * The engine processes events one at a time in chronological order.
 *
 * <h3>Usage — M/M/1 Queue</h3>
 * <pre>{@code
 * var sim = new DiscreteEventSimulation();
 * var queue = new MM1Queue(sim, 1.0, 0.8);
 * queue.scheduleArrival(0.0);
 * sim.runUntil(10_000);
 * queue.printStats();
 * }</pre>
 */
@SuppressWarnings({"PMD.RedundantFieldInitializer", "PMD.UnusedAssignment", "PMD.SystemPrintln", "PMD.UnusedPrivateField", "PMD.UnusedPrivateMethod", "PMD.ImmutableField"}) // Simulation example code
public final class DiscreteEventSimulation {

    /**
     * A scheduled event.
     *
     * @param time   absolute simulation time at which the event fires
     * @param name   descriptive label for logging/debugging
     * @param action callback executed when the event is processed
     */
    public record Event(double time, String name, Runnable action) implements Comparable<Event> {
        @Override
        public int compareTo(Event o) {
            return Double.compare(this.time, o.time);
        }
    }

    private final PriorityQueue<Event> eventQueue = new PriorityQueue<>();
    private double clock = 0.0;
    private long eventsProcessed = 0;

    /** Returns the current simulation clock time. @return current clock value */
    public double clock() {
        return clock;
    }
    /** Returns the total number of events processed so far. @return event count */
    public long eventsProcessed() {
        return eventsProcessed;
    }

    /**
     * Schedule an event at an absolute simulation time.
     *
     * @param time   absolute simulation time (must not be in the past)
     * @param name   descriptive event label
     * @param action callback to execute when the event fires
     * @throws IllegalArgumentException if {@code time} is before the current clock
     */
    public void schedule(double time, String name, Runnable action) {
        if (time < clock) {
            throw new IllegalArgumentException("Cannot schedule in the past");
        }
        eventQueue.add(new Event(time, name, action));
    }

    /**
     * Schedule an event relative to the current clock.
     *
     * @param delay  non-negative delay from the current clock
     * @param name   descriptive event label
     * @param action callback to execute when the event fires
     */
    public void scheduleDelay(double delay, String name, Runnable action) {
        schedule(clock + delay, name, action);
    }

    /**
     * Process events until the clock exceeds {@code endTime} or the queue is empty.
     *
     * @param endTime simulation time at which to stop processing
     */
    public void runUntil(double endTime) {
        while (!eventQueue.isEmpty() && eventQueue.peek().time() <= endTime) {
            Event e = eventQueue.poll();
            clock = e.time();
            e.action().run();
            eventsProcessed++;
        }
    }

    /**
     * Process exactly {@code n} events.
     *
     * @param n maximum number of events to process
     */
    public void runEvents(long n) {
        for (long i = 0; i < n && !eventQueue.isEmpty(); i++) {
            Event e = eventQueue.poll();
            clock = e.time();
            e.action().run();
            eventsProcessed++;
        }
    }

    // ── M/M/1 Queue Example ────────────────────────────────────────────

    /**
     * Single-server queue with Poisson arrivals and exponential service times.
     *
     * <p>Demonstrates the classic M/M/1 queuing model where:
     * <ul>
     *   <li>Arrivals follow a Poisson process (inter-arrival times are exponential)</li>
     *   <li>Service times are exponentially distributed</li>
     *   <li>There is one server</li>
     * </ul>
     */
    public static final class MM1Queue {
        private final DiscreteEventSimulation sim;
        private final double arrivalRate;
        private final double serviceRate;

        // State
        private final Deque<Double> waitingQueue = new ArrayDeque<>();
        private boolean serverBusy = false;

        // Statistics
        private long arrivals;
        private long departures;
        private double totalWaitTime;
        private double totalSystemTime;
        private double busyTime;
        private double lastEventTime = 0.0;
        private int maxQueueLength = 0;

        /**
         * Creates an M/M/1 queue bound to the given simulation engine.
         *
         * @param sim         the simulation engine to schedule events on
         * @param arrivalRate mean arrival rate λ (arrivals per time unit)
         * @param serviceRate mean service rate μ (services per time unit)
         */
        public MM1Queue(DiscreteEventSimulation sim, double arrivalRate, double serviceRate) {
            if (arrivalRate >= serviceRate) {
                System.err.println("Warning: ρ >= 1, queue will grow without bound");
            }
            this.sim = sim;
            this.arrivalRate = arrivalRate;
            this.serviceRate = serviceRate;
        }

        /**
         * Seed the simulation with the first arrival.
         *
         * @param startTime simulation time of the first arrival event
         */
        public void scheduleArrival(double startTime) {
            sim.schedule(startTime, "arrival", this::handleArrival);
        }

        private void handleArrival() {
            arrivals++;
            double now = sim.clock();

            // Schedule next arrival
            sim.scheduleDelay(exponential(arrivalRate), "arrival", this::handleArrival);

            if (serverBusy) {
                waitingQueue.addLast(now);
                maxQueueLength = Math.max(maxQueueLength, waitingQueue.size());
            } else {
                serverBusy = true;
                busyTime -= now; // will add departure time later
                double serviceTime = exponential(serviceRate);
                sim.scheduleDelay(serviceTime, "departure",
                        () -> handleDeparture(now));
            }
        }

        private void handleDeparture(double arrivalTime) {
            departures++;
            double now = sim.clock();
            busyTime += now;
            totalSystemTime += now - arrivalTime;

            if (!waitingQueue.isEmpty()) {
                double waitingSince = waitingQueue.pollFirst();
                totalWaitTime += now - waitingSince;
                busyTime -= now;
                double serviceTime = exponential(serviceRate);
                sim.scheduleDelay(serviceTime, "departure",
                        () -> handleDeparture(waitingSince));
            } else {
                serverBusy = false;
            }
        }

        private static double exponential(double rate) {
            return -Math.log(1.0 - ThreadLocalRandom.current().nextDouble()) / rate;
        }

        /** Theoretical utilization ρ = λ/μ. @return server utilization factor */
        public double rho() {
            return arrivalRate / serviceRate;
        }

        /** Prints queue statistics (arrivals, departures, utilization, wait times) to stdout. */
        public void printStats() {
            double simTime = sim.clock();
            System.out.printf("  Arrivals:          %d%n", arrivals);
            System.out.printf("  Departures:        %d%n", departures);
            System.out.printf("  Server util (ρ):   %.4f  (theoretical: %.4f)%n",
                    busyTime / simTime, rho());
            System.out.printf("  Avg system time:   %.4f  (theoretical: %.4f)%n",
                    departures > 0 ? totalSystemTime / departures : 0,
                    1.0 / (serviceRate - arrivalRate));
            System.out.printf("  Max queue length:  %d%n", maxQueueLength);
        }

        /** Returns total arrival count. @return number of arrivals */
        public long arrivals() {
            return arrivals;
        }
        /** Returns total departure count. @return number of departures */
        public long departures() {
            return departures;
        }
        /** Returns cumulative server busy time. @return busy time in simulation units */
        public double busyTime() {
            return busyTime;
        }
    }

    /**
     * Demo entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println("=== Discrete Event Simulation: M/M/1 Queue ===");
        System.out.println("  λ=0.8, μ=1.0  (ρ=0.8)");

        var sim = new DiscreteEventSimulation();
        var queue = new MM1Queue(sim, 0.8, 1.0);
        queue.scheduleArrival(0.0);
        sim.runUntil(100_000);

        System.out.printf("  Events processed:  %d%n", sim.eventsProcessed());
        queue.printStats();
    }
}
