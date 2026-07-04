# AGENTS.md — `systems` package

Read `examples/systems/AGENTS.md` first — it covers JMH scoping, the JNI build
workflow, and run commands.

- `JniExample` — the **graceful-degradation contract**: the static loader swallows
  `UnsatisfiedLinkError` (warn, don't fail); `safeAdd`/`fallbackAdd` are the safe
  entry points; raw `native` methods may throw at call time. Any new native method
  gets a pure-Java fallback and a `safe*` wrapper. No native source ships in the repo.
- `OffHeapMemoryExample` — direct `ByteBuffer` with native byte order;
  `OffHeapDoubleArray` is `AutoCloseable` with a `java.lang.ref.Cleaner` registration
  (the cleaner action is a documented no-op — it demonstrates the pattern, not real
  `free()`; keep those comments).
- `PerformanceBenchmarks` — JMH `@State` class with explicit
  `@Warmup`/`@Measurement`/`@Fork` and the module's only `main`. Benchmark methods
  must return their result (or use `Blackhole`) so DCE can't fake numbers. JMH is a
  main-scope dependency here — benchmarks live in `src/main` by design.
- `System.out`/`printf` output is allowed in THIS package only (Checkstyle
  suppression + `@SuppressWarnings("PMD.SystemPrintln")`); slf4j is intentionally
  unused in these demos.
