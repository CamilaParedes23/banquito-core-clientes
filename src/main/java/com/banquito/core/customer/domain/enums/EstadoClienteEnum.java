package com.banquito.core.customer.domain.enums;

import lombok.Getter;

@Getter
public enum EstadoClienteEnum {
    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO"),
    BLOQUEADO("BLOQUEADO"),
    SUSPENDIDO("SUSPENDIDO");

    private final String value;

    EstadoClienteEnum(String value) {
        this.value = value;
    }
}
