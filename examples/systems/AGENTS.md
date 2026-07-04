# AGENTS.md — `examples/systems/`

Read the root and `examples/AGENTS.md` first. Low-level Java: JNI interop, off-heap
memory, JMH microbenchmarks, and JVM profiling.

## Layout

| Class | Shows |
|-------|-------|
| `JniExample` | `native` methods with a **graceful-degradation pattern**: static `System.loadLibrary("native_example")` catches `UnsatisfiedLinkError` and warns instead of failing; `safeAdd` tries native and falls back to `fallbackAdd` (pure Java) |
| `OffHeapMemoryExample` | direct `ByteBuffer` with native byte order; nested `OffHeapDoubleArray implements AutoCloseable` using `java.lang.ref.Cleaner` (Java 9+ replacement for finalizers); memory-mapped files shown in Javadoc only |
| `PerformanceBenchmarks` | JMH `@State(Scope.Thread)` class — 8 `@Benchmark` methods (string concat vs builder, boxing, ArrayList vs LinkedList, streams vs loops); **the only runnable `main` in the hpc/etl/systems trio** |
| `ProfilingUtils` | MXBean JVM snapshots (`record JvmSnapshot`), `timeExecution`, `profileTask` with GC delta |

## JMH specifics — different from everywhere else

- `jmh-core` is a **main-scope** dependency (benchmarks live in `src/main`);
  `jmh-generator-annprocess` is `provided` (annotation processor only, not packaged).
- The processor generates harness code under `target/generated-*sources`; PMD,
  Checkstyle, and SpotBugs already exclude `jmh_generated` via the shared `config/`
  files — don't re-add per-module exclusions.
- Benchmark hygiene: return values from `@Benchmark` methods (or use `Blackhole`) so
  dead-code elimination doesn't fake your numbers; keep `@Warmup`/`@Measurement`/`@Fork`
  annotations explicit.

## Commands

```bash
./mvnw -pl examples/systems compile
./mvnw -pl examples/systems test
./mvnw -pl examples/systems compile exec:java \
  -Dexec.mainClass=com.example.template.systems.PerformanceBenchmarks   # run benchmarks (minutes)
```

exec-maven-plugin is pinned in the parent's `pluginManagement`
(`exec-maven-plugin.version`), so `exec:java` resolves a reproducible version.

## Gotchas

- **No native library ships with the repo.** `nativeAdd`/`nativeGreet` throw
  `UnsatisfiedLinkError` unless you compile the C library yourself (README has the
  `javac -h` / `gcc -shared -fPIC` workflow). Always call `safeAdd`/`fallbackAdd` from
  code that must work everywhere. `JniExample` has no `main`.
- `System.out`/`printf` output is **allowed in this package** via a Checkstyle
  suppression (`.*systems[/\\].*`) plus `@SuppressWarnings("PMD.SystemPrintln")` —
  that allowance is for demo/benchmark output only and does not extend to other modules.
- `OffHeapDoubleArray`'s Cleaner action is deliberately a no-op (direct buffers free
  themselves); it demonstrates the registration pattern, not real native `free()`. The
  code comments say so — keep them.
- Tests (14): `JniExampleTest` (fallback/safeAdd semantics that pass with or without a
  built native lib), `ProfilingUtilsTest` (snapshot sanity, task-run-once), and
  `OffHeapMemoryExampleTest` (round-trip, bounds, close idempotency). JMH benchmarks
  are not run in tests. `jacoco.skip=true`.
- `slf4j-api`/`logback` are dependencies but unused in source — demo output is
  intentionally stdout.
