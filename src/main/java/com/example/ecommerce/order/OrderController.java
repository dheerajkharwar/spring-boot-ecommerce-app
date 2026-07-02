package com.example.ecommerce.order;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public OrderController(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderDto> orders() {
        return orderRepository.findAll().stream()
                .map(OrderDto::from)
                .toList();
    }

    @GetMapping("/me")
    public List<OrderDto> myOrders(Authentication authentication) {
        return orderService.getCustomerOrders(authentication.getName());
    }

    @PostMapping
    public OrderDto checkout(@Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(request);
    }

    @PatchMapping("/{id}/status")
    public OrderDto updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateStatus(id, request.status());
    }
}
