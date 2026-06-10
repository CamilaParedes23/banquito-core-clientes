package com.banquito.core.customer.application.service;

import com.banquito.core.customer.api.dto.api.*;
import com.banquito.core.customer.domain.enums.*;
import com.banquito.core.customer.domain.model.*;
import com.banquito.core.customer.domain.repository.*;
import com.banquito.core.customer.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final ClienteRepository clienteRepository;
    private final SubtipoClienteRepository subtipoClienteRepository;
    private final ClientePersonaNaturalRepository naturalRepository;
    private final ClientePersonaJuridicaRepository juridicaRepository;
    private final ClienteRelacionRepository relacionRepository;
    private final CustomerMapper mapper;
    private final AuditoriaCustomerService auditoriaService;
    private final OutboxEventService outboxEventService;

    @Transactional(readOnly = true)
    public List<SubtypeCustomerResponse> obtenerSubtipos(TipoClienteEnum tipoCliente) {
        return subtipoClienteRepository.findByTipoClienteAndEstadoOrderByNombreAsc(tipoCliente, EstadoSubtipoClienteEnum.ACTIVO)
                .stream().map(mapper::toResponse).toList();
    }


    @Transactional(readOnly = true)
    public CustomerListResponse listarClientes(String customerType, String status, String search, Integer page, Integer size) {
        TipoClienteEnum tipoCliente = customerType == null || customerType.isBlank() ? null : parseTipoCliente(customerType);
        EstadoClienteEnum estado = status == null || status.isBlank() ? null : parseEstadoCliente(status);

        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : Math.min(size, 100);
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Cliente> result = clienteRepository.searchCustomers(
                tipoCliente == null ? null : tipoCliente.name(),
                estado == null ? null : estado.name(),
                normalizedSearch,
                pageable
        );

        List<Long> ids = result.getContent().stream().map(Cliente::getId).toList();
        Map<Long, ClientePersonaNatural> naturales = naturalRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(ClientePersonaNatural::getId, Function.identity()));
        Map<Long, ClientePersonaJuridica> juridicos = juridicaRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(ClientePersonaJuridica::getId, Function.identity()));

        List<CustomerBasicResponse> customers = result.getContent().stream()
                .map(cliente -> mapper.toBasicResponse(
                        cliente,
                        Optional.ofNullable(naturales.get(cliente.getId())),
                        Optional.ofNullable(juridicos.get(cliente.getId()))
                ))
                .toList();

        return new CustomerListResponse(result.getTotalElements(), safePage, safeSize, result.getTotalPages(), customers);
    }

    @Transactional(readOnly = true)
    public List<CustomerBasicResponse> listarClientesCompatibles(String customerType, Boolean activeOnly) {
        CustomerListResponse response = listarClientes(
                customerType,
                Boolean.TRUE.equals(activeOnly) ? EstadoClienteEnum.ACTIVO.name() : null,
                null,
                0,
                100
        );
        return response.customers();
    }

    @Transactional
    public CustomerDetailResponse crearPersonaNatural(CreateNaturalPersonCustomerRequest request, String actorUuid, String ipOrigen) {
        TipoIdentificacionEnum tipoIdentificacion = parseTipoIdentificacion(request.identificationType());
        validarIdentificacionDisponible(tipoIdentificacion, request.identification());
        SubtipoCliente subtipo = obtenerSubtipoActivo(request.subtypeCode(), TipoClienteEnum.NATURAL);
        GeneroEnum genero = request.gender() == null || request.gender().isBlank() ? null : parseGenero(request.gender());

        Cliente cliente = Cliente.crear(subtipo, TipoClienteEnum.NATURAL, tipoIdentificacion, request.identification(),
                request.email(), request.mobilePhone(), request.address(), request.identityUuid(), false);
        Cliente clienteGuardado = clienteRepository.saveAndFlush(cliente);
        naturalRepository.save(ClientePersonaNatural.crear(clienteGuardado, request.names(), request.lastNames(), request.birthDate(), genero, request.nationality()));

        registrarCambio(actorUuid, "CREATE_NATURAL_CUSTOMER", clienteGuardado.getUuidCliente(), ipOrigen, "NATURAL");
        return buscarDetalle(clienteGuardado.getUuidCliente());
    }

    @Transactional
    public CustomerDetailResponse crearPersonaJuridica(CreateLegalPersonCustomerRequest request, String actorUuid, String ipOrigen) {
        TipoIdentificacionEnum tipoIdentificacion = parseTipoIdentificacion(request.identificationType());
        if (tipoIdentificacion != TipoIdentificacionEnum.RUC) {
            throw new BusinessException("CUSTOMER_LEGAL_IDENTIFICATION_INVALID", "Una persona jurídica debe registrarse con RUC", HttpStatus.BAD_REQUEST);
        }
        validarIdentificacionDisponible(tipoIdentificacion, request.identification());
        SubtipoCliente subtipo = obtenerSubtipoActivo(request.subtypeCode(), TipoClienteEnum.JURIDICO);
        boolean pagosMasivos = Boolean.TRUE.equals(request.massPaymentsEnabled());

        Cliente cliente = Cliente.crear(subtipo, TipoClienteEnum.JURIDICO, tipoIdentificacion, request.identification(),
                request.email(), request.mobilePhone(), request.address(), request.identityUuid(), pagosMasivos);
        Cliente clienteGuardado = clienteRepository.saveAndFlush(cliente);
        juridicaRepository.save(ClientePersonaJuridica.crear(clienteGuardado, request.businessName(), request.tradeName(),
                request.incorporationDate(), request.economicActivity(), request.legalRepresentativeUuid(),
                request.legalRepresentativeIdentification()));

        registrarCambio(actorUuid, "CREATE_LEGAL_CUSTOMER", clienteGuardado.getUuidCliente(), ipOrigen, "JURIDICO");
        return buscarDetalle(clienteGuardado.getUuidCliente());
    }

    @Transactional(readOnly = true)
    public CustomerDetailResponse buscarDetalle(String customerUuid) {
        Cliente cliente = obtenerCliente(customerUuid);
        return mapper.toDetailResponse(cliente, naturalRepository.findById(cliente.getId()), juridicaRepository.findById(cliente.getId()));
    }


    @Transactional(readOnly = true)
    public CustomerBasicResponse buscarBasicoPorUuid(String customerUuid) {
        Cliente cliente = obtenerCliente(customerUuid);
        return mapper.toBasicResponse(cliente, naturalRepository.findById(cliente.getId()), juridicaRepository.findById(cliente.getId()));
    }

    @Transactional(readOnly = true)
    public CustomerBasicResponse buscarPorIdentificacion(String identification) {
        Cliente cliente = clienteRepository.findByIdentificacion(identification)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Cliente no encontrado", HttpStatus.NOT_FOUND));
        return mapper.toBasicResponse(cliente, naturalRepository.findById(cliente.getId()), juridicaRepository.findById(cliente.getId()));
    }

    @Transactional
    public CustomerDetailResponse actualizarContacto(String customerUuid, UpdateCustomerRequest request, String actorUuid, String ipOrigen) {
        Cliente cliente = obtenerCliente(customerUuid);
        cliente.actualizarDatosContacto(request.email(), request.mobilePhone(), request.address());
        clienteRepository.save(cliente);
        registrarCambio(actorUuid, "UPDATE_CUSTOMER_CONTACT", customerUuid, ipOrigen, null);
        return buscarDetalle(customerUuid);
    }

    @Transactional
    public CustomerDetailResponse cambiarEstado(String customerUuid, ChangeCustomerStatusRequest request, String actorUuid, String ipOrigen) {
        Cliente cliente = obtenerCliente(customerUuid);
        EstadoClienteEnum nuevoEstado = parseEstadoCliente(request.status());
        cliente.cambiarEstado(nuevoEstado);
        clienteRepository.save(cliente);
        registrarCambio(actorUuid, "CHANGE_CUSTOMER_STATUS", customerUuid, ipOrigen, nuevoEstado.name());
        return buscarDetalle(customerUuid);
    }

    @Transactional
    public CustomerDetailResponse cambiarEstadoPagosMasivos(String customerUuid, ChangeMassPaymentsStatusRequest request, String actorUuid, String ipOrigen) {
        Cliente cliente = obtenerCliente(customerUuid);
        if (cliente.getTipoCliente() != TipoClienteEnum.JURIDICO && Boolean.TRUE.equals(request.enabled())) {
            throw new BusinessException("CUSTOMER_MASS_PAYMENTS_ONLY_LEGAL", "Solo clientes jurídicos pueden habilitar pagos masivos", HttpStatus.BAD_REQUEST);
        }
        cliente.cambiarPagosMasivos(Boolean.TRUE.equals(request.enabled()));
        clienteRepository.save(cliente);
        registrarCambio(actorUuid, "CHANGE_MASS_PAYMENTS_STATUS", customerUuid, ipOrigen, String.valueOf(request.enabled()));
        return buscarDetalle(customerUuid);
    }

    @Transactional
    public CustomerRelationshipResponse crearRelacion(String customerUuid, CreateCustomerRelationshipRequest request, String actorUuid, String ipOrigen) {
        obtenerCliente(customerUuid);
        obtenerCliente(request.relatedCustomerUuid());
        TipoRelacionClienteEnum tipoRelacion = parseTipoRelacion(request.relationshipType());
        ClienteRelacion relacion = ClienteRelacion.crear(customerUuid, request.relatedCustomerUuid(), tipoRelacion,
                request.relatedIdentification(), request.relatedName(), request.startDate(), request.observation());
        ClienteRelacion guardada = relacionRepository.save(relacion);
        registrarCambio(actorUuid, "CREATE_CUSTOMER_RELATIONSHIP", customerUuid, ipOrigen, tipoRelacion.name());
        return mapper.toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<CustomerRelationshipResponse> obtenerRelaciones(String customerUuid, String relationshipType) {
        obtenerCliente(customerUuid);
        List<ClienteRelacion> relaciones;
        if (relationshipType != null && !relationshipType.isBlank()) {
            relaciones = relacionRepository.findByUuidClienteOrigenAndTipoRelacionAndEstadoOrderByFechaInicioDesc(
                    customerUuid, parseTipoRelacion(relationshipType), EstadoRelacionClienteEnum.ACTIVA);
        } else {
            relaciones = relacionRepository.findByUuidClienteOrigenAndEstadoOrderByFechaInicioDesc(customerUuid, EstadoRelacionClienteEnum.ACTIVA);
        }
        return relaciones.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public void validarClienteActivo(String customerUuid) {
        Cliente cliente = obtenerCliente(customerUuid);
        if (cliente.getEstado() != EstadoClienteEnum.ACTIVO) {
            throw new BusinessException("CUSTOMER_NOT_ACTIVE", "El cliente no está activo", HttpStatus.CONFLICT);
        }
    }

    @Transactional(readOnly = true)
    public void validarPagosMasivos(String customerUuid) {
        Cliente cliente = obtenerCliente(customerUuid);
        if (cliente.getEstado() != EstadoClienteEnum.ACTIVO || !Boolean.TRUE.equals(cliente.getActivoPagosMasivos())) {
            throw new BusinessException("CUSTOMER_MASS_PAYMENTS_DISABLED", "El cliente no está habilitado para pagos masivos", HttpStatus.CONFLICT);
        }
    }

    private Cliente obtenerCliente(String customerUuid) {
        return clienteRepository.findByUuidCliente(customerUuid)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Cliente no encontrado", HttpStatus.NOT_FOUND));
    }

    private SubtipoCliente obtenerSubtipoActivo(String codigo, TipoClienteEnum tipoCliente) {
        return subtipoClienteRepository.findByCodigoAndTipoClienteAndEstado(codigo, tipoCliente, EstadoSubtipoClienteEnum.ACTIVO)
                .orElseThrow(() -> new BusinessException("CUSTOMER_SUBTYPE_NOT_FOUND", "Subtipo de cliente no encontrado o inactivo", HttpStatus.NOT_FOUND));
    }

    private void validarIdentificacionDisponible(TipoIdentificacionEnum tipoIdentificacion, String identificacion) {
        if (clienteRepository.existsByTipoIdentificacionAndIdentificacion(tipoIdentificacion, identificacion)) {
            throw new BusinessException("CUSTOMER_IDENTIFICATION_ALREADY_EXISTS", "Ya existe un cliente con esa identificación", HttpStatus.CONFLICT);
        }
    }

    private void registrarCambio(String actorUuid, String accion, String customerUuid, String ipOrigen, String detalle) {
        auditoriaService.registrar(actorUuid, accion, "CLIENTE", customerUuid, ResultadoAuditoriaCustomerEnum.OK, ipOrigen, detalle == null ? null : "{\"detail\":\"" + detalle + "\"}");
        outboxEventService.registrarEvento("CUSTOMER_CHANGED", "CLIENTE", customerUuid, "{\"customerUuid\":\"" + customerUuid + "\",\"action\":\"" + accion + "\"}");
    }


    private TipoClienteEnum parseTipoCliente(String value) {
        try { return TipoClienteEnum.valueOf(value); }
        catch (Exception e) { throw new BusinessException("CUSTOMER_TYPE_INVALID", "Tipo de cliente inválido", HttpStatus.BAD_REQUEST); }
    }

    private TipoIdentificacionEnum parseTipoIdentificacion(String value) {
        try { return TipoIdentificacionEnum.valueOf(value); }
        catch (Exception e) { throw new BusinessException("CUSTOMER_IDENTIFICATION_TYPE_INVALID", "Tipo de identificación inválido", HttpStatus.BAD_REQUEST); }
    }

    private GeneroEnum parseGenero(String value) {
        try { return GeneroEnum.valueOf(value); }
        catch (Exception e) { throw new BusinessException("CUSTOMER_GENDER_INVALID", "Género inválido", HttpStatus.BAD_REQUEST); }
    }

    private EstadoClienteEnum parseEstadoCliente(String value) {
        try { return EstadoClienteEnum.valueOf(value); }
        catch (Exception e) { throw new BusinessException("CUSTOMER_STATUS_INVALID", "Estado de cliente inválido", HttpStatus.BAD_REQUEST); }
    }

    private TipoRelacionClienteEnum parseTipoRelacion(String value) {
        try { return TipoRelacionClienteEnum.valueOf(value); }
        catch (Exception e) { throw new BusinessException("CUSTOMER_RELATIONSHIP_TYPE_INVALID", "Tipo de relación inválido", HttpStatus.BAD_REQUEST); }
    }
}
