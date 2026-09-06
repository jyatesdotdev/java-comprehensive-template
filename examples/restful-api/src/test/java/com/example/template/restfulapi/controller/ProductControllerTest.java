package com.example.template.restfulapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.template.restfulapi.domain.Product;
import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.exception.ResourceNotFoundException;
import com.example.template.restfulapi.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-slice tests for {@link ProductController}: HTTP status codes, Location header, validation
 * failures, and 404 mapping via the {@code GlobalExceptionHandler}.
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

  private static final String BASE_PATH = "/api/v1/products";
  private static final String WIDGET_NAME = "Widget";
  private static final String WIDGET_DESCRIPTION = "A useful widget";
  private static final BigDecimal WIDGET_PRICE = new BigDecimal("29.99");
  private static final String NOT_FOUND_PREFIX = "Product not found: ";
  private static final String NAME_REQUIRED_DETAIL = "name: Name is required";
  private static final String JSON_STATUS = "$.status";
  private static final String JSON_ERROR = "$.error";
  private static final String JSON_MESSAGE = "$.message";
  private static final String JSON_DETAILS = "$.details";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ProductService productService;

  private Product sampleProduct(UUID id) {
    return new Product(id, WIDGET_NAME, WIDGET_DESCRIPTION, WIDGET_PRICE);
  }

  // ── GET (list) ─────────────────────────────────────────────────────

  @Test
  @DisplayName("GET /api/v1/products returns 200 with all products")
  void listShouldReturn200WithAllProducts() throws Exception {
    UUID id = UUID.randomUUID();
    when(productService.findAll()).thenReturn(List.of(sampleProduct(id)));

    mockMvc
        .perform(get(BASE_PATH))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(id.toString()))
        .andExpect(jsonPath("$[0].name").value(WIDGET_NAME))
        .andExpect(jsonPath("$[0].price").value(29.99));

    verify(productService).findAll();
  }

  @Test
  @DisplayName("GET /api/v1/products returns 200 with empty array when no products exist")
  void listShouldReturn200WithEmptyArrayWhenNoProducts() throws Exception {
    when(productService.findAll()).thenReturn(List.of());

    mockMvc
        .perform(get(BASE_PATH))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(productService).findAll();
  }

  // ── GET (single) ───────────────────────────────────────────────────

  @Test
  @DisplayName("GET /api/v1/products/{id} returns 200 with the product")
  void getShouldReturn200WithProduct() throws Exception {
    UUID id = UUID.randomUUID();
    when(productService.findById(id)).thenReturn(sampleProduct(id));

    mockMvc
        .perform(get(BASE_PATH + "/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value(WIDGET_NAME))
        .andExpect(jsonPath("$.description").value(WIDGET_DESCRIPTION))
        .andExpect(jsonPath("$.price").value(29.99));

    verify(productService).findById(id);
  }

  @Test
  @DisplayName("GET /api/v1/products/{id} returns 404 ErrorResponse when the product is missing")
  void getShouldReturn404WhenProductMissing() throws Exception {
    UUID id = UUID.randomUUID();
    when(productService.findById(id))
        .thenThrow(new ResourceNotFoundException(NOT_FOUND_PREFIX + id));

    mockMvc
        .perform(get(BASE_PATH + "/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath(JSON_STATUS).value(404))
        .andExpect(jsonPath(JSON_ERROR).value("Not Found"))
        .andExpect(jsonPath("$.message").value(NOT_FOUND_PREFIX + id))
        .andExpect(jsonPath("$.timestamp").exists());

    verify(productService).findById(id);
  }

  @Test
  @DisplayName("GET /api/v1/products/{id} returns 400 when the id is not a UUID")
  void getShouldReturn400WhenIdIsNotUuid() throws Exception {
    mockMvc
        .perform(get(BASE_PATH + "/not-a-uuid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath(JSON_STATUS).value(400))
        .andExpect(jsonPath(JSON_ERROR).value("Bad Request"))
        .andExpect(jsonPath(JSON_MESSAGE).value("Invalid value for parameter id"));

    verifyNoInteractions(productService);
  }

  @Test
  @DisplayName("GET /api/v1/products returns 500 ErrorResponse when the service fails")
  void listShouldReturn500WhenServiceThrows() throws Exception {
    when(productService.findAll()).thenThrow(new IllegalStateException("boom"));

    mockMvc
        .perform(get(BASE_PATH))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath(JSON_STATUS).value(500))
        .andExpect(jsonPath(JSON_ERROR).value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

    verify(productService).findAll();
  }

  // ── POST ───────────────────────────────────────────────────────────

  @Test
  @DisplayName("POST /api/v1/products returns 201 with Location header and body")
  void createShouldReturn201WithLocationHeader() throws Exception {
    UUID id = UUID.randomUUID();
    var request = new ProductRequest(WIDGET_NAME, WIDGET_DESCRIPTION, WIDGET_PRICE);
    when(productService.create(any(ProductRequest.class))).thenReturn(sampleProduct(id));

    mockMvc
        .perform(
            post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", BASE_PATH + "/" + id))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value(WIDGET_NAME));

    verify(productService).create(request);
  }

  @Test
  @DisplayName("POST with blank name returns 400 with field detail in ErrorResponse")
  void createShouldReturn400WhenNameIsBlank() throws Exception {
    var request = new ProductRequest("  ", WIDGET_DESCRIPTION, WIDGET_PRICE);

    mockMvc
        .perform(
            post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath(JSON_STATUS).value(400))
        .andExpect(jsonPath(JSON_ERROR).value("Validation Failed"))
        .andExpect(jsonPath("$.message").value("Request body has invalid fields"))
        .andExpect(jsonPath(JSON_DETAILS, hasItem(NAME_REQUIRED_DETAIL)));

    verifyNoInteractions(productService);
  }

  @Test
  @DisplayName("POST with negative price returns 400 with field detail in ErrorResponse")
  void createShouldReturn400WhenPriceIsNegative() throws Exception {
    var request = new ProductRequest(WIDGET_NAME, WIDGET_DESCRIPTION, new BigDecimal("-1.00"));

    mockMvc
        .perform(
            post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath(JSON_STATUS).value(400))
        .andExpect(jsonPath(JSON_ERROR).value("Validation Failed"))
        .andExpect(jsonPath(JSON_DETAILS, hasItem("price: Price must be positive")));

    verifyNoInteractions(productService);
  }

  @Test
  @DisplayName("POST with blank name and negative price reports both field errors")
  void createShouldReturn400WithAllFieldErrors() throws Exception {
    var request = new ProductRequest("", null, new BigDecimal("-5"));

    var responseBody =
        mockMvc
            .perform(
                post(BASE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(JSON_DETAILS, hasItem(NAME_REQUIRED_DETAIL)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(responseBody).contains(NAME_REQUIRED_DETAIL).contains("price:");
    verifyNoInteractions(productService);
  }

  @Test
  @DisplayName("POST with malformed JSON returns 400, not 500")
  void createShouldReturn400WhenJsonIsMalformed() throws Exception {
    mockMvc
        .perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON).content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath(JSON_STATUS).value(400))
        .andExpect(jsonPath(JSON_ERROR).value("Malformed JSON"));

    verifyNoInteractions(productService);
  }

  // ── PUT ────────────────────────────────────────────────────────────

  @Test
  @DisplayName("PUT /api/v1/products/{id} returns 200 with the updated product")
  void updateShouldReturn200WithUpdatedProduct() throws Exception {
    UUID id = UUID.randomUUID();
    var request = new ProductRequest("Gadget", "An updated gadget", new BigDecimal("49.99"));
    var updated = new Product(id, "Gadget", "An updated gadget", new BigDecimal("49.99"));
    when(productService.update(eq(id), any(ProductRequest.class))).thenReturn(updated);

    mockMvc
        .perform(
            put(BASE_PATH + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("Gadget"))
        .andExpect(jsonPath("$.price").value(49.99));

    verify(productService).update(id, request);
  }

  @Test
  @DisplayName("PUT /api/v1/products/{id} returns 404 when the product is missing")
  void updateShouldReturn404WhenProductMissing() throws Exception {
    UUID id = UUID.randomUUID();
    var request = new ProductRequest(WIDGET_NAME, WIDGET_DESCRIPTION, WIDGET_PRICE);
    when(productService.update(eq(id), any(ProductRequest.class)))
        .thenThrow(new ResourceNotFoundException(NOT_FOUND_PREFIX + id));

    mockMvc
        .perform(
            put(BASE_PATH + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath(JSON_STATUS).value(404))
        .andExpect(jsonPath(JSON_ERROR).value("Not Found"));

    verify(productService).update(id, request);
  }

  @Test
  @DisplayName("PUT with invalid body returns 400 without calling the service")
  void updateShouldReturn400WhenBodyInvalid() throws Exception {
    UUID id = UUID.randomUUID();
    var request = new ProductRequest("", WIDGET_DESCRIPTION, WIDGET_PRICE);

    mockMvc
        .perform(
            put(BASE_PATH + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath(JSON_DETAILS, hasItem(NAME_REQUIRED_DETAIL)));

    verifyNoInteractions(productService);
  }

  // ── DELETE ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("DELETE /api/v1/products/{id} returns 204 No Content")
  void deleteShouldReturn204() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(delete(BASE_PATH + "/" + id)).andExpect(status().isNoContent());

    verify(productService).delete(id);
  }

  @Test
  @DisplayName("DELETE /api/v1/products/{id} returns 404 when the product is missing")
  void deleteShouldReturn404WhenProductMissing() throws Exception {
    UUID id = UUID.randomUUID();
    doThrow(new ResourceNotFoundException(NOT_FOUND_PREFIX + id)).when(productService).delete(id);

    mockMvc
        .perform(delete(BASE_PATH + "/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath(JSON_STATUS).value(404))
        .andExpect(jsonPath(JSON_ERROR).value("Not Found"));

    verify(productService).delete(id);
  }
}
