# AGENTS.md — `app` tests

Read `app/AGENTS.md` first and the root `AGENTS.md`.

- `ApplicationTest` — `@SpringBootTest` context-load smoke test.
- `ApplicationMainTest` — covers `main()` via `Mockito.mockStatic(SpringApplication.class)`.
  It exists **only** to keep this module above the 80% JaCoCo gate; do not delete it
  as redundant.
- Style: JUnit 5 + AssertJ (`assertThat`), `*Test` suffix (surefire), `*IT` suffix for
  anything needing failsafe/Docker. Copy patterns from `examples/testing/` — that
  module is the testing reference for the repo.
