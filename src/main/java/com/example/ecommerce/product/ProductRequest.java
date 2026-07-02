package com.example.ecommerce.product;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String slug,
        @NotBlank String brand,
        @NotBlank @Size(max = 1000) String description,
        @NotNull Category category,
        @NotBlank String imageUrl,
        @Size(max = 5) List<@NotBlank String> imageUrls,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @Min(0) int stock,
        @Min(0) @Max(5) double rating,
        boolean featured
) {
}
