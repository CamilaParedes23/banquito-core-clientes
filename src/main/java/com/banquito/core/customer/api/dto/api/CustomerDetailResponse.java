package com.banquito.core.customer.api.dto.api;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerDetailResponse(
        String customerUuid,
        String identityUuid,
        String subtypeCode,
        String customerType,
        String identificationType,
        String identification,
        String email,
        String mobilePhone,
        String address,
        String status,
        Boolean massPaymentsEnabled,
        LocalDateTime registrationDate,
        NaturalPersonData naturalPerson,
        LegalPersonData legalPerson
) {
    public record NaturalPersonData(
            String names,
            String lastNames,
            LocalDate birthDate,
            String gender,
            String nationality
    ) {}

    public record LegalPersonData(
            String businessName,
            String tradeName,
            LocalDate incorporationDate,
            String economicActivity,
            String legalRepresentativeUuid,
            String legalRepresentativeIdentification
    ) {}
}
