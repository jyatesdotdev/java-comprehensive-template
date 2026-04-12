# High Performance Computing

Parallel streams, CompletableFuture, virtual threads, and concurrent collections for Java 17+ (virtual threads require Java 21+).

## Contents

| Class | Topics |
|---|---|
| `ParallelStreamExamples` | Parallel reduction, custom ForkJoinPool, `groupingByConcurrent`, ordered vs unordered |
| `CompletableFutureExamples` | Async chaining (`thenCompose`, `thenCombine`), fan-out/fan-in (`allOf`), error handling, timeouts |
| `ConcurrentCollectionsExamples` | `ConcurrentHashMap` (merge, search), `BlockingQueue` producer-consumer, `LongAdder`, `Semaphore`, `StampedLock` optimistic reads |
| `VirtualThreadExamples` | Virtual thread creation, `newVirtualThreadPerTaskExecutor`, massive concurrency, structured concurrency pattern *(Java 21+)* |

## Key Guidelines

### When to use parallel streams
- Large datasets (10k+ elements) with CPU-bound operations
- Stateless, non-interfering, associative operations
- Use a custom `ForkJoinPool` in server apps to avoid starving the common pool

### When to use CompletableFuture
- Composing multiple async I/O operations
- Fan-out/fan-in patterns (call multiple services in parallel)
- When you need fine-grained error handling and timeouts

### When to use virtual threads (Java 21+)
- I/O-bound workloads with high concurrency (HTTP servers, DB queries)
- Thread-per-request architecture
- **Avoid** for CPU-bound work — use parallel streams or `ForkJoinPool` instead

### Concurrent collections cheat sheet
| Need | Use |
|---|---|
| Thread-safe map with atomic updates | `ConcurrentHashMap` |
| Read-heavy, write-rare list | `CopyOnWriteArrayList` |
| Producer-consumer queue | `ArrayBlockingQueue` / `LinkedBlockingQueue` |
| High-contention counter | `LongAdder` (not `AtomicLong`) |
| Rate limiting / resource pooling | `Semaphore` |
| Optimistic read-heavy locking | `StampedLock` |

## How to Run

```bash
# From project root — compile (excludes VirtualThreadExamples on Java 17)
./mvnw -pl examples/hpc compile

# Run tests
./mvnw -pl examples/hpc test

# To include virtual threads (requires Java 21+):
# Update root pom.xml: <java.version>21</java.version>
```

## Performance Tips

1. **Measure first** — use JMH for micro-benchmarks, not `System.nanoTime()`
2. **Avoid shared mutable state** — prefer immutable data and thread-local accumulators
3. **Right-size thread pools** — CPU-bound: `Runtime.getRuntime().availableProcessors()`, I/O-bound: higher
4. **Prefer `LongAdder` over `AtomicLong`** under contention
5. **Use `StampedLock` optimistic reads** for read-dominated workloads

## Related Documentation

- [Main README](../../README.md) — Project overview and quick start
- [Best Practices](../../docs/best-practices.md) — Concurrency best practices
- [Architecture Patterns](../../docs/architecture-patterns.md) — Async patterns
- [Systems Programming](../systems/README.md) — JMH benchmarks, profiling
- [Tutorial](../../docs/TUTORIAL.md) — New developer walkthrough
