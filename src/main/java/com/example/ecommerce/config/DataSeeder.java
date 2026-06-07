package com.example.ecommerce.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ecommerce.product.Category;
import com.example.ecommerce.product.Product;
import com.example.ecommerce.product.ProductRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedCatalog(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            List<Product> products = List.of(
                    product("NMX-1001", "Nimbus Trail Runner", "nimbus-trail-runner", "AeroStep",
                            "Weather-ready running shoes with a responsive sole and recycled knit upper.",
                            Category.FOOTWEAR, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80",
                            new BigDecimal("7499.00"), 42, 4.8, true),
                    product("LUM-2024", "Luma Pro Desk Lamp", "luma-pro-desk-lamp", "Northline",
                            "Adjustable aluminum desk lamp with warm-to-cool lighting and USB-C charging.",
                            Category.HOME, "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=900&q=80",
                            new BigDecimal("3299.00"), 18, 4.6, true),
                    product("ARC-7780", "Arc Wireless Headphones", "arc-wireless-headphones", "SonicForge",
                            "Noise cancelling headphones tuned for long work sessions and low-latency calls.",
                            Category.ELECTRONICS, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80",
                            new BigDecimal("11999.00"), 25, 4.7, true),
                    product("MKT-4410", "Market Tote Pack", "market-tote-pack", "Urban Loom",
                            "Convertible tote backpack with laptop sleeve, bottle pocket, and waxed canvas finish.",
                            Category.ACCESSORIES, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=80",
                            new BigDecimal("2899.00"), 63, 4.4, false),
                    product("BRE-9021", "BrewLab Pour Over Kit", "brewlab-pour-over-kit", "Bean & Barrel",
                            "Ceramic dripper, glass server, stainless scale, and reusable filter for daily coffee.",
                            Category.KITCHEN, "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=80",
                            new BigDecimal("4599.00"), 14, 4.9, true),
                    product("STR-6112", "Strata Utility Jacket", "strata-utility-jacket", "Field Theory",
                            "Lightweight water-resistant jacket with structured pockets and breathable lining.",
                            Category.APPAREL, "https://images.unsplash.com/photo-1520975954732-35dd22299614?auto=format&fit=crop&w=900&q=80",
                            new BigDecimal("6499.00"), 31, 4.5, false),
                    product("PAD-3388", "Creator Sketch Tablet", "creator-sketch-tablet", "PixelSmith",
                            "Pressure-sensitive drawing tablet for designers, students, and digital artists.",
                            Category.ELECTRONICS, "https://images.unsplash.com/photo-1587302912306-cf1ed9c33146?auto=format&fit=crop&w=900&q=80",
                            new BigDecimal("8999.00"), 11, 4.3, false),
                    product("ZEN-5120", "Zen Sleep Diffuser", "zen-sleep-diffuser", "CalmHouse",
                            "Ultrasonic diffuser with timer modes, amber night light, and ceramic cover.",
                            Category.HOME, "https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=900&q=80",
                            new BigDecimal("2199.00"), 7, 4.2, false)
            );

            productRepository.saveAll(products);
        };
    }

    private Product product(String sku, String name, String slug, String brand, String description,
            Category category, String imageUrl, BigDecimal price, int stock, double rating, boolean featured) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setSlug(slug);
        product.setBrand(brand);
        product.setDescription(description);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setPrice(price);
        product.setStock(stock);
        product.setRating(rating);
        product.setFeatured(featured);
        product.setCreatedAt(Instant.now());
        return product;
    }
}
