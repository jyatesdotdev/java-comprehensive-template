# Database Interaction Examples

Demonstrates JPA/Hibernate, Spring Data JPA, JDBC, connection pooling, and transaction management.

## Module Structure

```
src/main/java/com/example/template/database/
├── entity/
│   ├── Order.java          # JPA entity with @OneToMany, @Version, lifecycle callbacks
│   ├── OrderItem.java      # Child entity with @ManyToOne
│   └── OrderStatus.java    # Enum mapped with @Enumerated
├── repository/
│   └── OrderRepository.java # Spring Data JPA: derived queries, JPQL, native SQL, bulk updates
├── service/
│   └── OrderService.java   # @Transactional: propagation, isolation, read-only, rollback rules
├── jdbc/
│   └── JdbcOrderDao.java   # JdbcTemplate, NamedParameterJdbcTemplate, SimpleJdbcInsert, batch ops
├── config/
│   └── DataSourceConfig.java # Programmatic HikariCP configuration
└── DatabaseExampleApplication.java

src/main/resources/
├── application.yml          # DataSource, HikariCP, JPA, Flyway config (H2 + PostgreSQL profiles)
└── db/migration/
    └── V1__create_orders.sql # Flyway migration script
```

## Key Concepts

### JPA / Hibernate
- Entity mapping with `@Entity`, `@Table`, `@Column`
- Relationships: `@OneToMany` / `@ManyToOne` with cascade and orphan removal
- Optimistic locking with `@Version`
- Lifecycle callbacks: `@PrePersist`, `@PreUpdate`
- Fetch strategies: `FetchType.LAZY` with `JOIN FETCH` for eager loading when needed

### Spring Data JPA
- Derived query methods (convention-based)
- `@Query` with JPQL and native SQL
- `@Modifying` for bulk updates
- `@Param` for named parameters

### Transaction Management
- Class-level `@Transactional(readOnly = true)` with method-level overrides
- Propagation: `REQUIRED` (default), `REQUIRES_NEW` (independent transaction)
- Isolation: `REPEATABLE_READ` for consistent reads
- Rollback rules: `rollbackFor = Exception.class`

### JDBC (when JPA isn't the right fit)
- `JdbcTemplate` — positional parameters, `queryForObject`, `batchUpdate`
- `NamedParameterJdbcTemplate` — named parameters with `MapSqlParameterSource`
- `SimpleJdbcInsert` — fluent insert with auto-generated keys
- `RowMapper` — reusable result set mapping

### Connection Pooling (HikariCP)
- Pool sizing: `maximumPoolSize`, `minimumIdle`
- Timeouts: `connectionTimeout`, `idleTimeout`, `maxLifetime`
- Leak detection: `leakDetectionThreshold`
- Profile-specific tuning (dev vs production)

### Schema Migration (Flyway)
- Versioned migrations: `V{version}__{description}.sql`
- `baseline-on-migrate` for existing databases
- Profile-specific migration locations

## Configuration Profiles

| Profile     | Database   | Pool Size | DDL Strategy |
|-------------|------------|-----------|--------------|
| default     | H2 (mem)   | 10        | validate     |
| production  | PostgreSQL | 20        | validate     |

## Best Practices Demonstrated

1. **Disable OSIV** — `spring.jpa.open-in-view: false` prevents lazy loading outside transactions
2. **Flyway for DDL** — `ddl-auto: validate` ensures Hibernate matches Flyway-managed schema
3. **Batch operations** — `hibernate.jdbc.batch_size` + `order_inserts` for bulk performance
4. **N+1 prevention** — `default_batch_fetch_size` and explicit `JOIN FETCH` queries
5. **Read-only transactions** — class-level default reduces lock overhead for reads
6. **Leak detection** — HikariCP warns when connections aren't returned promptly

## Related Documentation

- [Main README](../../README.md) — Project overview and quick start
- [Best Practices](../../docs/best-practices.md) — Resource management, error handling
- [Third-Party Libraries](../../docs/third-party-libraries.md) — HikariCP, Flyway reference
- [Architecture Patterns](../../docs/architecture-patterns.md) — Layer design and project structure
- [Tutorial](../../docs/TUTORIAL.md) — New developer walkthrough
