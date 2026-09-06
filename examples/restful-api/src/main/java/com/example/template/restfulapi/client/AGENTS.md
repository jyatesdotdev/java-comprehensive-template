# AGENTS.md — `restfulapi.client`

Read `examples/restful-api/AGENTS.md` for module context.

- Three deliberate, parallel demonstrations of the same CRUD calls:
  `ProductRestTemplateClient` (classic RestTemplate + Spring 6.1 `RestClient`),
  `ProductWebClientExample` (reactive WebClient, `Mono`/`Flux` + a bounded
  `block(Duration)` bridge), `ProductJaxRsClient` (Jersey, `AutoCloseable`,
  try-with-resources on `Response`, explicit connect/read timeouts).
- These are **copyable demos, not wired beans** — README examples instantiate them
  with `new`. `RestClientConfig` shows how you *would* wire them (timeouts configured
  centrally). Keep that separation; don't inject the demo classes into the server flow.
- Conventions to preserve: every remote call has a timeout; non-2xx handling is
  explicit (`onStatus` / status checks → `ClientException`/`ResourceNotFoundException`);
  list responses use `ParameterizedTypeReference`/`GenericType`, never raw types.
  `ProductRestTemplateClient` uses `RestTemplateBuilder` (never `new RestTemplate()`).
- Package-visible constructors inject `RestTemplate`/`RestClient`/`WebClient`/`Client`
  for tests — do not use `ReflectionTestUtils.setField`.
- Jersey artifacts are version-less here; bump `jersey.version` in the root pom.
- Tests: error-path unit tests live in `src/test/.../client/`.
