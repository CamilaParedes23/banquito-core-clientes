package com.banquito.core.customer.domain.model;

import com.banquito.core.customer.domain.enums.EstadoSubtipoClienteEnum;
import com.banquito.core.customer.domain.enums.TipoClienteEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "SUBTIPO_CLIENTE")
public class SubtipoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "CODIGO", length = 30, nullable = false)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CLIENTE", length = 15, nullable = false)
    private TipoClienteEnum tipoCliente;

    @Column(name = "NOMBRE", length = 100, nullable = false)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 300)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 15, nullable = false)
    private EstadoSubtipoClienteEnum estado;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public SubtipoCliente() {}

    public SubtipoCliente(Integer id) { this.id = id; }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaCreacion == null) fechaCreacion = now;
        if (fechaActualizacion == null) fechaActualizacion = now;
        if (estado == null) estado = EstadoSubtipoClienteEnum.ACTIVO;
    }

    @PreUpdate
    public void preUpdate() { fechaActualizacion = LocalDateTime.now(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubtipoCliente that)) return false;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "SubtipoCliente{" + "id=" + id + ", codigo='" + codigo + '\'' + ", tipoCliente=" + tipoCliente + ", estado=" + estado + '}';
    }
}
