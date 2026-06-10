package com.banquito.core.customer.api.dto.api;

import java.time.LocalDate;

public record CustomerRelationshipResponse(
        String relationshipUuid,
        String originCustomerUuid,
        String relatedCustomerUuid,
        String relationshipType,
        String relatedIdentification,
        String relatedName,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String observation
) {}
