# AGENTS.md — `database` tests

Read `examples/database/AGENTS.md` first; testing styles come from `examples/testing/`.

- All 47 tests run on embedded H2 with the **real Flyway migration** — no Docker.
  Packages mirror main: `entity/` (behavior + lifecycle callbacks called directly —
  they're package-private for exactly this), `service/` (Mockito, every exception
  path), `repository/` (`@DataJpaTest`, one case per query method), `jdbc/`
  (`@JdbcTest` + `@Import(JdbcOrderDao.class)`).
- `*Test` suffix only — these are surefire tests. A real-PostgreSQL test would be a
  Testcontainers `*IT` (model it on `examples/testing/PostgresContainerIT`).
- In `@DataJpaTest`, clear the persistence context (`entityManager.clear()`) before
  asserting re-reads — especially after `@Modifying` bulk updates, which bypass the
  first-level cache.
- Behavior pins to know: `findByIdWithItems` returns `Optional` and uses LEFT JOIN
  FETCH (item-less orders load with an empty collection); `OrderStatus` constant order
  is asserted because the column is STRING-mapped.
- PMD applies: assertions in every test, literals hoisted to constants, no `System.out`.
