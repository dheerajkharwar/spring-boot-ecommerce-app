package com.example.ecommerce.product;

import java.util.Arrays;
import java.util.List;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;

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
    public ProductDto createProduct(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        product.setCreatedAt(Instant.now());
        return ProductDto.from(productRepository.save(product));
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        applyRequest(product, request);
        return ProductDto.from(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public void seedProductsIfEmpty(List<Product> products) {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.saveAll(products);
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setSlug(normalizeSlug(request.slug(), request.name()));
        product.setBrand(request.brand().trim());
        product.setDescription(request.description().trim());
        product.setCategory(request.category());
        product.setImageUrl(request.imageUrl().trim());
        product.setImageUrls(normalizeImageUrls(request));
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setRating(request.rating());
        product.setFeatured(request.featured());
    }

    private String normalizeSlug(String slug, String name) {
        String source = slug == null || slug.isBlank() ? name : slug;
        return source.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private List<String> normalizeImageUrls(ProductRequest request) {
        List<String> imageUrls = new ArrayList<>();
        if (request.imageUrls() != null) {
            request.imageUrls().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(String::trim)
                    .limit(5)
                    .forEach(imageUrls::add);
        }
        if (imageUrls.isEmpty()) {
            imageUrls.add(request.imageUrl().trim());
        }
        return imageUrls;
    }
}
