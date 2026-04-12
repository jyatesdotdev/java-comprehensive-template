# ETL & Batch Processing

Data pipeline patterns using Apache Spark, Spring Batch, and a pure-Java pipeline abstraction.

## Contents

| Class | Pattern | Framework |
|---|---|---|
| `SparkEtlExample` | RDD word count, DataFrame CSV→Parquet, Spark SQL, typed Datasets | Apache Spark 3.5 |
| `CsvToJsonBatchConfig` | Chunk-oriented CSV→JSON with skip/fault-tolerance | Spring Batch 5 |
| `DataPipeline` | Composable Extract→Filter→Transform→Load pipeline | Pure Java (no deps) |

## Apache Spark (`spark/`)

Three API styles demonstrated:

- **RDD API** — `wordCountRdd()`: parallelize → flatMap → mapToPair → reduceByKey. Low-level, full control.
- **DataFrame API** — `csvTransformExample()`: read CSV → filter/derive columns → groupBy/agg → write Parquet. Preferred for Spark 3.x (Catalyst optimizer, schema enforcement).
- **Spark SQL** — `sparkSqlExample()`: register temp view, run SQL with aggregations and window functions.
- **Typed Dataset** — `typedDatasetExample()`: compile-time type safety with `Encoders.bean()`.

### Running Spark locally

```java
// In-process (tests/dev)
SparkEtlExample.wordCountRdd(List.of("hello world", "hello spark"));

// Production: package as uber-jar, submit to cluster
// spark-submit --master yarn --class com.example.template.etl.spark.SparkEtlExample target/template-etl.jar
```

### Java 17+ compatibility

Spark 3.5 requires JVM module opens. These are configured in `pom.xml` via surefire `argLine`:
```
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.nio=ALL-UNNAMED
```

## Spring Batch (`batch/`)

A complete chunk-oriented job: **CSV → Transform → JSON**.

Key patterns:
- `FlatFileItemReader` with column mapping and header skip
- `ItemProcessor` with filtering (return `null` to skip) and business logic
- `JsonFileItemWriter` with Jackson marshalling
- Fault tolerance: `skipLimit(10)` for `NumberFormatException` on malformed data
- H2 in-memory job repository (auto-initialized)

### Configuration

```yaml
spring.batch.jdbc.initialize-schema: always  # create batch metadata tables
spring.batch.job.enabled: false               # launch jobs programmatically
```

### Running

```java
@Autowired JobLauncher launcher;
@Autowired Job csvToJsonJob;

launcher.run(csvToJsonJob, new JobParametersBuilder()
    .addLocalDateTime("runTime", LocalDateTime.now())
    .toJobParameters());
```

## Data Pipeline (`pipeline/`)

A lightweight, framework-free pipeline for in-process ETL:

```java
int loaded = DataPipeline.<RawRecord>extract(() -> readFromDb())
    .filter(RawRecord::isValid)
    .transform(this::enrich)
    .transform(this::normalize)
    .load(batch -> writeToTarget(batch), 500);
```

Features:
- Composable `filter()` and `transform()` stages
- Batch loading with configurable chunk size
- Functional interfaces (`Extractor`, `Loader`) for easy testing
- No framework dependencies — works anywhere

## When to use what

| Scenario | Recommendation |
|---|---|
| Large-scale distributed data (TB+) | Apache Spark |
| Scheduled batch jobs with restart/retry | Spring Batch |
| In-process transforms, testing, small data | `DataPipeline` |
| Stream processing | Consider Kafka Streams or Flink (not covered here) |

## How to Build & Test

```bash
# From project root
./mvnw -pl examples/etl compile
./mvnw -pl examples/etl test

# Run only DataPipeline tests (no Spark/Spring context)
./mvnw -pl examples/etl test -Dtest=DataPipelineTest
```

## Related Documentation

- [Main README](../../README.md) — Project overview and quick start
- [Architecture Patterns](../../docs/architecture-patterns.md) — CQRS, event sourcing patterns
- [Third-Party Libraries](../../docs/third-party-libraries.md) — Spring Batch, Spark reference
- [Best Practices](../../docs/best-practices.md) — Code style and conventions
- [Tutorial](../../docs/TUTORIAL.md) — New developer walkthrough
