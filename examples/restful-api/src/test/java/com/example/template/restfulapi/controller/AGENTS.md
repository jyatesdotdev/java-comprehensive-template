# AGENTS.md — restful-api `controller` tests

Rules: `../AGENTS.md` (module test guide) and the main-side
`controller/AGENTS.md`. Local: `@WebMvcTest(ProductController.class)` +
`@MockBean ProductService` — slice tests, no full context. Every endpoint covers:
happy path, validation 400 (assert field-level `details`), 404 via the advice
(assert `ErrorResponse` shape), and correct status/headers (201 + `Location`, 204).
