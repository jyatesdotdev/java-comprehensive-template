# AGENTS.md — `database` package root

Read `examples/database/AGENTS.md` first — it defines the persistence patterns. Each
subpackage has its own AGENTS.md.

- Layering: `service` (transaction boundary) → `repository` (Spring Data JPA) →
  `entity`. `jdbc` is a deliberate parallel access path (raw JdbcTemplate) over the
  same tables; `config` holds infrastructure wiring. Don't mix the JPA and JDBC paths
  in one call stack.
- This root package holds only `DatabaseExampleApplication`.
- Schema is owned by Flyway (`src/main/resources/db/migration`, portable DDL);
  Hibernate only validates (`ddl-auto: validate`). Entity changes therefore always
  come with a new `V{n}__*.sql` migration — never an edit to an applied one.
