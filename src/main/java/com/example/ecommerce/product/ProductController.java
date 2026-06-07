package com.example.ecommerce.product;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.common.ResourceNotFoundException;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public List<ProductDto> products(@RequestParam(required = false) Category category,
            @RequestParam(required = false) String query) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return productRepository.search(category, normalizedQuery).stream()
                .map(ProductDto::from)
                .toList();
    }

    @GetMapping("/products/featured")
    public List<ProductDto> featuredProducts() {
        return productRepository.findByFeaturedTrueOrderByRatingDesc().stream()
                .map(ProductDto::from)
                .toList();
    }

    @GetMapping("/products/{slug}")
    public ProductDto product(@PathVariable String slug) {
        return productRepository.findBySlug(slug)
                .map(ProductDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return Arrays.asList(Category.values());
    }
}
