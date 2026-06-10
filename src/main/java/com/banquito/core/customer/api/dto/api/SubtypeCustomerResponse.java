package com.banquito.core.customer.api.dto.api;

public record SubtypeCustomerResponse(
        Integer id,
        String code,
        String customerType,
        String name,
        String description,
        String status
) {}
