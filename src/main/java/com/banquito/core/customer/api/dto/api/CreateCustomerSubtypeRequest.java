package com.banquito.core.customer.api.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerSubtypeRequest(
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "code debe usar mayúsculas, números o guion bajo")
        String code,
        @NotBlank @Size(max = 15) String customerType,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 300) String description
) {}
