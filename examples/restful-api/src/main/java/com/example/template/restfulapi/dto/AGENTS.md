# AGENTS.md — `restfulapi.dto`

Read `examples/restful-api/AGENTS.md` for the layering contract.

- DTOs are **records**, always. `*Request` for input, `*Response` for output.
- Requests carry Jakarta Validation on components (`@NotBlank`, `@NotNull`,
  `@Positive`, ...) — validation happens at the boundary, not in services — plus
  `@Schema` annotations for OpenAPI.
- Responses are pure output: no validation annotations, include server-managed fields
  (id, timestamps).
- Defensive copying: any collection component gets `List.copyOf(...)` in a compact
  constructor (see `ErrorResponse`) so the record can't alias a caller's mutable list.
  Convenience constructors delegate to the canonical one for defaults.
- No behavior beyond trivial derivation — logic lives in services; mapping lives in
  `mapper`.
- Tests: record construction, defaults, and defensive-copy semantics in
  `src/test/.../dto/`.
