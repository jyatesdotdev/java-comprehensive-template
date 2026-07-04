# AGENTS.md — `etl.spark`

Read `examples/etl/AGENTS.md` for module context — especially the JVM flags section.

- `SparkEtlExample` is a static-method demo of the four Spark API levels: RDD
  (`JavaSparkContext` in try-with-resources, `local[*]`), DataFrame (CSV → transform →
  Parquet), Spark SQL (temp view + text-block SQL), typed `Dataset`
  (`Encoders.bean` over a `Serializable` record).
- **Any new execution path needs the module-system flags**: compile already has
  `--add-exports java.base/sun.nio.ch=ALL-UNNAMED`; runtime needs the three
  `--add-opens` flags from the surefire `argLine` or `SparkContext` creation fails.
- Spark resources are always closed via try-with-resources; masters are `local[*]` in
  examples (cluster submission is a deployment concern, not code).
- Artifacts are Scala-suffixed `_2.13` and versioned by the parent's `spark.version`;
  keep the logging exclusions on both spark deps (see module guide — provider clash
  is fatal at runtime).
- This package is currently **untested** (JVM-heavy). If you add logic, a `local[*]`
  session test under surefire is acceptable; keep it under ~60s.
