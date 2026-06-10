package com.banquito.core.customer.domain.model;

import com.banquito.core.customer.domain.enums.ResultadoAuditoriaCustomerEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "AUDITORIA_CUSTOMER_EVENTO")
public class AuditoriaCustomerEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "UUID_CORRELACION", length = 36)
    private String uuidCorrelacion;

    @Column(name = "UUID_USUARIO", length = 36)
    private String uuidUsuario;

    @Column(name = "MODULO", length = 60, nullable = false)
    private String modulo;

    @Column(name = "ACCION", length = 80, nullable = false)
    private String accion;

    @Column(name = "ENTIDAD", length = 80, nullable = false)
    private String entidad;

    @Column(name = "ENTIDAD_ID", length = 80)
    private String entidadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESULTADO", length = 15, nullable = false)
    private ResultadoAuditoriaCustomerEnum resultado;

    @Column(name = "CANAL_ORIGEN", length = 30)
    private String canalOrigen;

    @Column(name = "IP_ORIGEN", length = 45)
    private String ipOrigen;

    @Column(name = "DETALLE_JSON", columnDefinition = "json")
    private String detalleJson;

    @Column(name = "FECHA_EVENTO", nullable = false)
    private LocalDateTime fechaEvento;

    public AuditoriaCustomerEvento() {}

    public AuditoriaCustomerEvento(Long id) { this.id = id; }

    public static AuditoriaCustomerEvento crear(String uuidCorrelacion, String uuidUsuario, String accion,
                                                 String entidad, String entidadId, ResultadoAuditoriaCustomerEnum resultado,
                                                 String ipOrigen, String detalleJson) {
        AuditoriaCustomerEvento evento = new AuditoriaCustomerEvento();
        evento.uuidCorrelacion = uuidCorrelacion;
        evento.uuidUsuario = uuidUsuario;
        evento.modulo = "CUSTOMER";
        evento.accion = accion;
        evento.entidad = entidad;
        evento.entidadId = entidadId;
        evento.resultado = resultado;
        evento.ipOrigen = ipOrigen;
        evento.detalleJson = detalleJson;
        return evento;
    }

    @PrePersist
    public void prePersist() {
        if (fechaEvento == null) fechaEvento = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditoriaCustomerEvento that)) return false;
        if (this.id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "AuditoriaCustomerEvento{" + "id=" + id + ", accion='" + accion + '\'' + ", entidad='" + entidad + '\'' + ", resultado=" + resultado + '}';
    }
}
