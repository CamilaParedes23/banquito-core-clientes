package com.banquito.core.customer.application.service;

import com.banquito.core.customer.domain.enums.ResultadoAuditoriaCustomerEnum;
import com.banquito.core.customer.domain.model.AuditoriaCustomerEvento;
import com.banquito.core.customer.domain.repository.AuditoriaCustomerEventoRepository;
import com.banquito.core.customer.shared.tracing.CorrelationIdHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditoriaCustomerService {

    private final AuditoriaCustomerEventoRepository repository;

    public void registrar(String uuidUsuario, String accion, String entidad, String entidadId,
                          ResultadoAuditoriaCustomerEnum resultado, String ipOrigen, String detalleJson) {
        repository.save(AuditoriaCustomerEvento.crear(
                CorrelationIdHolder.get(), uuidUsuario, accion, entidad, entidadId, resultado, ipOrigen, detalleJson
        ));
    }
}
