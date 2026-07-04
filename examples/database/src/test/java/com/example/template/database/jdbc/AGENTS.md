# AGENTS.md — database `jdbc` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `jdbc/AGENTS.md`. Local:
`@JdbcTest` + `@Import(JdbcOrderDao.class)` — the slice auto-configures JdbcTemplate
and runs Flyway on H2, but doesn't scan `@Repository` beans, hence the explicit
import. Cover generated-key inserts, projections/ordering, and `batchUpdate` counts.
