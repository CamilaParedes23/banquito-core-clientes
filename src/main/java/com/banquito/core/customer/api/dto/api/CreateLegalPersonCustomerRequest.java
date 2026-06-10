package com.banquito.core.customer.api.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateLegalPersonCustomerRequest(
        @NotBlank @Size(max = 30) String subtypeCode,
        @Size(max = 36) String identityUuid,
        @NotBlank @Size(max = 15) String identificationType,
        @NotBlank @Size(max = 20) String identification,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 25) String mobilePhone,
        @NotBlank @Size(max = 300) String address,
        @NotBlank @Size(max = 180) String businessName,
        @Size(max = 180) String tradeName,
        @NotNull @PastOrPresent LocalDate incorporationDate,
        @Size(max = 200) String economicActivity,
        @Size(max = 36) String legalRepresentativeUuid,
        @Size(max = 20) String legalRepresentativeIdentification,
        Boolean massPaymentsEnabled
) {}
