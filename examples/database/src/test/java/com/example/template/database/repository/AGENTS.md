# AGENTS.md — database `repository` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `repository/AGENTS.md`.
Local: `@DataJpaTest` on embedded H2 with the **real Flyway migration**
(`ddl-auto: validate` holds). One test case per query method. After `@Modifying` bulk
updates, `entityManager.clear()` before re-reading — they bypass the first-level
cache. Behavior pin: `findByIdWithItems` is `Optional` + LEFT JOIN FETCH (item-less
orders load with empty items).
