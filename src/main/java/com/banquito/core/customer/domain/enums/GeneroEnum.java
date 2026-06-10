package com.banquito.core.customer.domain.enums;

import lombok.Getter;

@Getter
public enum GeneroEnum {
    MASCULINO("MASCULINO"),
    FEMENINO("FEMENINO"),
    OTRO("OTRO"),
    NO_DECLARA("NO_DECLARA");

    private final String value;

    GeneroEnum(String value) {
        this.value = value;
    }
}
