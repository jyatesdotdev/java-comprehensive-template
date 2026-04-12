package com.example.template.restfulapi.service;

import com.example.template.restfulapi.domain.Product;
import com.example.template.restfulapi.dto.ProductRequest;
import java.util.List;
import java.util.UUID;

/** Service interface for product operations. Decouples controller from implementation. */
public interface ProductService {

    /**
     * Returns all products.
     *
     * @return unmodifiable list of all products
     */
    List<Product> findAll();

    /**
     * Finds a product by its unique identifier.
     *
     * @param id the product UUID
     * @return the matching product
     * @throws com.example.template.restfulapi.exception.ResourceNotFoundException if no product exists with the given ID
     */
    Product findById(UUID id);

    /**
     * Creates a new product from the given request.
     *
     * @param request product data
     * @return the newly created product with a generated ID
     */
    Product create(ProductRequest request);

    /**
     * Updates an existing product.
     *
     * @param id      the product UUID
     * @param request updated product data
     * @return the updated product
     * @throws com.example.template.restfulapi.exception.ResourceNotFoundException if no product exists with the given ID
     */
    Product update(UUID id, ProductRequest request);

    /**
     * Deletes a product by ID.
     *
     * @param id the product UUID
     * @throws com.example.template.restfulapi.exception.ResourceNotFoundException if no product exists with the given ID
     */
    void delete(UUID id);
}
