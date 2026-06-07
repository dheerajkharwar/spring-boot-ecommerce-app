package com.example.ecommerce.dashboard;

import java.math.BigDecimal;
import java.util.List;

import com.example.ecommerce.product.ProductDto;

public record DashboardSummary(
        BigDecimal revenue,
        long orders,
        long customers,
        long products,
        List<ProductDto> lowStockProducts
) {
}
