package com.banquito.core.customer.api.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 25) String mobilePhone,
        @NotBlank @Size(max = 300) String address
) {}
