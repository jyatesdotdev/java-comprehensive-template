# AGENTS.md — `etl` package root

Read `examples/etl/AGENTS.md` first — especially the Spark JVM-flags and logging
sections. Each subpackage has its own AGENTS.md.

- Three intentionally separate approaches, one per subpackage: `spark/` (cluster-scale
  Spark), `batch/` (Spring Batch chunk processing), `pipeline/` (dependency-free pure
  Java). They must not depend on each other — each demonstrates a self-contained
  answer to "how do I process data".
- There is no `@SpringBootApplication` in this module by design; the batch job is
  configuration-only and needs a host app to run.
- Logging backend is Logback; the pom excludes Spark's `log4j-slf4j2-impl` to avoid a
  fatal SLF4J provider conflict — keep those exclusions when touching Spark deps.
