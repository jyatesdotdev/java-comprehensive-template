# AGENTS.md — `restfulapi.config`

Read `examples/restful-api/AGENTS.md` for module context.

- `@Configuration` classes only — infrastructure wiring, no business logic.
- `OpenApiConfig` builds the springdoc `OpenAPI` metadata bean; keep it in sync with
  reality (title/version/contact) since it fronts `/swagger-ui.html`.
- HTTP client beans (`RestClientConfig`) live under `client/`, not this package.
- Reserve `@Bean` methods for third-party/infrastructure types; project-owned classes
  use `@Service`/`@Component` + constructor injection instead.
