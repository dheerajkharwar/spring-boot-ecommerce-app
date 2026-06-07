package com.example.ecommerce.order;

import java.math.BigDecimal;

public record OrderItemDto(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {

    public static OrderItemDto from(OrderLineItem item) {
        return new OrderItemDto(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
