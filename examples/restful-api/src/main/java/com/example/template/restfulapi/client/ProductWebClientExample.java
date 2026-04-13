package com.example.template.restfulapi.client;

import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.dto.ProductResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Non-blocking REST client using Spring WebFlux {@link WebClient}.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Reactive HTTP calls returning {@link Mono} and {@link reactor.core.publisher.Flux}</li>
 *   <li>Error handling with {@code onStatus}</li>
 *   <li>Timeout configuration</li>
 *   <li>Blocking bridge via {@code block()} for interop with imperative code</li>
 * </ul>
 *
 * <p>Prefer WebClient over RestTemplate when:
 * <ul>
 *   <li>You need non-blocking I/O (high-throughput scenarios)</li>
 *   <li>You're already in a reactive pipeline</li>
 *   <li>You want streaming responses</li>
 * </ul>
 */
public class ProductWebClientExample {

    private final WebClient webClient;

    /**
     * Creates a WebClient targeting the given base URL.
     *
     * @param baseUrl the API base URL (e.g. {@code http://localhost:8080})
     */
    public ProductWebClientExample(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Reactive GET all — returns a Mono that emits the full list.
     *
     * @return mono emitting the list of all products
     */
    public Mono<List<ProductResponse>> listReactive() {
        return webClient.get()
                .uri("/api/v1/products")
                .retrieve()
                .bodyToFlux(ProductResponse.class)
                .collectList();
    }

    /**
     * Reactive GET by ID with error handling.
     *
     * @param id the product UUID
     * @return mono emitting the product response
     */
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public Mono<ProductResponse> getReactive(UUID id) {
        return webClient.get()
                .uri("/api/v1/products/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        resp -> Mono.error(new RuntimeException("Product not found: " + id))) // NOPMD - example code
                .bodyToMono(ProductResponse.class);
    }

    /**
     * Reactive POST to create a product.
     *
     * @param request the product creation request
     * @return mono emitting the created product response
     */
    public Mono<ProductResponse> createReactive(ProductRequest request) {
        return webClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ProductResponse.class);
    }

    /**
     * Reactive PUT to update a product.
     *
     * @param id      the product UUID
     * @param request the updated product data
     * @return mono emitting the updated product response
     */
    public Mono<ProductResponse> updateReactive(UUID id, ProductRequest request) {
        return webClient.put()
                .uri("/api/v1/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ProductResponse.class);
    }

    /**
     * Reactive DELETE.
     *
     * @param id the product UUID
     * @return mono completing when the delete finishes
     */
    public Mono<Void> deleteReactive(UUID id) {
        return webClient.delete()
                .uri("/api/v1/products/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // ── Blocking bridge for imperative code ────────────────────────────

    /**
     * Blocking wrapper — useful when integrating reactive client into non-reactive code.
     *
     * @return list of all products
     */
    public List<ProductResponse> listBlocking() {
        return listReactive().block(Duration.ofSeconds(5));
    }

    /**
     * Blocking create with timeout.
     *
     * @param request the product creation request
     * @return the created product response
     */
    public ProductResponse createBlocking(ProductRequest request) {
        return createReactive(request).block(Duration.ofSeconds(5));
    }
}
