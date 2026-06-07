package com.example.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void returnsSeededProducts() {
        assertThat(productRepository.findAll())
                .hasSizeGreaterThan(3)
                .extracting(Product::getName)
                .contains("Arc Wireless Headphones");
    }

    @Test
    void filtersProductsByCategory() {
        assertThat(productRepository.search(Category.ELECTRONICS, null))
                .isNotEmpty()
                .allMatch(product -> product.getCategory() == Category.ELECTRONICS);
    }
}
