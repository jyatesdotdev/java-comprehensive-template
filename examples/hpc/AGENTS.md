# AGENTS.md — `examples/hpc/`

Read the root and `examples/AGENTS.md` first. Reference module for concurrency:
parallel streams, `CompletableFuture` composition, concurrent collections, and
virtual threads.

## Layout — four final utility classes, static methods, no main

| Class | Shows |
|-------|-------|
| `ParallelStreamExamples` | custom `ForkJoinPool` (try-with-resources + `pool.submit(...).get()`), `groupingByConcurrent`, ordered vs `unordered()` streams |
| `CompletableFutureExamples` | `thenCompose`/`thenApply` chains, `allOf` fan-out/fan-in, `thenCombine`, `exceptionally`, `handle`, `orTimeout` |
| `ConcurrentCollectionsExamples` | `ConcurrentHashMap.merge`/`search`, `BlockingQueue` producer-consumer with a poison pill, `LongAdder` + `CountDownLatch`, nested `RateLimiter` (Semaphore) and `Point` (`StampedLock` optimistic read) |
| `VirtualThreadExamples` | `Thread.ofVirtual()`, `Executors.newVirtualThreadPerTaskExecutor()`, 100k-thread demo; `StructuredTaskScope` shown only as Javadoc pseudo-code (preview API — do not compile it in) |

These are library-style methods meant to be called from tests or copied — nothing here
runs standalone.

## Conventions this module encodes

- **Always restore the interrupt flag**: `catch (InterruptedException e) {
  Thread.currentThread().interrupt(); ... }` — every blocking call here follows it; so
  must your additions.
- Checked exceptions from async work are surfaced as `CompletionException`; don't
  swallow them in `exceptionally`.
- No SLF4J here on purpose — pure library code returns values instead of logging.
- Class-level `@SuppressWarnings("PMD....") // Example code` annotations are narrow
  and justified; follow the pattern rather than widening config rules.

## Java-version guard

The pom has a `java17` profile (JDK activation range `[17,21)`) that excludes
`VirtualThreadExamples.java` from compilation. On the default toolchain (Java 21) the
profile is **inactive** and the class compiles normally. Keep the guard if you add more
21-only APIs to that class; anything using Java 21 APIs outside it needs its own
exclusion or a note.

## Commands

```bash
./mvnw -pl examples/hpc compile
./mvnw -pl examples/hpc test     # 36 tests, one test class per example class
```

## Gotchas

- Tests (36) are fully deterministic: futures joined with explicit timeouts
  (`get(5, SECONDS)`, AssertJ `succeedsWithin`/`failsWithin`), `CountDownLatch`/
  `CyclicBarrier` coordination, `@Timeout` guards — **never** sleep-based assertions.
  Match that style for anything you add.
- This module intentionally has **no dependencies beyond the parent** (JMH benchmarks
  live in `examples/systems/`, not here). Keep it JDK-only.
- User-facing strings formatted with `%f`/`%d` use `String.format(Locale.ROOT, ...)`
  so output is machine-independent — follow that precedent (see commit history: this
  repo has fixed default-locale formatting bugs more than once).
- `jacoco.skip=true` — no coverage gate.
