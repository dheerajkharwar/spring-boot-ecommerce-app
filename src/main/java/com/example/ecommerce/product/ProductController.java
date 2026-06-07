package com.example.ecommerce.product;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<ProductDto> products(@RequestParam(required = false) Category category,
            @RequestParam(required = false) String query) {
        return productService.getProducts(category, query);
    }

    @GetMapping("/products/featured")
    public List<ProductDto> featuredProducts() {
        return productService.getFeaturedProducts();
    }

    @GetMapping("/products/{slug}")
    public ProductDto product(@PathVariable String slug) {
        return productService.getProductBySlug(slug);
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return productService.getCategories();
    }
}
