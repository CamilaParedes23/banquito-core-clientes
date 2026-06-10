package com.banquito.core.customer.domain.enums;

import lombok.Getter;

@Getter
public enum TipoIdentificacionEnum {
    CEDULA("CEDULA"),
    RUC("RUC"),
    PASAPORTE("PASAPORTE");

    private final String value;

    TipoIdentificacionEnum(String value) {
        this.value = value;
    }
}
