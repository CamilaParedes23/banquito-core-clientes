package com.banquito.core.customer.api.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 25) String mobilePhone,
        @NotBlank @Size(max = 300) String address,
        @Size(max = 120) String names,
        @Size(max = 120) String lastNames,
        @Size(max = 80) String nationality,
        @Size(max = 180) String businessName,
        @Size(max = 180) String tradeName,
        @Size(max = 200) String economicActivity
) {}
