package com.banquito.core.customer.api.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCustomerRelationshipRequest(
        @NotBlank @Size(max = 36) String relatedCustomerUuid,
        @NotBlank @Size(max = 40) String relationshipType,
        @Size(max = 20) String relatedIdentification,
        @Size(max = 180) String relatedName,
        @NotNull LocalDate startDate,
        @Size(max = 500) String observation
) {}
