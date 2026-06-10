package com.banquito.core.customer.api.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeCustomerStatusRequest(
        @NotBlank @Size(max = 20) String status
) {}
