package com.banquito.core.customer.domain.model;

import com.banquito.core.customer.domain.enums.EstadoOutboxEventEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "OUTBOX_EVENT")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "UUID_EVENTO", length = 36, nullable = false)
    private String uuidEvento;

    @Column(name = "UUID_CORRELACION", length = 36)
    private String uuidCorrelacion;

    @Column(name = "TIPO_EVENTO", length = 80, nullable = false)
    private String tipoEvento;

    @Column(name = "AGREGADO_TIPO", length = 80, nullable = false)
    private String agregadoTipo;

    @Column(name = "AGREGADO_ID", length = 80, nullable = false)
    private String agregadoId;

    @Column(name = "PAYLOAD_JSON", columnDefinition = "json", nullable = false)
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20, nullable = false)
    private EstadoOutboxEventEnum estado;

    @Column(name = "INTENTOS", nullable = false)
    private Integer intentos;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_PUBLICACION")
    private LocalDateTime fechaPublicacion;

    @Column(name = "ERROR_ULTIMO", length = 1000)
    private String errorUltimo;

    public OutboxEvent() {}

    public OutboxEvent(Long id) { this.id = id; }

    public static OutboxEvent crear(String uuidCorrelacion, String tipoEvento, String agregadoTipo, String agregadoId, String payloadJson) {
        OutboxEvent event = new OutboxEvent();
        event.uuidEvento = UUID.randomUUID().toString();
        event.uuidCorrelacion = uuidCorrelacion;
        event.tipoEvento = tipoEvento;
        event.agregadoTipo = agregadoTipo;
        event.agregadoId = agregadoId;
        event.payloadJson = payloadJson;
        event.estado = EstadoOutboxEventEnum.PENDIENTE;
        event.intentos = 0;
        return event;
    }

    @PrePersist
    public void prePersist() {
        if (uuidEvento == null) uuidEvento = UUID.randomUUID().toString();
        if (estado == null) estado = EstadoOutboxEventEnum.PENDIENTE;
        if (intentos == null) intentos = 0;
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboxEvent that)) return false;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "OutboxEvent{" + "id=" + id + ", tipoEvento='" + tipoEvento + '\'' + ", estado=" + estado + '}';
    }
}
