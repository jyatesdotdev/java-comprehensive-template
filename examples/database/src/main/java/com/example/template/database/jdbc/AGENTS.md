# AGENTS.md — `database.jdbc`

Read `examples/database/AGENTS.md` for the persistence patterns.

- This is the deliberate **non-JPA** access path: `JdbcTemplate` (positional params),
  `NamedParameterJdbcTemplate` (`MapSqlParameterSource`), `SimpleJdbcInsert`
  (`executeAndReturnKey`), and `batchUpdate`. It coexists with the JPA stack over the
  same tables — don't call it from within a JPA transaction expecting first-level
  cache coherence.
- Projections are immutable **records** (`OrderSummary`) mapped by a `static final
  RowMapper` constant — never map to JPA entities here.
- SQL is always parameterized (`?` or named) — string-concatenated SQL is a
  FindSecBugs finding and a review rejection.
- Class is `@Repository`-annotated for exception translation.
- Tests: `@JdbcTest` + `@Import(JdbcOrderDao.class)` in `src/test/.../jdbc/`, running
  against H2 with the real Flyway schema.
