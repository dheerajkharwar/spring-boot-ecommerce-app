package com.example.ecommerce.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotBlank String cartId,
        @Valid @NotNull CustomerDetailsRequest customer,
        @NotNull PaymentMethod paymentMethod
) {
}
