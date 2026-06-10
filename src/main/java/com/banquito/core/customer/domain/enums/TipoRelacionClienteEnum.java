package com.banquito.core.customer.domain.enums;

import lombok.Getter;

@Getter
public enum TipoRelacionClienteEnum {
    REPRESENTANTE_LEGAL("REPRESENTANTE_LEGAL"),
    APODERADO("APODERADO"),
    CONTACTO_AUTORIZADO("CONTACTO_AUTORIZADO"),
    BENEFICIARIO_FINAL("BENEFICIARIO_FINAL"),
    ACCIONISTA("ACCIONISTA");

    private final String value;

    TipoRelacionClienteEnum(String value) {
        this.value = value;
    }
}
