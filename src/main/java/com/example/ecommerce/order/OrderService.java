package com.example.ecommerce.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.cart.CartDto;
import com.example.ecommerce.cart.CartItemDto;
import com.example.ecommerce.cart.CartService;

@Service
public class OrderService {

    private static final BigDecimal SHIPPING_FEE = new BigDecimal("99.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.18");

    private final CartService cartService;
    private final OrderRepository orderRepository;

    public OrderService(CartService cartService, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderDto checkout(CheckoutRequest request) {
        CartDto cart = cartService.getCart(request.cartId());
        if (cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        BigDecimal tax = cart.subtotal().multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shippingFee = cart.subtotal().compareTo(new BigDecimal("5000.00")) >= 0
                ? BigDecimal.ZERO
                : SHIPPING_FEE;

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomer(request.customer().toEntity());
        order.setPaymentMethod(request.paymentMethod());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setSubtotal(cart.subtotal());
        order.setShippingFee(shippingFee);
        order.setTax(tax);
        order.setTotal(cart.subtotal().add(tax).add(shippingFee));
        order.setPlacedAt(Instant.now());

        for (CartItemDto cartItem : cart.items()) {
            OrderLineItem item = new OrderLineItem();
            item.setProductId(cartItem.productId());
            item.setProductName(cartItem.name());
            item.setQuantity(cartItem.quantity());
            item.setUnitPrice(cartItem.unitPrice());
            item.setLineTotal(cartItem.lineTotal());
            order.addItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        cartService.clear(request.cartId());
        return OrderDto.from(savedOrder);
    }
}
