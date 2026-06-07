package com.example.ecommerce.product;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.common.ResourceNotFoundException;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProducts(Category category, String query) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return productRepository.search(category, normalizedQuery).stream()
                .map(ProductDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueOrderByRatingDesc().stream()
                .map(ProductDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(ProductDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
    }

    public List<Category> getCategories() {
        return Arrays.asList(Category.values());
    }

    @Transactional
    public void seedProductsIfEmpty(List<Product> products) {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.saveAll(products);
    }
}
