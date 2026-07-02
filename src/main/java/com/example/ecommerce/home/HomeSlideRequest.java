package com.example.ecommerce.home;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HomeSlideRequest(
        @NotBlank String title,
        @NotBlank @Size(max = 600) String subtitle,
        @NotBlank String imageUrl,
        @Min(1) @Max(5) int position,
        boolean active
) {
}
