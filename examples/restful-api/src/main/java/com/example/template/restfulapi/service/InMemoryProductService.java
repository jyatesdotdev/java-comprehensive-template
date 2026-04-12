package com.example.template.restfulapi.service;

import com.example.template.restfulapi.domain.Product;
import com.example.template.restfulapi.dto.ProductRequest;
import com.example.template.restfulapi.exception.ResourceNotFoundException;
import com.example.template.restfulapi.mapper.ProductMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * In-memory implementation of {@link ProductService}.
 *
 * <p>Uses a {@link ConcurrentHashMap} as a simple store so the example
 * runs without a database. Replace with a JPA repository in production.
 */
@Service
public class InMemoryProductService implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(InMemoryProductService.class);
    private final Map<UUID, Product> store = new ConcurrentHashMap<>();

    @Override
    public List<Product> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Product findById(UUID id) {
        Product product = store.get(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        return product;
    }

    @Override
    public Product create(ProductRequest request) {
        var product = new Product(UUID.randomUUID(), request.name(), request.description(), request.price());
        store.put(product.getId(), product);
        log.info("Created product {}", product.getId());
        return product;
    }

    @Override
    public Product update(UUID id, ProductRequest request) {
        Product product = findById(id);
        ProductMapper.updateEntity(product, request);
        product.setUpdatedAt(Instant.now());
        log.info("Updated product {}", id);
        return product;
    }

    @Override
    public void delete(UUID id) {
        if (store.remove(id) == null) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        log.info("Deleted product {}", id);
    }
}
