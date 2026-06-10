package com.banquito.core.customer.domain.repository;

import com.banquito.core.customer.domain.enums.EstadoClienteEnum;
import com.banquito.core.customer.domain.enums.TipoIdentificacionEnum;
import com.banquito.core.customer.domain.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUuidCliente(String uuidCliente);
    Optional<Cliente> findByTipoIdentificacionAndIdentificacion(TipoIdentificacionEnum tipoIdentificacion, String identificacion);
    Optional<Cliente> findByIdentificacion(String identificacion);
    boolean existsByTipoIdentificacionAndIdentificacion(TipoIdentificacionEnum tipoIdentificacion, String identificacion);
    boolean existsByUuidClienteAndEstado(String uuidCliente, EstadoClienteEnum estado);
    @Query(value = """
            SELECT c.*
            FROM CLIENTE c
            LEFT JOIN CLIENTE_PERSONA_NATURAL n ON n.CLIENTE_ID = c.ID
            LEFT JOIN CLIENTE_PERSONA_JURIDICA j ON j.CLIENTE_ID = c.ID
            WHERE (:customerType IS NULL OR c.TIPO_CLIENTE = :customerType)
              AND (:status IS NULL OR c.ESTADO = :status)
              AND (
                    :search IS NULL
                    OR LOWER(c.IDENTIFICACION) LIKE CONCAT('%', :search, '%')
                    OR LOWER(c.EMAIL) LIKE CONCAT('%', :search, '%')
                    OR LOWER(c.TELEFONO_MOVIL) LIKE CONCAT('%', :search, '%')
                    OR LOWER(n.NOMBRES) LIKE CONCAT('%', :search, '%')
                    OR LOWER(n.APELLIDOS) LIKE CONCAT('%', :search, '%')
                    OR LOWER(j.RAZON_SOCIAL) LIKE CONCAT('%', :search, '%')
                    OR LOWER(j.NOMBRE_COMERCIAL) LIKE CONCAT('%', :search, '%')
              )
            ORDER BY c.FECHA_ALTA DESC, c.ID DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM CLIENTE c
            LEFT JOIN CLIENTE_PERSONA_NATURAL n ON n.CLIENTE_ID = c.ID
            LEFT JOIN CLIENTE_PERSONA_JURIDICA j ON j.CLIENTE_ID = c.ID
            WHERE (:customerType IS NULL OR c.TIPO_CLIENTE = :customerType)
              AND (:status IS NULL OR c.ESTADO = :status)
              AND (
                    :search IS NULL
                    OR LOWER(c.IDENTIFICACION) LIKE CONCAT('%', :search, '%')
                    OR LOWER(c.EMAIL) LIKE CONCAT('%', :search, '%')
                    OR LOWER(c.TELEFONO_MOVIL) LIKE CONCAT('%', :search, '%')
                    OR LOWER(n.NOMBRES) LIKE CONCAT('%', :search, '%')
                    OR LOWER(n.APELLIDOS) LIKE CONCAT('%', :search, '%')
                    OR LOWER(j.RAZON_SOCIAL) LIKE CONCAT('%', :search, '%')
                    OR LOWER(j.NOMBRE_COMERCIAL) LIKE CONCAT('%', :search, '%')
              )
            """,
            nativeQuery = true)
    Page<Cliente> searchCustomers(@Param("customerType") String customerType,
                                  @Param("status") String status,
                                  @Param("search") String search,
                                  Pageable pageable);

}
