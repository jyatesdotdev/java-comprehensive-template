# AGENTS.md — `hpc` tests

Read `examples/hpc/AGENTS.md` first.

- 35 tests, one test class per example class. The determinism rules are absolute:
  join futures with explicit timeouts (`get(5, SECONDS)`, AssertJ
  `succeedsWithin`/`failsWithin`); coordinate with `CountDownLatch`/`CyclicBarrier`;
  guard blocking tests with `@Timeout`; **never** assert on `Thread.sleep` timing.
- Proving concurrency properties: the max-concurrency test for `RateLimiter` uses a
  `CyclicBarrier(2)` to demonstrate exactly-two permits — copy that technique rather
  than statistical sampling.
- Error-path futures: assert the *cause chain* (`ExecutionException`/
  `CompletionException` cause), not just "it threw".
- Keep the whole module's test run under ~60 seconds; use modest counts for
  mass-concurrency demos (1_000 virtual threads proves the point as well as 100_000).
- PMD applies: every test asserts; no `System.out`; repeated literals become constants.
