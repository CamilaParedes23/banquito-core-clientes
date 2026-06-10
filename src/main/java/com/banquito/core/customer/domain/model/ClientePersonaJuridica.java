package com.banquito.core.customer.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "CLIENTE_PERSONA_JURIDICA")
public class ClientePersonaJuridica {

    @Id
    @Column(name = "CLIENTE_ID", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "CLIENTE_ID", nullable = false)
    private Cliente cliente;

    @Column(name = "RAZON_SOCIAL", length = 180, nullable = false)
    private String razonSocial;

    @Column(name = "NOMBRE_COMERCIAL", length = 180)
    private String nombreComercial;

    @Column(name = "FECHA_CONSTITUCION", nullable = false)
    private LocalDate fechaConstitucion;

    @Column(name = "ACTIVIDAD_ECONOMICA", length = 200)
    private String actividadEconomica;

    @Column(name = "REPRESENTANTE_LEGAL_UUID", length = 36)
    private String representanteLegalUuid;

    @Column(name = "REPRESENTANTE_LEGAL_IDENTIFICACION", length = 20)
    private String representanteLegalIdentificacion;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion;

    public ClientePersonaJuridica() {}

    public ClientePersonaJuridica(Long id) { this.id = id; }

    public static ClientePersonaJuridica crear(Cliente cliente, String razonSocial, String nombreComercial,
                                                LocalDate fechaConstitucion, String actividadEconomica,
                                                String representanteLegalUuid, String representanteLegalIdentificacion) {
        ClientePersonaJuridica juridica = new ClientePersonaJuridica();
        juridica.cliente = cliente;
        juridica.razonSocial = razonSocial;
        juridica.nombreComercial = nombreComercial;
        juridica.fechaConstitucion = fechaConstitucion;
        juridica.actividadEconomica = actividadEconomica;
        juridica.representanteLegalUuid = representanteLegalUuid;
        juridica.representanteLegalIdentificacion = representanteLegalIdentificacion;
        return juridica;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaCreacion == null) fechaCreacion = now;
        if (fechaActualizacion == null) fechaActualizacion = now;
    }

    @PreUpdate
    public void preUpdate() { fechaActualizacion = LocalDateTime.now(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientePersonaJuridica that)) return false;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "ClientePersonaJuridica{" + "id=" + id + ", razonSocial='" + razonSocial + '\'' + '}';
    }
}
