package com.example.ecommerce.cart;

import java.math.BigDecimal;

public record CartItemDto(
        Long productId,
        String name,
        String brand,
        String imageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
