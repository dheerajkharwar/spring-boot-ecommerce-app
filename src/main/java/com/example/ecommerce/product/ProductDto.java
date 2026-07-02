package com.example.ecommerce.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(
        Long id,
        String sku,
        String name,
        String slug,
        String brand,
        String description,
        Category category,
        String imageUrl,
        List<String> imageUrls,
        BigDecimal price,
        int stock,
        double rating,
        boolean featured
) {

    public static ProductDto from(Product product) {
        return new ProductDto(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getBrand(),
                product.getDescription(),
                product.getCategory(),
                product.getImageUrl(),
                product.getImageUrls().isEmpty() ? List.of(product.getImageUrl()) : product.getImageUrls(),
                product.getPrice(),
                product.getStock(),
                product.getRating(),
                product.isFeatured()
        );
    }
}
