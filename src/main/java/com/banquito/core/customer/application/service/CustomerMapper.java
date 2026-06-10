package com.banquito.core.customer.application.service;

import com.banquito.core.customer.api.dto.api.CustomerBasicResponse;
import com.banquito.core.customer.api.dto.api.CustomerDetailResponse;
import com.banquito.core.customer.api.dto.api.CustomerRelationshipResponse;
import com.banquito.core.customer.api.dto.api.SubtypeCustomerResponse;
import com.banquito.core.customer.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerMapper {

    public SubtypeCustomerResponse toResponse(SubtipoCliente subtipo) {
        return new SubtypeCustomerResponse(
                subtipo.getId(), subtipo.getCodigo(), subtipo.getTipoCliente().name(), subtipo.getNombre(), subtipo.getDescripcion(), subtipo.getEstado().name()
        );
    }

    public CustomerBasicResponse toBasicResponse(Cliente cliente, Optional<ClientePersonaNatural> natural,
                                                 Optional<ClientePersonaJuridica> juridica) {
        return new CustomerBasicResponse(
                cliente.getUuidCliente(),
                cliente.getTipoCliente().name(),
                cliente.getTipoIdentificacion().name(),
                cliente.getIdentificacion(),
                displayName(cliente, natural, juridica),
                cliente.getEstado().name(),
                cliente.getActivoPagosMasivos()
        );
    }

    public CustomerDetailResponse toDetailResponse(Cliente cliente, Optional<ClientePersonaNatural> natural,
                                                   Optional<ClientePersonaJuridica> juridica) {
        CustomerDetailResponse.NaturalPersonData naturalData = natural
                .map(n -> new CustomerDetailResponse.NaturalPersonData(
                        n.getNombres(), n.getApellidos(), n.getFechaNacimiento(),
                        n.getGenero() == null ? null : n.getGenero().name(), n.getNacionalidad()))
                .orElse(null);
        CustomerDetailResponse.LegalPersonData legalData = juridica
                .map(j -> new CustomerDetailResponse.LegalPersonData(
                        j.getRazonSocial(), j.getNombreComercial(), j.getFechaConstitucion(),
                        j.getActividadEconomica(), j.getRepresentanteLegalUuid(), j.getRepresentanteLegalIdentificacion()))
                .orElse(null);
        return new CustomerDetailResponse(
                cliente.getUuidCliente(),
                cliente.getUuidIdentidadPrincipal(),
                cliente.getSubtipoCliente().getCodigo(),
                cliente.getTipoCliente().name(),
                cliente.getTipoIdentificacion().name(),
                cliente.getIdentificacion(),
                cliente.getEmail(),
                cliente.getTelefonoMovil(),
                cliente.getDireccion(),
                cliente.getEstado().name(),
                cliente.getActivoPagosMasivos(),
                cliente.getFechaAlta(),
                naturalData,
                legalData
        );
    }

    public CustomerRelationshipResponse toResponse(ClienteRelacion relacion) {
        return new CustomerRelationshipResponse(
                relacion.getUuidRelacion(), relacion.getUuidClienteOrigen(), relacion.getUuidClienteRelacionado(),
                relacion.getTipoRelacion().name(), relacion.getIdentificacionRelacionada(), relacion.getNombreRelacionado(),
                relacion.getFechaInicio(), relacion.getFechaFin(), relacion.getEstado().name(), relacion.getObservacion()
        );
    }

    private String displayName(Cliente cliente, Optional<ClientePersonaNatural> natural, Optional<ClientePersonaJuridica> juridica) {
        if (natural.isPresent()) {
            ClientePersonaNatural n = natural.get();
            return n.getNombres() + " " + n.getApellidos();
        }
        if (juridica.isPresent()) {
            return juridica.get().getRazonSocial();
        }
        return cliente.getIdentificacion();
    }
}
