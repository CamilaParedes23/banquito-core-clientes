package com.banquito.core.customer.api.dto.api;

import java.util.List;

public record CustomerListResponse(
        long total,
        int page,
        int size,
        int totalPages,
        List<CustomerBasicResponse> customers
) {}
