# AGENTS.md — `database.repository`

Read `examples/database/AGENTS.md` for the persistence patterns.

- Query ladder — use the simplest rung that works: derived method name → `@Query` JPQL
  with `@Param` → native SQL (last resort, and say why in javadoc).
- **Never return `null`**: single results are `Optional<T>`, collections are
  (possibly empty) `List<T>`.
- Fetching associations eagerly = `LEFT JOIN FETCH` in a dedicated method
  (`findByIdWithItems`) — LEFT, so parent rows without children still return.
- Bulk writes use `@Modifying @Query` and return the affected row count; remember the
  persistence context is stale afterwards (tests clear it before re-reading).
- No business logic here — conditions and orchestration belong in `service`.
- Every query method gets a `@DataJpaTest` case in `src/test/.../repository/` running
  against H2 + the real Flyway migration.
