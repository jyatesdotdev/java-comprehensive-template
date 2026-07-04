# AGENTS.md — `restfulapi` package root

Read `examples/restful-api/AGENTS.md` first — it defines the layering contract. Each
subpackage here has its own AGENTS.md with local rules.

- Dependency direction is strictly one-way: `controller → service (interface) →
  domain/dto`, with `mapper` guarding the domain↔DTO boundary and `exception`
  centralizing error translation. Never import "down-stack" types up-stack (e.g., a
  controller type inside a service).
- This root package holds only `RestApiApplication` (`@SpringBootApplication`); keep
  its `@SuppressWarnings("PMD.UseUtilityClass")` and its comment.
- A new resource means touching, in order: `domain` → `dto` → `service` → `mapper` →
  `controller` (+ tests per layer). If you skip a layer, you're probably leaking
  domain objects over HTTP.
