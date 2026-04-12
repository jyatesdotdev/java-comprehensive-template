package com.example.template.systems;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Programmatic profiling utilities using JMX MXBeans.
 *
 * <p>For production profiling, prefer external tools:
 * <ul>
 *   <li><b>JDK Flight Recorder (JFR)</b>: {@code -XX:StartFlightRecording=filename=rec.jfr,duration=60s}</li>
 *   <li><b>async-profiler</b>: {@code ./asprof -d 30 -f profile.html <pid>}</li>
 *   <li><b>VisualVM / JConsole</b>: GUI-based JMX monitoring</li>
 * </ul>
 *
 * <p>Useful JVM flags for diagnostics:
 * <pre>{@code
 * -XX:+PrintGCDetails -XX:+PrintGCDateStamps   # GC logging (pre-Java 9)
 * -Xlog:gc*:file=gc.log                        # Unified logging (Java 9+)
 * -XX:NativeMemoryTracking=summary              # Track native memory
 * -XX:+HeapDumpOnOutOfMemoryError               # Auto heap dump on OOM
 * }</pre>
 */
@SuppressWarnings("PMD.SystemPrintln") // Profiling output
public final class ProfilingUtils {

    private ProfilingUtils() {}

    /**
     * Snapshot of JVM memory and GC state.
     *
     * @param heapUsedMb    current heap usage in megabytes
     * @param heapMaxMb     maximum heap size in megabytes
     * @param nonHeapUsedMb current non-heap (metaspace, code cache) usage in megabytes
     * @param threadCount   number of live threads
     * @param totalGcCount  cumulative GC collection count across all collectors
     * @param totalGcTimeMs cumulative GC time in milliseconds across all collectors
     */
    public record JvmSnapshot(
            long heapUsedMb,
            long heapMaxMb,
            long nonHeapUsedMb,
            int threadCount,
            long totalGcCount,
            long totalGcTimeMs
    ) {
        @Override
        public String toString() {
            return String.format(
                    "Heap: %dMB/%dMB | Non-heap: %dMB | Threads: %d | GC: %d collections, %dms",
                    heapUsedMb, heapMaxMb, nonHeapUsedMb, threadCount, totalGcCount, totalGcTimeMs);
        }
    }

    /**
     * Captures current JVM state via MXBeans.
     *
     * @return a snapshot of heap, non-heap, thread, and GC metrics
     */
    public static JvmSnapshot snapshot() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();

        long gcCount = gcs.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
        long gcTime = gcs.stream().mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();

        return new JvmSnapshot(
                mem.getHeapMemoryUsage().getUsed() / (1024 * 1024),
                mem.getHeapMemoryUsage().getMax() / (1024 * 1024),
                mem.getNonHeapMemoryUsage().getUsed() / (1024 * 1024),
                threads.getThreadCount(),
                gcCount,
                gcTime
        );
    }

    /**
     * Times a {@link Runnable} and returns elapsed duration.
     *
     * @param task the task to execute and measure
     * @return wall-clock duration of the task execution
     */
    public static Duration timeExecution(Runnable task) {
        Instant start = Instant.now();
        task.run();
        return Duration.between(start, Instant.now());
    }

    /**
     * Prints before/after JVM snapshots around a task.
     *
     * @param label descriptive name printed in the output header
     * @param task  the task to profile
     */
    public static void profileTask(String label, Runnable task) {
        JvmSnapshot before = snapshot();
        Duration elapsed = timeExecution(task);
        JvmSnapshot after = snapshot();

        System.out.printf("=== %s ===%n", label);
        System.out.printf("  Before: %s%n", before);
        System.out.printf("  After:  %s%n", after);
        System.out.printf("  Elapsed: %dms%n", elapsed.toMillis());
        System.out.printf("  GC delta: %d collections, %dms%n",
                after.totalGcCount() - before.totalGcCount(),
                after.totalGcTimeMs() - before.totalGcTimeMs());
    }
}
