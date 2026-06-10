package com.banquito.core.customer.domain.enums;

import lombok.Getter;

@Getter
public enum EstadoRelacionClienteEnum {
    ACTIVA("ACTIVA"),
    INACTIVA("INACTIVA"),
    REVOCADA("REVOCADA"),
    VENCIDA("VENCIDA");

    private final String value;

    EstadoRelacionClienteEnum(String value) {
        this.value = value;
    }
}
