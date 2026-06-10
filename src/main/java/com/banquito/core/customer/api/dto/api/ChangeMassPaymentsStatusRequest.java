package com.banquito.core.customer.api.dto.api;

import jakarta.validation.constraints.NotNull;

public record ChangeMassPaymentsStatusRequest(
        @NotNull Boolean enabled
) {}
