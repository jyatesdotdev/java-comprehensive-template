# AGENTS.md — etl `pipeline` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `pipeline/AGENTS.md`.
Local: pure-JDK unit tests for the full `DataPipeline` API — construction, chaining
across type changes, batching (batch sizes and returned counts), edge cases (empty
source, batch > data, filter-all), and the positive-`batchSize` validation. New
pipeline API = new cases in `DataPipelineTest`, same file.
