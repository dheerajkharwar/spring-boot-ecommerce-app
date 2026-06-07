package com.example.ecommerce.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.ecommerce.common.ResourceNotFoundException;
import com.example.ecommerce.product.Product;
import com.example.ecommerce.product.ProductRepository;

@Service
public class CartService {

    private final ProductRepository productRepository;
    private final Map<String, Map<Long, Integer>> carts = new ConcurrentHashMap<>();

    public CartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public CartDto getCart(String cartId) {
        Map<Long, Integer> items = carts.getOrDefault(cartId, Map.of());
        ArrayList<CartItemDto> itemDtos = new ArrayList<>();

        for (Map.Entry<Long, Integer> item : items.entrySet()) {
            Product product = productRepository.findById(item.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getKey()));
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getValue()));
            itemDtos.add(new CartItemDto(
                    product.getId(),
                    product.getName(),
                    product.getBrand(),
                    product.getImageUrl(),
                    product.getPrice(),
                    item.getValue(),
                    lineTotal
            ));
        }

        BigDecimal subtotal = itemDtos.stream()
                .map(CartItemDto::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int itemCount = itemDtos.stream().mapToInt(CartItemDto::quantity).sum();

        return new CartDto(cartId, itemDtos, itemCount, subtotal);
    }

    public CartDto addItem(String cartId, AddCartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));
        if (request.quantity() > product.getStock()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }

        carts.computeIfAbsent(cartId, ignored -> new ConcurrentHashMap<>())
                .merge(request.productId(), request.quantity(), Integer::sum);
        return getCart(cartId);
    }

    public CartDto updateItem(String cartId, Long productId, int quantity) {
        Map<Long, Integer> items = carts.computeIfAbsent(cartId, ignored -> new ConcurrentHashMap<>());
        if (quantity <= 0) {
            items.remove(productId);
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
            if (quantity > product.getStock()) {
                throw new IllegalArgumentException("Requested quantity exceeds available stock");
            }
            items.put(productId, quantity);
        }
        return getCart(cartId);
    }

    public void clear(String cartId) {
        carts.remove(cartId);
    }
}
