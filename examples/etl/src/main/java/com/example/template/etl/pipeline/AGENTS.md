# AGENTS.md — `etl.pipeline`

Read `examples/etl/AGENTS.md` for module context.

- `DataPipeline<T>` is **pure JDK** — no Spring, no Spark, no third-party imports.
  Keep it that way; its whole point is showing when framework overhead is unnecessary.
- API shape: static factories (`extract(Extractor)`, `of(Iterable)`), fluent
  intermediate ops returning a NEW pipeline (`filter`, `transform` — the type may
  change), terminal ops (`load(Loader, batchSize)`, `collect()`). Extension points are
  nested `@FunctionalInterface`s.
- Inputs are validated eagerly (`load` rejects non-positive `batchSize` with
  `IllegalArgumentException`) — new operations follow suit: fail fast, document with
  `@throws`.
- Fully unit-tested in `src/test/.../pipeline/DataPipelineTest` — every public method
  and edge case (empty source, batch > data, filter-all). New API = new tests, same file.
