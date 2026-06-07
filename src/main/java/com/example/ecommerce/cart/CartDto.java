package com.example.ecommerce.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(
        String id,
        List<CartItemDto> items,
        int itemCount,
        BigDecimal subtotal
) {
}
