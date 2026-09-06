# AGENTS.md — `examples/restful-api/`

Read the root and `examples/AGENTS.md` first. This is the **reference module for REST
layering** — when any doc says "follow the restful-api structure", this is the contract.

## Architecture (dependency direction is one-way, top to bottom)

```
controller/   ProductController — HTTP layer only. @Valid input, ResponseEntity out.
   ↓ (interface)
service/      ProductService (interface) + InMemoryProductService (@Service impl)
   ↓
domain/       Product — mutable entity, NEVER exposed over HTTP
dto/          ProductRequest / ProductResponse / ErrorResponse — immutable records
mapper/       ProductMapper — final class, static methods, entity ↔ DTO boundary
exception/    ResourceNotFoundException, ClientException, GlobalExceptionHandler
config/       OpenApiConfig
client/       Three standalone HTTP client demos + RestClientConfig (not wired into the server)
```

Rules encoded here — keep them when extending:

- Controllers depend on the **service interface**, never the implementation, and never
  touch `domain` types in signatures — DTOs only, converted via the mapper.
- DTOs are **records** with Jakarta Validation on components (`@NotBlank`,
  `@NotNull @Positive`) and `@Schema` OpenAPI docs. `ErrorResponse` defensively copies
  `details` in its compact constructor and provides convenience constructors for defaults.
- HTTP semantics: POST → `ResponseEntity.created(location)` (201 + Location header),
  DELETE → `noContent()` (204), misses → throw `ResourceNotFoundException` and let
  `GlobalExceptionHandler` (`@RestControllerAdvice`) map it to 404 with an
  `ErrorResponse`; validation failures become 400 with field-level details.
- Every endpoint carries `@Operation`/`@ApiResponses`/`@Tag` — Swagger UI at
  `/swagger-ui.html` must stay accurate.
- Constructor injection; SLF4J parameterized logging (debug for expected 404s, error
  for unhandled).

Base path: **`/api/v1/products`** (some docs say `/api/products` — wrong). New
resources: `/api/v1/<plural-noun>`.

## Commands

```bash
./mvnw -pl examples/restful-api spring-boot:run     # server on :8080
./mvnw -pl examples/restful-api test                # unit tests
curl -s localhost:8080/api/v1/products | jq .       # smoke check
```

## The three clients

`ProductRestTemplateClient` (RestTemplate + Spring 6.1 RestClient),
`ProductWebClientExample` (reactive WebClient, Mono/Flux + blocking bridge),
`ProductJaxRsClient` (Jersey, `AutoCloseable`, explicit timeouts). They are
**instantiated with `new` in README examples, not Spring-wired** — `RestClientConfig`
beans exist but nothing injects the client classes. Treat them as copyable demos.

## Module-specific facts and gotchas

- **Storage is a `ConcurrentHashMap`** in `InMemoryProductService` — data is lost on
  restart. There is no database here; persistence patterns live in `examples/database/`.
  Note `findById` returns the live mutable `Product` and `update` mutates it in place
  (the map is thread-safe, the entity is not) — fine for a demo, don't copy into
  concurrent production code.
- The Dockerfile HEALTHCHECK hits `/actuator/health`; `spring-boot-starter-actuator`
  is a dependency for exactly that reason — don't remove it. The Dockerfile expects a
  prebuilt jar: run `./mvnw -pl examples/restful-api -am package` before `docker build`.
- CI's `container-scan` and `docker` jobs build **this** module's Dockerfile.
- Jersey and springdoc versions live in the **root** pom (`jersey.version`,
  `springdoc.version`) — declare them version-less here. Bump all three Jersey
  artifacts together.
- `jacoco.skip=true`. Tests (45 `@Test` methods): `ProductControllerTest` (`@WebMvcTest`
  + `@MockBean`, including validation-400, 404-handler, bad UUID / malformed JSON),
  `InMemoryProductServiceTest`, `ProductMapperTest`, `ErrorResponseTest`,
  `ProductClientExceptionTest`, `RestApiApplicationTest` (context load). ArchUnit rules
  live in `archunit/` (`@ArchTest` fields, not `@Test`). Extend these when you touch
  the corresponding layer; `updateEntity` is a **full overwrite** (a null description
  clears the stored value) and a test pins that.
