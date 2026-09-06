package com.example.template.restfulapi.client;

import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.dto.ProductResponse;
import com.example.template.restfulapi.exception.ClientException;
import com.example.template.restfulapi.exception.ResourceNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * REST client using Spring's {@link RestTemplate} and the newer {@link RestClient}.
 *
 * <p>Demonstrates two approaches:
 *
 * <ul>
 *   <li>{@link RestTemplate} — classic synchronous client (still widely used)
 *   <li>{@link RestClient} — modern fluent API introduced in Spring 6.1 (preferred for new code)
 * </ul>
 *
 * <p>Both are synchronous and blocking. For non-blocking I/O, see {@link ProductWebClientExample}.
 * Every call uses connect/read timeouts and maps 404 to {@link ResourceNotFoundException} and other
 * HTTP errors to {@link ClientException}.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals") // Example code
public class ProductRestTemplateClient {

  private static final Logger log = LoggerFactory.getLogger(ProductRestTemplateClient.class);
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

  private final RestTemplate restTemplate;
  private final RestClient restClient;
  private final String baseUrl;

  /**
   * Creates REST clients targeting the given base URL, with timeouts and error mapping.
   *
   * @param baseUrl the API base URL (e.g. {@code http://localhost:8080})
   */
  public ProductRestTemplateClient(String baseUrl) {
    this(baseUrl, buildRestTemplate());
  }

  private ProductRestTemplateClient(String baseUrl, RestTemplate restTemplate) {
    this(
        baseUrl,
        restTemplate,
        RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(restTemplate.getRequestFactory())
            .build());
  }

  /**
   * Creates a client with injected HTTP components (tests and custom factories).
   *
   * @param baseUrl the API base URL
   * @param restTemplate RestTemplate with timeouts and error mapping
   * @param restClient RestClient used for the fluent API examples
   */
  ProductRestTemplateClient(String baseUrl, RestTemplate restTemplate, RestClient restClient) {
    this.baseUrl = baseUrl;
    this.restTemplate = restTemplate;
    this.restClient = restClient;
  }

  private static RestTemplate buildRestTemplate() {
    return new RestTemplateBuilder()
        .setConnectTimeout(CONNECT_TIMEOUT)
        .setReadTimeout(READ_TIMEOUT)
        .errorHandler(new ProductStatusErrorHandler())
        .build();
  }

  // ── RestTemplate examples ──────────────────────────────────────────

  /**
   * GET all products using RestTemplate with ParameterizedTypeReference for generic lists.
   *
   * @return list of all products
   */
  public List<ProductResponse> listWithRestTemplate() {
    var request =
        RequestEntity.get(baseUrl + "/api/v1/products").accept(MediaType.APPLICATION_JSON).build();
    var response =
        restTemplate.exchange(request, new ParameterizedTypeReference<List<ProductResponse>>() {});
    List<ProductResponse> body = response.getBody();
    if (body == null) {
      return List.of();
    }
    log.info("Listed {} products (RestTemplate)", body.size());
    return body;
  }

  /**
   * GET single product by ID using RestTemplate.
   *
   * @param id the product UUID
   * @return the matching product response
   * @throws ResourceNotFoundException if the product is not found (404)
   * @throws ClientException if the server returns another error status
   */
  public ProductResponse getWithRestTemplate(UUID id) {
    ProductResponse body =
        restTemplate.getForObject(baseUrl + "/api/v1/products/{id}", ProductResponse.class, id);
    if (body == null) {
      throw new ClientException("Empty response for product " + id);
    }
    return body;
  }

  /**
   * POST to create a product using RestTemplate. Returns the created resource.
   *
   * @param request the product creation request
   * @return the created product response
   * @throws ClientException if the server returns an error status or an empty body
   */
  public ProductResponse createWithRestTemplate(ProductRequest request) {
    var response =
        restTemplate.postForEntity(baseUrl + "/api/v1/products", request, ProductResponse.class);
    log.info("Created product at {} (RestTemplate)", response.getHeaders().getLocation());
    ProductResponse body = response.getBody();
    if (body == null) {
      throw new ClientException("Empty response when creating product");
    }
    return body;
  }

  /**
   * PUT to update a product using RestTemplate.
   *
   * @param id the product UUID
   * @param request the updated product data
   * @throws ResourceNotFoundException if the product is not found (404)
   * @throws ClientException if the server returns another error status
   */
  public void updateWithRestTemplate(UUID id, ProductRequest request) {
    restTemplate.put(baseUrl + "/api/v1/products/{id}", request, id);
  }

  /**
   * DELETE a product using RestTemplate.
   *
   * @param id the product UUID
   * @throws ResourceNotFoundException if the product is not found (404)
   * @throws ClientException if the server returns another error status
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
    List<ProductResponse> body =
        withErrorMapping(restClient.get().uri("/api/v1/products").retrieve(), null)
            .body(new ParameterizedTypeReference<>() {});
    return body == null ? List.of() : body;
  }

  /**
   * POST to create a product using RestClient.
   *
   * @param request the product creation request
   * @return the created product response
   */
  public ProductResponse createWithRestClient(ProductRequest request) {
    ProductResponse body =
        withErrorMapping(
                restClient
                    .post()
                    .uri("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve(),
                null)
            .body(ProductResponse.class);
    if (body == null) {
      throw new ClientException("Empty response when creating product");
    }
    return body;
  }

  /**
   * GET single product using RestClient with error handling.
   *
   * @param id the product UUID
   * @return the matching product response
   * @throws ResourceNotFoundException if the product is not found (404)
   */
  public ProductResponse getWithRestClient(UUID id) {
    ProductResponse body =
        withErrorMapping(restClient.get().uri("/api/v1/products/{id}", id).retrieve(), id)
            .body(ProductResponse.class);
    if (body == null) {
      throw new ClientException("Empty response for product " + id);
    }
    return body;
  }

  private static RestClient.ResponseSpec withErrorMapping(RestClient.ResponseSpec spec, UUID id) {
    return spec.onStatus(
            status -> status.value() == 404,
            (req, resp) -> {
              throw new ResourceNotFoundException("Product not found: " + id);
            })
        .onStatus(
            HttpStatusCode::isError,
            (req, resp) -> {
              throw new ClientException(
                  "Request failed with status: " + resp.getStatusCode().value());
            });
  }

  /**
   * Maps HTTP 404 to {@link ResourceNotFoundException} and other 4xx/5xx to {@link
   * ClientException}.
   */
  static final class ProductStatusErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
      HttpStatusCode status = response.getStatusCode();
      return status.is4xxClientError() || status.is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
      int code = response.getStatusCode().value();
      if (code == 404) {
        throw new ResourceNotFoundException("Product not found");
      }
      throw new ClientException("Request failed with status: " + code);
    }
  }
}
