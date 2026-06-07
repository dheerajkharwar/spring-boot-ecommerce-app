package com.example.ecommerce.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/carts/{cartId}")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartDto getCart(@PathVariable String cartId) {
        return cartService.getCart(cartId);
    }

    @PostMapping("/items")
    public CartDto addItem(@PathVariable String cartId, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(cartId, request);
    }

    @PutMapping("/items/{productId}")
    public CartDto updateItem(@PathVariable String cartId, @PathVariable Long productId,
            @Min(0) @RequestBody int quantity) {
        return cartService.updateItem(cartId, productId, quantity);
    }

    @DeleteMapping
    public void clearCart(@PathVariable String cartId) {
        cartService.clear(cartId);
    }
}
