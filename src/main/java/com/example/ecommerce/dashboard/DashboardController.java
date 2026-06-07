package com.example.ecommerce.dashboard;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.order.Order;
import com.example.ecommerce.order.OrderRepository;
import com.example.ecommerce.product.ProductDto;
import com.example.ecommerce.product.ProductRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public DashboardController(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/summary")
    public DashboardSummary summary() {
        List<Order> orders = orderRepository.findAll();
        BigDecimal revenue = orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardSummary(
                revenue,
                orders.size(),
                orders.stream().map(order -> order.getCustomer().getEmail()).distinct().count(),
                productRepository.count(),
                productRepository.findByStockLessThanEqualOrderByStockAsc(12).stream()
                        .map(ProductDto::from)
                        .toList()
        );
    }
}
