# AGENTS.md — `hpc` package

Read `examples/hpc/AGENTS.md` first — it maps all four classes and the Java-version guard.

- All classes are `final` with private constructors and **static** methods — pure
  library demos returning values, no `main`, no SLF4J (callers decide about logging),
  no `System.out`.
- Non-negotiable concurrency conventions demonstrated here and required of additions:
  restore the interrupt flag on `InterruptedException`
  (`Thread.currentThread().interrupt()`); wrap checked async failures in
  `CompletionException`; custom `ForkJoinPool`s in try-with-resources; shared state
  only via concurrent types (`ConcurrentHashMap`, `LongAdder`, `Semaphore`,
  `StampedLock`).
- Locale-sensitive formatting uses `String.format(Locale.ROOT, ...)`.
- Java-21-only APIs stay inside `VirtualThreadExamples` (the pom's `java17` profile
  excludes that one file on older JDKs); `StructuredTaskScope` remains javadoc-only
  until it leaves preview.
- Every public method has a deterministic test in `src/test/.../hpc/` — futures joined
  with timeouts, latch/barrier coordination, never sleep-based assertions.
