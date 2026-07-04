# AGENTS.md — `restfulapi.service`

Read `examples/restful-api/AGENTS.md` for the layering contract.

- Pattern: interface (`ProductService`) + strategy-named implementation
  (`InMemoryProductService`) — never `*Impl`. Controllers see only the interface.
- Misses throw `ResourceNotFoundException` (unchecked); document it with `@throws` on
  the interface method. Don't return `null` — throw, or return `Optional`/empty
  collections.
- Return defensive copies (`List.copyOf`) — never the live backing collection.
- The in-memory store is a demo: `ConcurrentHashMap` protects the map but entities are
  mutable and updates are not atomic. Real persistence belongs in a repository layer
  (see `examples/database`); don't grow storage logic here.
- SLF4J parameterized logging; debug for expected misses, never log-and-rethrow.
- Tests: plain unit tests in `src/test/.../service/` (no Spring context needed).
