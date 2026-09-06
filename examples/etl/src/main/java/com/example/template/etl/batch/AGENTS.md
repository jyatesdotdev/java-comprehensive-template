# AGENTS.md — `etl.batch`

Read `examples/etl/AGENTS.md` for module context.

- `CsvToJsonBatchConfig` is a Spring Batch 5 chunk job as pure `@Configuration`:
  reader (`FlatFileItemReader` over classpath `data/input.csv`, header skipped) →
  processor (`ItemProcessor`; **return `null` to filter a record out** — that's the
  Batch idiom, not an error) → writer (`JsonFileItemWriter` to `output/results.json`,
  a working-directory-relative path).
- Fault tolerance is declared on the step: `faultTolerant().skipLimit(10)
  .skip(NumberFormatException.class)`. Widen skips deliberately, never to
  `Exception.class`.
- Chunk size is a named constant (`CHUNK_SIZE = 100`); records are nested Java
  `record`s (`InputRecord`/`OutputRecord`).
- There is no runner here: `spring.batch.job.enabled: false` and no
  `@SpringBootApplication` — launching requires a host app injecting `JobLauncher`.
  Do **not** add `@EnableBatchProcessing` on Boot 3.3 (it disables auto-config).
- Tests instantiate the config class **directly** (no Spring context) and drive the
  reader/processor by hand — see `CsvToJsonBatchConfigTest`; extend it for new steps.
