# AGENTS.md — `etl` tests

Read `examples/etl/AGENTS.md` first; testing styles come from `examples/testing/`.

- 17 tests, no Docker, no Spring context: `pipeline/DataPipelineTest` (full pipeline
  API + validation edges) and `batch/CsvToJsonBatchConfigTest` (config instantiated
  directly with `new`; processor tiers/filtering; reader opened manually via
  `reader.open(new ExecutionContext())` against the real `data/input.csv`, closed in
  a finally).
- Spark is intentionally untested here. A future Spark test runs under surefire
  (the `--add-opens` argLine is already configured) with `local[*]`.
- Batch processor convention: a `null` return from `ItemProcessor` means "filtered
  out" — assert it as such, it's not a failure.
- PMD applies: assertions in every test, no `System.out`, literals hoisted to
  constants when repeated.
