# AGENTS.md — etl `batch` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `batch/AGENTS.md`. Local:
instantiate `new CsvToJsonBatchConfig()` directly — **no Spring context, no
JobLauncher**. Drive the processor by hand (remember: `null` return = filtered, not
failure) and the reader via `open(new ExecutionContext())` against the real
`data/input.csv`, closing in a finally. A full job execution test would use
`spring-batch-test`'s `JobLauncherTestUtils` — deliberately not done here to keep the
suite context-free.
