# AGENTS.md — `etl.spark`

Read `examples/etl/AGENTS.md` for module context — especially the JVM flags section.

- `SparkEtlExample` is a static-method demo of the four Spark API levels: RDD
  (`JavaSparkContext` in try-with-resources, `local[*]`), DataFrame (CSV → transform →
  Parquet; **takes a caller-owned `SparkSession`** — never return a `Dataset` after
  closing the session), Spark SQL (temp view + text-block SQL), typed `Dataset`
  (`Encoders.bean` over a `Serializable` record).
- **Any new execution path needs the module-system flags**: compile already has
  `--add-exports java.base/sun.nio.ch=ALL-UNNAMED`; runtime needs the three
  `--add-opens` flags from the surefire `argLine` or `SparkContext` creation fails.
- Spark resources are always closed via try-with-resources; masters are `local[*]` in
  examples (cluster submission is a deployment concern, not code).
- Artifacts are Scala-suffixed `_2.13` and versioned by the parent's `spark.version`;
  keep the logging exclusions on both spark deps (see module guide — provider clash
  is fatal at runtime).
- Spark execution is still JVM-heavy and needs servlet classes Spark pulls for its UI
  even with `spark.ui.enabled=false`. Do not add a surefire test unless that classpath
  is solved. DataFrame/SQL/typed Dataset paths remain example-only.
