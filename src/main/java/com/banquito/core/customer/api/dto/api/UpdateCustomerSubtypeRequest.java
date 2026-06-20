package com.banquito.core.customer.api.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerSubtypeRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 300) String description
) {}
