package com.banquito.core.customer.domain.repository;

import com.banquito.core.customer.domain.enums.EstadoSubtipoClienteEnum;
import com.banquito.core.customer.domain.enums.TipoClienteEnum;
import com.banquito.core.customer.domain.model.SubtipoCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubtipoClienteRepository extends JpaRepository<SubtipoCliente, Integer> {
    Optional<SubtipoCliente> findByCodigo(String codigo);
    Optional<SubtipoCliente> findByCodigoAndTipoClienteAndEstado(String codigo, TipoClienteEnum tipoCliente, EstadoSubtipoClienteEnum estado);
    List<SubtipoCliente> findByTipoClienteAndEstadoOrderByNombreAsc(TipoClienteEnum tipoCliente, EstadoSubtipoClienteEnum estado);
}
