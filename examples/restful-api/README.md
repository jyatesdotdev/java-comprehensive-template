# RESTful API Examples

Spring Boot REST server and client implementations demonstrating CRUD operations with proper layering.

## Server Architecture

```
controller/          → REST endpoints (@RestController)
  ProductController  → CRUD for /api/v1/products
service/             → Business logic interface + implementation
  ProductService     → Interface
  InMemoryProductService → ConcurrentHashMap-backed impl
domain/              → Domain entities
  Product            → Mutable entity with identity
dto/                 → Request/response records
  ProductRequest     → Validated input (Jakarta Validation)
  ProductResponse    → Immutable output
  ErrorResponse      → Standardized error body
mapper/              → Entity ↔ DTO mapping
  ProductMapper      → Static utility mapper
exception/           → Error handling
  ResourceNotFoundException → Maps to 404
  GlobalExceptionHandler    → @RestControllerAdvice
```

## Client Implementations

```
client/
  ProductRestTemplateClient → RestTemplate + RestClient (Spring 6.1) — synchronous
  ProductWebClientExample   → WebClient (Spring WebFlux) — reactive/non-blocking
  ProductJaxRsClient        → JAX-RS Client API (Jersey) — framework-agnostic
  RestClientConfig          → Spring @Configuration for client beans with timeouts
```

### When to Use Which Client

| Client | Blocking? | Spring Required? | Best For |
|--------|-----------|------------------|----------|
| **RestTemplate** | Yes | Yes | Legacy code, simple synchronous calls |
| **RestClient** (6.1+) | Yes | Yes | New synchronous code (replaces RestTemplate) |
| **WebClient** | No | Yes (WebFlux) | High-throughput, reactive pipelines, streaming |
| **JAX-RS Client** | Yes | No | Jakarta EE apps, framework-agnostic code |

## Key Patterns Demonstrated

### Server
- **DTO separation**: Controllers never expose domain entities directly
- **Validation**: `@Valid` + Jakarta Bean Validation on request records
- **Global exception handling**: `@RestControllerAdvice` translates exceptions to consistent error responses
- **Proper HTTP semantics**: 201 + Location header for POST, 204 for DELETE, 404 for missing resources
- **Service interface**: Decouples controller from storage implementation
- **Java records**: Used for all DTOs (immutable, concise)

### Client
- **RestClient fluent API**: Modern replacement for RestTemplate with cleaner syntax
- **WebClient reactive chains**: Mono/Flux composition with error handling
- **JAX-RS resource lifecycle**: AutoCloseable client with try-with-resources
- **Timeout configuration**: Connection and read timeouts on all clients
- **Error handling**: Status-based error mapping on each client type

## How to Run

```bash
# From project root
./mvnw -pl examples/restful-api spring-boot:run

# Or compile only
./mvnw -pl examples/restful-api compile
```

## Example Requests

```bash
# Create a product
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Widget", "description": "A useful widget", "price": 29.99}'

# List all products
curl http://localhost:8080/api/v1/products

# Get by ID (replace <id> with actual UUID)
curl http://localhost:8080/api/v1/products/<id>

# Update
curl -X PUT http://localhost:8080/api/v1/products/<id> \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated Widget", "price": 39.99}'

# Delete
curl -X DELETE http://localhost:8080/api/v1/products/<id>
```

## Client Usage Examples

```java
// RestTemplate (classic)
var client = new ProductRestTemplateClient("http://localhost:8080");
var product = client.createWithRestTemplate(new ProductRequest("Widget", "Desc", BigDecimal.TEN));
var all = client.listWithRestTemplate();

// RestClient (Spring 6.1+ — preferred for new code)
var product = client.createWithRestClient(new ProductRequest("Widget", "Desc", BigDecimal.TEN));

// WebClient (reactive)
var wcClient = new ProductWebClientExample("http://localhost:8080");
wcClient.createReactive(request).subscribe(p -> System.out.println("Created: " + p));
// Or blocking bridge:
var products = wcClient.listBlocking();

// JAX-RS (framework-agnostic)
try (var jaxrs = new ProductJaxRsClient("http://localhost:8080")) {
    var product = jaxrs.create(new ProductRequest("Widget", "Desc", BigDecimal.TEN));
    jaxrs.delete(product.id());
}
```

## Related Documentation

- [Main README](../../README.md) — Project overview and quick start
- [Architecture Patterns](../../docs/architecture-patterns.md) — Microservices, hexagonal architecture
- [Best Practices](../../docs/best-practices.md) — Error handling, logging, code style
- [Documentation Standards](../../docs/documentation-standards.md) — OpenAPI/Swagger integration
- [Tutorial](../../docs/TUTORIAL.md) — New developer walkthrough
- [Extending the Template](../../docs/EXTENDING.md) — Adding endpoints and services
