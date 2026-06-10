package com.banquito.core.customer.domain.model;

import com.banquito.core.customer.domain.enums.EstadoClienteEnum;
import com.banquito.core.customer.domain.enums.TipoClienteEnum;
import com.banquito.core.customer.domain.enums.TipoIdentificacionEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "CLIENTE")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "UUID_CLIENTE", length = 36, nullable = false)
    private String uuidCliente;

    @Column(name = "UUID_IDENTIDAD_PRINCIPAL", length = 36)
    private String uuidIdentidadPrincipal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBTIPO_CLIENTE_ID", nullable = false)
    private SubtipoCliente subtipoCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CLIENTE", length = 15, nullable = false)
    private TipoClienteEnum tipoCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_IDENTIFICACION", length = 15, nullable = false)
    private TipoIdentificacionEnum tipoIdentificacion;

    @Column(name = "IDENTIFICACION", length = 20, nullable = false)
    private String identificacion;

    @Column(name = "EMAIL", length = 160, nullable = false)
    private String email;

    @Column(name = "TELEFONO_MOVIL", length = 25, nullable = false)
    private String telefonoMovil;

    @Column(name = "DIRECCION", length = 300, nullable = false)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20, nullable = false)
    private EstadoClienteEnum estado;

    @Column(name = "ACTIVO_PAGOS_MASIVOS", nullable = false)
    private Boolean activoPagosMasivos;

    @Column(name = "FECHA_ALTA", nullable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public Cliente() {}

    public Cliente(Long id) { this.id = id; }

    public static Cliente crear(SubtipoCliente subtipoCliente, TipoClienteEnum tipoCliente,
                                TipoIdentificacionEnum tipoIdentificacion, String identificacion,
                                String email, String telefonoMovil, String direccion,
                                String uuidIdentidadPrincipal, boolean activoPagosMasivos) {
        Cliente cliente = new Cliente();
        cliente.uuidCliente = UUID.randomUUID().toString();
        cliente.subtipoCliente = subtipoCliente;
        cliente.tipoCliente = tipoCliente;
        cliente.tipoIdentificacion = tipoIdentificacion;
        cliente.identificacion = identificacion;
        cliente.email = email;
        cliente.telefonoMovil = telefonoMovil;
        cliente.direccion = direccion;
        cliente.uuidIdentidadPrincipal = uuidIdentidadPrincipal;
        cliente.activoPagosMasivos = activoPagosMasivos;
        cliente.estado = EstadoClienteEnum.ACTIVO;
        return cliente;
    }

    public void cambiarEstado(EstadoClienteEnum nuevoEstado) { this.estado = nuevoEstado; }

    public void cambiarPagosMasivos(boolean activo) { this.activoPagosMasivos = activo; }

    public void actualizarDatosContacto(String email, String telefonoMovil, String direccion) {
        this.email = email;
        this.telefonoMovil = telefonoMovil;
        this.direccion = direccion;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (uuidCliente == null) uuidCliente = UUID.randomUUID().toString();
        if (estado == null) estado = EstadoClienteEnum.ACTIVO;
        if (activoPagosMasivos == null) activoPagosMasivos = false;
        if (fechaAlta == null) fechaAlta = now;
        if (fechaActualizacion == null) fechaActualizacion = now;
    }

    @PreUpdate
    public void preUpdate() { fechaActualizacion = LocalDateTime.now(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente that)) return false;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "Cliente{" + "id=" + id + ", uuidCliente='" + uuidCliente + '\'' + ", tipoCliente=" + tipoCliente + ", identificacion='" + identificacion + '\'' + ", estado=" + estado + '}';
    }
}
