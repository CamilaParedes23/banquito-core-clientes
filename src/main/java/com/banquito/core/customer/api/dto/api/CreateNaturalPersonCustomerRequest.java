package com.banquito.core.customer.api.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateNaturalPersonCustomerRequest(
        @NotBlank @Size(max = 30) String subtypeCode,
        @Size(max = 36) String identityUuid,
        @NotBlank @Size(max = 15) String identificationType,
        @NotBlank @Size(max = 20) String identification,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 25) String mobilePhone,
        @NotBlank @Size(max = 300) String address,
        @NotBlank @Size(max = 120) String names,
        @NotBlank @Size(max = 120) String lastNames,
        @NotNull @Past LocalDate birthDate,
        @Size(max = 20) String gender,
        @Size(max = 80) String nationality
) {}
