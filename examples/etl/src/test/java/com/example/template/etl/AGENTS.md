# AGENTS.md — `etl` tests

Read `examples/etl/AGENTS.md` first; testing styles come from `examples/testing/`.

- Tests, no Docker: `pipeline/DataPipelineTest` (full pipeline API + validation
  edges, including loaders that store the batch) and `batch/CsvToJsonBatchConfigTest`
  (config instantiated directly with `new`). Spark remains untested in surefire
  (missing `javax.servlet`); the API no longer returns a Dataset after closing the
  session.
- Batch processor convention: a `null` return from `ItemProcessor` means "filtered
  out" — assert it as such, it's not a failure.
- PMD applies: assertions in every test, no `System.out`, literals hoisted to
  constants when repeated.
