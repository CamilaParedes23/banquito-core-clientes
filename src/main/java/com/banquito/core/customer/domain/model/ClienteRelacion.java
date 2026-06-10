package com.banquito.core.customer.domain.model;

import com.banquito.core.customer.domain.enums.EstadoRelacionClienteEnum;
import com.banquito.core.customer.domain.enums.TipoRelacionClienteEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "CLIENTE_RELACION")
public class ClienteRelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "UUID_RELACION", length = 36, nullable = false)
    private String uuidRelacion;

    @Column(name = "UUID_CLIENTE_ORIGEN", length = 36, nullable = false)
    private String uuidClienteOrigen;

    @Column(name = "UUID_CLIENTE_RELACIONADO", length = 36, nullable = false)
    private String uuidClienteRelacionado;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_RELACION", length = 40, nullable = false)
    private TipoRelacionClienteEnum tipoRelacion;

    @Column(name = "IDENTIFICACION_RELACIONADA", length = 20)
    private String identificacionRelacionada;

    @Column(name = "NOMBRE_RELACIONADO", length = 180)
    private String nombreRelacionado;

    @Column(name = "FECHA_INICIO", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "FECHA_FIN")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 15, nullable = false)
    private EstadoRelacionClienteEnum estado;

    @Column(name = "OBSERVACION", length = 500)
    private String observacion;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public ClienteRelacion() {}

    public ClienteRelacion(Long id) { this.id = id; }

    public static ClienteRelacion crear(String uuidClienteOrigen, String uuidClienteRelacionado,
                                        TipoRelacionClienteEnum tipoRelacion, String identificacionRelacionada,
                                        String nombreRelacionado, LocalDate fechaInicio, String observacion) {
        ClienteRelacion relacion = new ClienteRelacion();
        relacion.uuidRelacion = UUID.randomUUID().toString();
        relacion.uuidClienteOrigen = uuidClienteOrigen;
        relacion.uuidClienteRelacionado = uuidClienteRelacionado;
        relacion.tipoRelacion = tipoRelacion;
        relacion.identificacionRelacionada = identificacionRelacionada;
        relacion.nombreRelacionado = nombreRelacionado;
        relacion.fechaInicio = fechaInicio;
        relacion.observacion = observacion;
        relacion.estado = EstadoRelacionClienteEnum.ACTIVA;
        return relacion;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (uuidRelacion == null) uuidRelacion = UUID.randomUUID().toString();
        if (estado == null) estado = EstadoRelacionClienteEnum.ACTIVA;
        if (fechaCreacion == null) fechaCreacion = now;
        if (fechaActualizacion == null) fechaActualizacion = now;
    }

    @PreUpdate
    public void preUpdate() { fechaActualizacion = LocalDateTime.now(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClienteRelacion that)) return false;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "ClienteRelacion{" + "id=" + id + ", uuidClienteOrigen='" + uuidClienteOrigen + '\'' + ", tipoRelacion=" + tipoRelacion + ", estado=" + estado + '}';
    }
}
