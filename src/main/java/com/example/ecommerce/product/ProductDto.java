package com.example.ecommerce.product;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String sku,
        String name,
        String slug,
        String brand,
        String description,
        Category category,
        String imageUrl,
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
                product.getPrice(),
                product.getStock(),
                product.getRating(),
                product.isFeatured()
        );
    }
}
