package com.banquito.core.customer.api.dto.api;

public record CustomerBasicResponse(
        String customerUuid,
        String customerType,
        String identificationType,
        String identification,
        String displayName,
        String tradeName,
        String subtypeCode,
        String subtypeName,
        String email,
        String mobilePhone,
        String legalRepresentativeIdentification,
        String legalRepresentativeName,
        Boolean massPaymentsEnabled,
        String status
) {}
