package com.example.ecommerce.order;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerDetailsRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotBlank String phone,
        @NotBlank String addressLine,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String postalCode
) {

    CustomerDetails toEntity() {
        CustomerDetails details = new CustomerDetails();
        details.setFullName(fullName);
        details.setEmail(email);
        details.setPhone(phone);
        details.setAddressLine(addressLine);
        details.setCity(city);
        details.setState(state);
        details.setPostalCode(postalCode);
        return details;
    }
}
