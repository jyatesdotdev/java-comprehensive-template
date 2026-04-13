package com.example.template.restfulapi.client;

import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.dto.ProductResponse;
import com.example.template.restfulapi.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * REST client using Spring's {@link RestTemplate} and the newer {@link RestClient}.
 *
 * <p>Demonstrates two approaches:
 * <ul>
 *   <li>{@link RestTemplate} — classic synchronous client (still widely used)</li>
 *   <li>{@link RestClient} — modern fluent API introduced in Spring 6.1 (preferred for new code)</li>
 * </ul>
 *
 * <p>Both are synchronous and blocking. For non-blocking I/O, see {@link ProductWebClientExample}.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals") // Example code
public class ProductRestTemplateClient {

    private static final Logger log = LoggerFactory.getLogger(ProductRestTemplateClient.class);

    private final RestTemplate restTemplate;
    private final RestClient restClient;
    private final String baseUrl;

    /**
     * Creates REST clients targeting the given base URL.
     *
     * @param baseUrl the API base URL (e.g. {@code http://localhost:8080})
     */
    public ProductRestTemplateClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    // ── RestTemplate examples ──────────────────────────────────────────

    /**
     * GET all products using RestTemplate with ParameterizedTypeReference for generic lists.
     *
     * @return list of all products
     */
    public List<ProductResponse> listWithRestTemplate() {
        var request = RequestEntity.get(baseUrl + "/api/v1/products").accept(MediaType.APPLICATION_JSON).build();
        var response = restTemplate.exchange(request, new ParameterizedTypeReference<List<ProductResponse>>() { });
        log.info("Listed {} products (RestTemplate)", response.getBody().size());
        return response.getBody();
    }

    /**
     * GET single product by ID using RestTemplate.
     *
     * @param id the product UUID
     * @return the matching product response
     */
    public ProductResponse getWithRestTemplate(UUID id) {
        return restTemplate.getForObject(baseUrl + "/api/v1/products/{id}", ProductResponse.class, id);
    }

    /**
     * POST to create a product using RestTemplate. Returns the created resource.
     *
     * @param request the product creation request
     * @return the created product response
     */
    public ProductResponse createWithRestTemplate(ProductRequest request) {
        var response = restTemplate.postForEntity(baseUrl + "/api/v1/products", request, ProductResponse.class);
        log.info("Created product at {} (RestTemplate)", response.getHeaders().getLocation());
        return response.getBody();
    }

    /**
     * PUT to update a product using RestTemplate.
     *
     * @param id      the product UUID
     * @param request the updated product data
     */
    public void updateWithRestTemplate(UUID id, ProductRequest request) {
        restTemplate.put(baseUrl + "/api/v1/products/{id}", request, id);
    }

    /**
     * DELETE a product using RestTemplate.
     *
     * @param id the product UUID
     */
    public void deleteWithRestTemplate(UUID id) {
        restTemplate.delete(baseUrl + "/api/v1/products/{id}", id);
    }

    // ── RestClient examples (Spring 6.1+) ──────────────────────────────

    /**
     * GET all products using the modern RestClient fluent API.
     *
     * @return list of all products
     */
    public List<ProductResponse> listWithRestClient() {
        return restClient.get()
                .uri("/api/v1/products")
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }

    /**
     * POST to create a product using RestClient.
     *
     * @param request the product creation request
     * @return the created product response
     */
    public ProductResponse createWithRestClient(ProductRequest request) {
        return restClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ProductResponse.class);
    }

    /**
     * GET single product using RestClient with error handling.
     *
     * @param id the product UUID
     * @return the matching product response
     * @throws ResourceNotFoundException if the product is not found (404)
     */
    public ProductResponse getWithRestClient(UUID id) {
        return restClient.get()
                .uri("/api/v1/products/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    throw new ResourceNotFoundException("Product not found: " + id);
                })
                .body(ProductResponse.class);
    }
}
