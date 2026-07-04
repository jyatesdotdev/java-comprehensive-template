# AGENTS.md — `restfulapi.config`

Read `examples/restful-api/AGENTS.md` for module context.

- `@Configuration` classes only — infrastructure wiring, no business logic.
- `OpenApiConfig` builds the springdoc `OpenAPI` metadata bean; keep it in sync with
  reality (title/version/contact) since it fronts `/swagger-ui.html`.
- `RestClientConfig` centralizes HTTP client beans (RestTemplate via builder,
  RestClient wrapping it, WebClient with a Reactor Netty response timeout). The rule
  it demonstrates: **timeouts are configured once, here** — never inline them at call
  sites.
- Reserve `@Bean` methods for third-party/infrastructure types; project-owned classes
  use `@Service`/`@Component` + constructor injection instead.
