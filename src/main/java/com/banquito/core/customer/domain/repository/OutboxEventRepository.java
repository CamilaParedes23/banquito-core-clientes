package com.banquito.core.customer.domain.repository;

import com.banquito.core.customer.domain.enums.EstadoOutboxEventEnum;
import com.banquito.core.customer.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByEstadoOrderByFechaCreacionAsc(EstadoOutboxEventEnum estado);
}
