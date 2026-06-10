package com.banquito.core.customer.api.dto.api;

public record CustomerBasicResponse(
        String customerUuid,
        String customerType,
        String identificationType,
        String identification,
        String displayName,
        String status,
        Boolean massPaymentsEnabled
) {}
