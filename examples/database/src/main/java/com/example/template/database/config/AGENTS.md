# AGENTS.md — `database.config`

Read `examples/database/AGENTS.md` for module context.

- `DataSourceConfig` builds the HikariCP pool programmatically:
  `DataSourceProperties` + a `@Primary @ConfigurationProperties("spring.datasource.hikari")`
  `HikariDataSource` bean. It **replaces** Spring Boot's auto-configured pool — the
  `spring.datasource.hikari.*` block in `application.yml` binds to this bean.
- This pattern earns its keep when you need multiple data sources or non-default pool
  wiring; for a single default pool, plain auto-configuration + yml would suffice
  (the javadoc says so — keep that honesty).
- Pool tuning values live in `application.yml`, not in code; the production Spring
  profile overrides them there.
- Infrastructure wiring only — no business logic, no entity imports.
