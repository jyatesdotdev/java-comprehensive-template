# AGENTS.md — `examples/etl/`

Read the root and `examples/AGENTS.md` first. Three approaches to data processing:
Apache Spark, Spring Batch, and a dependency-free Java pipeline.

## Layout

| Class | Shows |
|-------|-------|
| `spark/SparkEtlExample` | RDD word count (`JavaSparkContext` in try-with-resources, `local[*]`), DataFrame CSV→Parquet, Spark SQL over temp views (text blocks), typed `Dataset` via `Encoders.bean` with a `Serializable` record |
| `batch/CsvToJsonBatchConfig` | Spring Batch 5 chunk job (chunk size 100): `FlatFileItemReader` (classpath `data/input.csv`) → filtering `ItemProcessor` (return `null` to drop) → `JsonFileItemWriter`; `faultTolerant().skipLimit(10).skip(NumberFormatException.class)` |
| `pipeline/DataPipeline<T>` | Pure-Java extract/filter/transform/load abstraction with `@FunctionalInterface` extension points — the only part with tests (`DataPipelineTest`) |

Resources: `application.yml` (H2 `batchdb`, batch schema auto-init, jobs **not**
auto-run) and `data/input.csv` (sample input) both exist under `src/main/resources/`.

## Spark on modern JVMs — do not remove these flags

Spark 3.5 needs module-system escapes on Java 17+/21. This pom already configures:

- compiler: `--add-exports java.base/sun.nio.ch=ALL-UNNAMED`
- surefire `argLine`: `--add-opens java.base/java.lang=ALL-UNNAMED
  --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED`

Any *other* way you launch Spark code (exec plugin, IDE run config, `java -jar`) needs
the same `--add-opens` flags or `SparkContext` construction fails at runtime.

Spark artifacts are Scala-suffixed (`spark-core_2.13`, `spark-sql_2.13`, version from
the parent's `spark.version`). Keep the `_2.13` suffix consistent, and keep **all** the
logging exclusions (`slf4j-log4j12`, `log4j`, and `log4j-slf4j2-impl` on both Spark
artifacts): Logback is the backend here, and `log4j-slf4j2-impl` cannot coexist with
Spring Boot's `log4j-to-slf4j` bridge — SLF4J 2 picks the Log4j provider and throws a
`LoggingException` at the first `LoggerFactory.getLogger` call in any plain JVM launch.

## Commands

```bash
./mvnw -pl examples/etl compile
./mvnw -pl examples/etl test                       # 17 tests (pipeline + batch beans)
./mvnw -pl examples/etl test -Dtest=DataPipelineTest
```

## Gotchas

- **There is no `@SpringBootApplication` main class in this module.** The batch job is
  configuration-only; running it requires a host app that injects `JobLauncher` and the
  `csvToJsonJob` bean (the README shows the snippet). Don't "fix" a failed
  `spring-boot:run` by hacking the pom — add a main class deliberately if you need one.
- `SparkEtlExample` has **no `main`** either, despite the README's `spark-submit
  --class` example — its methods are called in-process. Add a `main` if you actually
  need spark-submit.
- Tests (17): `DataPipelineTest` covers the whole pipeline API including the
  positive-`batchSize` guard; `CsvToJsonBatchConfigTest` instantiates the config
  directly (no Spring context) and exercises the processor tiers/filtering plus the
  reader against the real `data/input.csv`. **Spark code is untested** (heavy runtime);
  if you add Spark logic, test it with a `local[*]` session inside surefire — the
  `argLine` above already makes that possible.
- Spring Batch versions come from the Spring Boot BOM in the parent — don't add a
  module-local version property.
- Batch writer outputs to filesystem path `output/results.json` (relative to the
  working directory) — mind that in tests/CI.
- `jacoco.skip=true`.
