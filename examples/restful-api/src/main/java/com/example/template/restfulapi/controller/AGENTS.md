# AGENTS.md — `restfulapi.controller`

Read `examples/restful-api/AGENTS.md` for the layering contract.

- Controllers are HTTP translation only — no business logic, no storage access.
  Depend on the **service interface**, injected via constructor.
- Signatures use DTO records exclusively; convert with `ProductMapper`. A `domain`
  type in a controller signature is a bug.
- HTTP semantics are non-negotiable: POST → 201 + `Location` header via
  `ResponseEntity.created(...)`; DELETE → 204 `noContent()`; not-found → throw
  `ResourceNotFoundException` (the `@RestControllerAdvice` maps it to 404); validation
  via `@Valid @RequestBody` (400 with field details comes free).
- Every endpoint carries `@Operation` / `@ApiResponses` / `@Tag` — Swagger UI at
  `/swagger-ui.html` must stay truthful.
- URL scheme: `/api/v1/<plural-noun>`; version bumps are a new path, not a breaking
  change to an existing one.
- Tests: `@WebMvcTest` + `@MockBean` service in `src/test/.../controller/` — extend
  `ProductControllerTest` when you add endpoints (happy path, validation 400, 404).
