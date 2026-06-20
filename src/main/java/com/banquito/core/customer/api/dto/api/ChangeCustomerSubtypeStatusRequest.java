package com.banquito.core.customer.api.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeCustomerSubtypeStatusRequest(
        @NotBlank @Size(max = 15) String status
) {}
