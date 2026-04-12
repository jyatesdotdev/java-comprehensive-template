package com.example.template.restfulapi.controller;

import com.example.template.restfulapi.dto.ErrorResponse;
import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.dto.ProductResponse;
import com.example.template.restfulapi.mapper.ProductMapper;
import com.example.template.restfulapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for product CRUD operations.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Standard HTTP method mapping (GET, POST, PUT, DELETE)</li>
 *   <li>Request validation with {@code @Valid}</li>
 *   <li>Proper HTTP status codes (201 Created with Location header, 204 No Content)</li>
 *   <li>DTO separation — controllers never expose domain entities</li>
 *   <li>OpenAPI annotations for Swagger UI documentation</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product CRUD operations")
public class ProductController {

    private final ProductService productService;

    /**
     * Constructs the controller with the given service.
     *
     * @param productService the product service implementation
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Lists all products.
     *
     * @return list of product response DTOs
     */
    @Operation(summary = "List all products")
    @ApiResponse(responseCode = "200", description = "List of products")
    @GetMapping
    public List<ProductResponse> list() {
        return productService.findAll().stream().map(ProductMapper::toResponse).toList();
    }

    /**
     * Retrieves a single product by ID.
     *
     * @param id the product UUID
     * @return the matching product response
     * @throws com.example.template.restfulapi.exception.ResourceNotFoundException if not found
     */
    @Operation(summary = "Get a product by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable UUID id) {
        return ProductMapper.toResponse(productService.findById(id));
    }

    /**
     * Creates a new product.
     *
     * @param request the product creation request
     * @return 201 Created with the product response and Location header
     */
    @Operation(summary = "Create a product", description = "Creates a new product and returns it with a generated ID")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created"),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        var product = productService.create(request);
        var response = ProductMapper.toResponse(product);
        return ResponseEntity.created(URI.create("/api/v1/products/" + product.getId())).body(response);
    }

    /**
     * Updates an existing product.
     *
     * @param id      the product UUID
     * @param request the updated product data
     * @return the updated product response
     * @throws com.example.template.restfulapi.exception.ResourceNotFoundException if not found
     */
    @Operation(summary = "Update a product")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return ProductMapper.toResponse(productService.update(id, request));
    }

    /**
     * Deletes a product by ID.
     *
     * @param id the product UUID
     * @return 204 No Content on success
     * @throws com.example.template.restfulapi.exception.ResourceNotFoundException if not found
     */
    @Operation(summary = "Delete a product")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
