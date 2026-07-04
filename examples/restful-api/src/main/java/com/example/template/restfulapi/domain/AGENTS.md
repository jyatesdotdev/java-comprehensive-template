# AGENTS.md — `restfulapi.domain`

Read `examples/restful-api/AGENTS.md` for the layering contract.

- Domain entities (`Product`) are deliberately mutable POJOs — the one place in this
  module where mutability is the norm (mirroring what a JPA entity would need).
- Domain types NEVER appear in controller signatures or response bodies — the
  `mapper` package converts to/from DTO records at the boundary.
- Server-managed fields (`id`, `createdAt`, `updatedAt`) are set by the entity or the
  service, never accepted from client input.
- Keep this package dependency-free within the module: domain imports nothing from
  `controller`, `service`, `dto`, or `mapper`.
