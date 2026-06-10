package com.banquito.core.customer.application.service;

import com.banquito.core.customer.domain.model.OutboxEvent;
import com.banquito.core.customer.domain.repository.OutboxEventRepository;
import com.banquito.core.customer.shared.tracing.CorrelationIdHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository repository;

    public void registrarEvento(String tipoEvento, String agregadoTipo, String agregadoId, String payloadJson) {
        repository.save(OutboxEvent.crear(CorrelationIdHolder.get(), tipoEvento, agregadoTipo, agregadoId, payloadJson));
    }
}
