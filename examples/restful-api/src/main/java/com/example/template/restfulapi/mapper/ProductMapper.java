package com.example.template.restfulapi.mapper;

import com.example.template.restfulapi.domain.Product;
import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.dto.ProductResponse;

/** Maps between domain entities and DTOs. */
public final class ProductMapper {

    private ProductMapper() { }

    /**
     * Converts a domain entity to a response DTO.
     *
     * @param p the product entity
     * @return product response DTO
     */
    public static ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCreatedAt(),
                p.getUpdatedAt());
    }

    /**
     * Applies request fields to an existing entity (partial update).
     *
     * @param product the entity to update
     * @param request the incoming request data
     */
    public static void updateEntity(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
    }
}
