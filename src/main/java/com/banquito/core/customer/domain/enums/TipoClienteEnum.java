package com.banquito.core.customer.domain.enums;

import lombok.Getter;

@Getter
public enum TipoClienteEnum {
    NATURAL("NATURAL"),
    JURIDICO("JURIDICO");

    private final String value;

    TipoClienteEnum(String value) {
        this.value = value;
    }
}
