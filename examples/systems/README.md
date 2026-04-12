# Systems Programming

Low-level Java programming: JNI, off-heap memory, performance optimization, and profiling.

## Contents

| Class | Topic |
|---|---|
| `JniExample` | JNI native method declarations, library loading, Java fallback pattern |
| `OffHeapMemoryExample` | Direct ByteBuffer, off-heap arrays with Cleaner, memory-mapped files |
| `PerformanceBenchmarks` | JMH benchmarks: string concat, boxed vs primitive, collections, streams |
| `ProfilingUtils` | JVM snapshots via MXBeans, execution timing, GC monitoring |

## JNI Workflow

JNI lets Java call native C/C++ code. The workflow:

```
1. Declare native methods in Java
2. Generate C header:  javac -h native/include src/.../JniExample.java
3. Implement C functions matching generated signatures
4. Compile shared library:
     Linux:  gcc -shared -fPIC -o libnative_example.so -I$JAVA_HOME/include -I$JAVA_HOME/include/linux native.c
     macOS:  gcc -shared -o libnative_example.dylib -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin native.c
5. Run with: java -Djava.library.path=./native com.example.template.systems.JniExample
```

The `JniExample` class also demonstrates a **fallback pattern** — try native, fall back to pure Java — useful for optional native acceleration.

## Off-Heap Memory

Direct `ByteBuffer` allocates memory outside the GC heap:

```java
ByteBuffer buf = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder());
buf.putInt(42);
buf.flip();
int val = buf.getInt(); // 42
```

Use cases:
- Large buffers that would cause GC pressure
- Zero-copy I/O (NIO channels)
- Interop with native code via JNI
- Memory-mapped files (`FileChannel.map()`)

The `OffHeapDoubleArray` class shows the `Cleaner` pattern (Java 9+) for deterministic resource cleanup.

> **Java 21+**: The Foreign Function & Memory API (`java.lang.foreign`) provides a safer, more ergonomic alternative to both JNI and direct ByteBuffers. Consider migrating when targeting Java 21+.

## Performance Optimization

### Run Benchmarks

```bash
mvn -pl examples/systems compile exec:java \
  -Dexec.mainClass="com.example.template.systems.PerformanceBenchmarks"
```

### Key Findings (typical results)

| Benchmark | Relative Speed | Why |
|---|---|---|
| `StringBuilder` vs `String +=` | ~50-100x faster | Avoids O(n²) copying |
| `long` vs `Long` sum | ~3-5x faster | No auto-boxing allocation |
| `ArrayList` vs `LinkedList` | ~2-3x faster | Cache-friendly contiguous memory |
| `for` loop vs `Stream` | ~similar | JIT optimizes both; streams add small overhead |

### Optimization Checklist

1. **Measure first** — use JMH, JFR, or async-profiler before optimizing
2. **Avoid premature optimization** — focus on algorithmic complexity first
3. **Minimize allocations** in hot paths (reuse objects, use primitives)
4. **Right-size collections** — `new ArrayList<>(expectedSize)`, `new HashMap<>(cap, 0.75f)`
5. **Prefer `StringBuilder`** for string building in loops
6. **Use primitives** over boxed types in computation-heavy code
7. **Keep hot methods small** — helps JIT inlining (< ~325 bytecodes)
8. **Avoid megamorphic calls** — limit interface implementations at hot call sites to ≤2

## Profiling

### JDK Flight Recorder (JFR) — Recommended

```bash
# Start recording with the application
java -XX:StartFlightRecording=filename=app.jfr,duration=60s,settings=profile -jar app.jar

# Attach to running process
jcmd <pid> JFR.start filename=app.jfr duration=60s

# Analyze with JDK Mission Control (JMC)
jmc app.jfr
```

### async-profiler — CPU & Allocation Profiling

```bash
# CPU flame graph
./asprof -d 30 -f cpu.html <pid>

# Allocation profiling
./asprof -e alloc -d 30 -f alloc.html <pid>

# Wall-clock (includes I/O wait)
./asprof -e wall -d 30 -f wall.html <pid>
```

### Programmatic Profiling (ProfilingUtils)

```java
ProfilingUtils.profileTask("My Operation", () -> {
    // code to profile
});
// Prints: heap usage, GC count/time, elapsed time
```

### Useful JVM Flags

```
-Xlog:gc*:file=gc.log                    # GC logging (Java 9+)
-XX:NativeMemoryTracking=summary          # Native memory tracking
-XX:+HeapDumpOnOutOfMemoryError           # Auto heap dump on OOM
-XX:+PrintCompilation                     # JIT compilation log
-XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining  # Inlining decisions
```

## How to Run

```bash
# Compile
mvn -pl examples/systems compile

# Run tests
mvn -pl examples/systems test

# Run benchmarks
mvn -pl examples/systems compile exec:java \
  -Dexec.mainClass="com.example.template.systems.PerformanceBenchmarks"
```

## Related Documentation

- [Main README](../../README.md) — Project overview and quick start
- [Best Practices](../../docs/best-practices.md) — Performance, code style
- [Development Workflow](../../docs/development-workflow.md) — Profiling guidance
- [HPC Module](../hpc/README.md) — Parallel streams, virtual threads
- [Toolchain](../../docs/TOOLCHAIN.md) — JDK and build tool setup
