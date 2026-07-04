# AGENTS.md — `systems` tests

Read `examples/systems/AGENTS.md` first.

- 14 tests. The key discipline: tests must pass **with or without** a built native
  library — assert `safeAdd(a, b) == fallbackAdd(a, b)` unconditionally, and branch on
  `catchThrowable` for the raw-native path (UnsatisfiedLinkError when absent, value
  parity when present). Never require `libnative_example` to exist.
- JMH benchmarks are NOT run in tests — they take minutes and measure nothing useful
  under surefire. Test benchmark *setup* logic only, if any.
- Profiling assertions are sanity bounds (non-negative heap, threadCount ≥ 1,
  Duration within a generous window), not exact values — JVM metrics vary.
- Resource tests prove close-idempotency with try-with-resources +
  `assertThatCode(...).doesNotThrowAnyException()` (PMD's `CloseResource` rejects
  manual double-close constructions).
- Tests print nothing — the `System.out` allowance covers the demo classes, not tests.
