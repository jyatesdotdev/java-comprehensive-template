package com.example.template.restfulapi.client;

import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.dto.ProductResponse;
import com.example.template.restfulapi.exception.ClientException;
import com.example.template.restfulapi.exception.ResourceNotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST client using the Jakarta RESTful Web Services (JAX-RS) Client API.
 *
 * <p>Uses Jersey as the JAX-RS reference implementation. This approach is framework-agnostic —
 * works outside Spring and in Jakarta EE containers.
 *
 * <p>Demonstrates:
 *
 * <ul>
 *   <li>JAX-RS {@link Client} lifecycle (create, use, close)
 *   <li>Fluent target/path/request builder
 *   <li>Generic type handling with {@link GenericType}
 *   <li>Timeout configuration
 *   <li>Response status checking on every method
 * </ul>
 *
 * <p>Implements {@link AutoCloseable} — use with try-with-resources.
 */
public class ProductJaxRsClient implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(ProductJaxRsClient.class);

  private final Client client;
  private final String baseUrl;

  /**
   * Creates a JAX-RS client targeting the given base URL.
   *
   * @param baseUrl the API base URL (e.g. {@code http://localhost:8080})
   */
  public ProductJaxRsClient(String baseUrl) {
    this(
        baseUrl,
        ClientBuilder.newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build());
  }

  /**
   * Creates a client around an existing JAX-RS {@link Client} (tests and custom factories).
   *
   * @param baseUrl the API base URL
   * @param client the configured JAX-RS client
   */
  ProductJaxRsClient(String baseUrl, Client client) {
    this.baseUrl = baseUrl;
    this.client = client;
  }

  /**
   * GET all products.
   *
   * @return list of all products
   */
  public List<ProductResponse> list() {
    try (Response response =
        client.target(baseUrl).path("api/v1/products").request(MediaType.APPLICATION_JSON).get()) {
      requireStatus(response, null, 200);
      List<ProductResponse> body = response.readEntity(new GenericType<>() {});
      return body == null ? List.of() : body;
    }
  }

  /**
   * GET single product with manual status check.
   *
   * @param id the product UUID
   * @return the matching product response
   * @throws ResourceNotFoundException if the product is not found (404)
   */
  public ProductResponse get(UUID id) {
    try (Response response =
        client
            .target(baseUrl)
            .path("api/v1/products/{id}")
            .resolveTemplate("id", id)
            .request(MediaType.APPLICATION_JSON)
            .get()) {
      requireStatus(response, id, 200);
      return response.readEntity(ProductResponse.class);
    }
  }

  /**
   * POST to create a product.
   *
   * @param request the product creation request
   * @return the created product response
   */
  public ProductResponse create(ProductRequest request) {
    try (Response response =
        client
            .target(baseUrl)
            .path("api/v1/products")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(request))) {
      requireStatus(response, null, 200, 201);
      log.info("Created product, location: {}", response.getLocation());
      return response.readEntity(ProductResponse.class);
    }
  }

  /**
   * PUT to update a product.
   *
   * @param id the product UUID
   * @param request the updated product data
   * @return the updated product response
   */
  public ProductResponse update(UUID id, ProductRequest request) {
    try (Response response =
        client
            .target(baseUrl)
            .path("api/v1/products/{id}")
            .resolveTemplate("id", id)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(request))) {
      requireStatus(response, id, 200);
      return response.readEntity(ProductResponse.class);
    }
  }

  /**
   * DELETE a product.
   *
   * @param id the product UUID
   * @throws ClientException if the server does not return 204
   */
  public void delete(UUID id) {
    try (Response response =
        client
            .target(baseUrl)
            .path("api/v1/products/{id}")
            .resolveTemplate("id", id)
            .request()
            .delete()) {
      requireStatus(response, id, 204);
    }
  }

  @Override
  public void close() {
    client.close();
  }

  private static void requireStatus(Response response, UUID id, int... okStatuses) {
    int status = response.getStatus();
    for (int ok : okStatuses) {
      if (status == ok) {
        return;
      }
    }
    if (status == 404) {
      throw new ResourceNotFoundException("Product not found: " + id);
    }
    throw new ClientException("Request failed with status: " + status);
  }
}
