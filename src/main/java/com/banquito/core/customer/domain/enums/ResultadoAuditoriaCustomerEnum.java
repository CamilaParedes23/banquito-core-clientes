package com.banquito.core.customer.domain.enums;

import lombok.Getter;

@Getter
public enum ResultadoAuditoriaCustomerEnum {
    OK("OK"),
    ERROR("ERROR"),
    DENEGADO("DENEGADO");

    private final String value;

    ResultadoAuditoriaCustomerEnum(String value) {
        this.value = value;
    }
}
