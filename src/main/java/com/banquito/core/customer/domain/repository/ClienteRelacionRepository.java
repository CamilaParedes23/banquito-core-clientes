package com.banquito.core.customer.domain.repository;

import com.banquito.core.customer.domain.enums.EstadoRelacionClienteEnum;
import com.banquito.core.customer.domain.enums.TipoRelacionClienteEnum;
import com.banquito.core.customer.domain.model.ClienteRelacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRelacionRepository extends JpaRepository<ClienteRelacion, Long> {
    Optional<ClienteRelacion> findByUuidRelacion(String uuidRelacion);
    List<ClienteRelacion> findByUuidClienteOrigenAndEstadoOrderByFechaInicioDesc(String uuidClienteOrigen, EstadoRelacionClienteEnum estado);
    List<ClienteRelacion> findByUuidClienteOrigenAndTipoRelacionAndEstadoOrderByFechaInicioDesc(String uuidClienteOrigen, TipoRelacionClienteEnum tipoRelacion, EstadoRelacionClienteEnum estado);
}
