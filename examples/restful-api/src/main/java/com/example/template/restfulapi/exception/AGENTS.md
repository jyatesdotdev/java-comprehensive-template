# AGENTS.md — `restfulapi.exception`

Read `examples/restful-api/AGENTS.md` for the layering contract.

- Domain exceptions extend `RuntimeException` (unchecked) with `serialVersionUID = 1L`
  and a `*Exception` suffix. Throw them from services; never catch them in controllers.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) is the ONLY place exceptions
  become HTTP: `ResourceNotFoundException` → 404, `MethodArgumentNotValidException` →
  400 with field-level `details`, catch-all `Exception` → 500. Every handler returns a
  consistent `ErrorResponse` body.
- Logging discipline: debug for expected client errors (404s), **error with the
  throwable** for the catch-all; never leak internals (stack traces, SQL) into the
  response message.
- Adding an exception type = adding a handler mapping here + a `@WebMvcTest` case
  asserting status and body shape.
