package com.example.template.restfulapi.client;

import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.dto.ProductResponse;
import com.example.template.restfulapi.exception.ClientException;
import com.example.template.restfulapi.exception.ResourceNotFoundException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Non-blocking REST client using Spring WebFlux {@link WebClient}.
 *
 * <p>Demonstrates:
 *
 * <ul>
 *   <li>Reactive HTTP calls returning {@link Mono} and {@link reactor.core.publisher.Flux}
 *   <li>Error handling with {@code onStatus} on every call
 *   <li>Timeout configuration via Reactor Netty {@code responseTimeout}
 *   <li>Blocking bridge via {@code block()} for interop with imperative code
 * </ul>
 *
 * <p>Prefer WebClient over RestTemplate when:
 *
 * <ul>
 *   <li>You need non-blocking I/O (high-throughput scenarios)
 *   <li>You're already in a reactive pipeline
 *   <li>You want streaming responses
 * </ul>
 */
public class ProductWebClientExample {

  private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(5);

  private final WebClient webClient;

  /**
   * Creates a WebClient targeting the given base URL, with a response timeout.
   *
   * @param baseUrl the API base URL (e.g. {@code http://localhost:8080})
   */
  public ProductWebClientExample(String baseUrl) {
    this(
        WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(
                new ReactorClientHttpConnector(
                    HttpClient.create().responseTimeout(RESPONSE_TIMEOUT)))
            .build());
  }

  /**
   * Creates a client around an existing {@link WebClient} (tests and custom factories).
   *
   * @param webClient the configured WebClient
   */
  ProductWebClientExample(WebClient webClient) {
    this.webClient = webClient;
  }

  /**
   * Reactive GET all — returns a Mono that emits the full list.
   *
   * @return mono emitting the list of all products
   */
  public Mono<List<ProductResponse>> listReactive() {
    return withErrorMapping(webClient.get().uri("/api/v1/products").retrieve(), null)
        .bodyToFlux(ProductResponse.class)
        .collectList();
  }

  /**
   * Reactive GET by ID with error handling.
   *
   * @param id the product UUID
   * @return mono emitting the product response
   */
  public Mono<ProductResponse> getReactive(UUID id) {
    return withErrorMapping(webClient.get().uri("/api/v1/products/{id}", id).retrieve(), id)
        .bodyToMono(ProductResponse.class);
  }

  /**
   * Reactive POST to create a product.
   *
   * @param request the product creation request
   * @return mono emitting the created product response
   */
  public Mono<ProductResponse> createReactive(ProductRequest request) {
    return withErrorMapping(
            webClient
                .post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve(),
            null)
        .bodyToMono(ProductResponse.class);
  }

  /**
   * Reactive PUT to update a product.
   *
   * @param id the product UUID
   * @param request the updated product data
   * @return mono emitting the updated product response
   */
  public Mono<ProductResponse> updateReactive(UUID id, ProductRequest request) {
    return withErrorMapping(
            webClient
                .put()
                .uri("/api/v1/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve(),
            id)
        .bodyToMono(ProductResponse.class);
  }

  /**
   * Reactive DELETE.
   *
   * @param id the product UUID
   * @return mono completing when the delete finishes
   */
  public Mono<Void> deleteReactive(UUID id) {
    return withErrorMapping(webClient.delete().uri("/api/v1/products/{id}", id).retrieve(), id)
        .bodyToMono(Void.class);
  }

  // ── Blocking bridge for imperative code ────────────────────────────

  /**
   * Blocking wrapper — useful when integrating reactive client into non-reactive code.
   *
   * @return list of all products
   */
  public List<ProductResponse> listBlocking() {
    List<ProductResponse> body = listReactive().block(BLOCK_TIMEOUT);
    return body == null ? List.of() : body;
  }

  /**
   * Blocking create with timeout.
   *
   * @param request the product creation request
   * @return the created product response
   */
  public ProductResponse createBlocking(ProductRequest request) {
    ProductResponse body = createReactive(request).block(BLOCK_TIMEOUT);
    if (body == null) {
      throw new ClientException("Empty response when creating product");
    }
    return body;
  }

  private static WebClient.ResponseSpec withErrorMapping(WebClient.ResponseSpec spec, UUID id) {
    return spec.onStatus(
            status -> status.value() == 404,
            resp -> Mono.error(new ResourceNotFoundException("Product not found: " + id)))
        .onStatus(
            HttpStatusCode::isError,
            resp ->
                Mono.error(
                    new ClientException(
                        "Request failed with status: " + resp.statusCode().value())));
  }
}
