package com.banquito.core.customer.domain.model;

import com.banquito.core.customer.domain.enums.GeneroEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "CLIENTE_PERSONA_NATURAL")
public class ClientePersonaNatural {

    @Id
    @Column(name = "CLIENTE_ID", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "CLIENTE_ID", nullable = false)
    private Cliente cliente;

    @Column(name = "NOMBRES", length = 120, nullable = false)
    private String nombres;

    @Column(name = "APELLIDOS", length = 120, nullable = false)
    private String apellidos;

    @Column(name = "FECHA_NACIMIENTO", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENERO", length = 20)
    private GeneroEnum genero;

    @Column(name = "NACIONALIDAD", length = 80)
    private String nacionalidad;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion;

    public ClientePersonaNatural() {}

    public ClientePersonaNatural(Long id) { this.id = id; }

    public static ClientePersonaNatural crear(Cliente cliente, String nombres, String apellidos,
                                               LocalDate fechaNacimiento, GeneroEnum genero, String nacionalidad) {
        ClientePersonaNatural natural = new ClientePersonaNatural();
        natural.cliente = cliente;
        natural.nombres = nombres;
        natural.apellidos = apellidos;
        natural.fechaNacimiento = fechaNacimiento;
        natural.genero = genero;
        natural.nacionalidad = nacionalidad;
        return natural;
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
        if (!(o instanceof ClientePersonaNatural that)) return false;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "ClientePersonaNatural{" + "id=" + id + ", nombres='" + nombres + '\'' + ", apellidos='" + apellidos + '\'' + '}';
    }
}
