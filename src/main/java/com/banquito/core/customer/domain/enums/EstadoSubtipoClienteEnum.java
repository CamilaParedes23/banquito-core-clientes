package com.banquito.core.customer.domain.enums;

import lombok.Getter;

@Getter
public enum EstadoSubtipoClienteEnum {
    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO");

    private final String value;

    EstadoSubtipoClienteEnum(String value) {
        this.value = value;
    }
}
