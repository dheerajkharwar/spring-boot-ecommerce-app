package com.example.ecommerce.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDto(
        Long id,
        String orderNumber,
        String customerName,
        String customerEmail,
        OrderStatus status,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal tax,
        BigDecimal total,
        Instant placedAt,
        List<OrderItemDto> items
) {

    public static OrderDto from(Order order) {
        return new OrderDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getFullName(),
                order.getCustomer().getEmail(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTax(),
                order.getTotal(),
                order.getPlacedAt(),
                order.getItems().stream().map(OrderItemDto::from).toList()
        );
    }
}
