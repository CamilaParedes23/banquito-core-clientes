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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    public List<SubtypeCustomerResponse> obtenerSubtipos(String customerType, String status) {
        TipoClienteEnum tipoCliente = customerType == null || customerType.isBlank()
                ? null : parseTipoCliente(customerType);
        EstadoSubtipoClienteEnum estado = status == null || status.isBlank()
                ? EstadoSubtipoClienteEnum.ACTIVO : parseEstadoSubtipo(status);

        List<SubtipoCliente> subtipos;
        if (tipoCliente != null) {
            subtipos = subtipoClienteRepository.findByTipoClienteAndEstadoOrderByNombreAsc(tipoCliente, estado);
        } else {
            subtipos = subtipoClienteRepository.findByEstadoOrderByTipoClienteAscNombreAsc(estado);
        }
        return subtipos.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public SubtypeCustomerResponse crearSubtipo(CreateCustomerSubtypeRequest request, String actorUuid, String ipOrigen) {
        String codigo = request.code().trim().toUpperCase();
        if (subtipoClienteRepository.findByCodigo(codigo).isPresent()) {
            throw new BusinessException("CUSTOMER_SUBTYPE_ALREADY_EXISTS", "Ya existe un subtipo con ese código", HttpStatus.CONFLICT);
        }
        TipoClienteEnum tipoCliente = parseTipoCliente(request.customerType());
        SubtipoCliente subtipo = subtipoClienteRepository.saveAndFlush(
                SubtipoCliente.crear(codigo, tipoCliente, request.name().trim(), normalize(request.description())));
        registrarCambioSubtipo(actorUuid, "CREATE_CUSTOMER_SUBTYPE", subtipo, ipOrigen);
        return mapper.toResponse(subtipo);
    }

    @Transactional
    public SubtypeCustomerResponse actualizarSubtipo(String code, UpdateCustomerSubtypeRequest request,
                                                     String actorUuid, String ipOrigen) {
        SubtipoCliente subtipo = obtenerSubtipo(code);
        subtipo.actualizar(request.name().trim(), normalize(request.description()));
        subtipoClienteRepository.saveAndFlush(subtipo);
        registrarCambioSubtipo(actorUuid, "UPDATE_CUSTOMER_SUBTYPE", subtipo, ipOrigen);
        return mapper.toResponse(subtipo);
    }

    @Transactional
    public SubtypeCustomerResponse cambiarEstadoSubtipo(String code, ChangeCustomerSubtypeStatusRequest request,
                                                        String actorUuid, String ipOrigen) {
        SubtipoCliente subtipo = obtenerSubtipo(code);
        EstadoSubtipoClienteEnum nuevoEstado = parseEstadoSubtipo(request.status());
        subtipo.cambiarEstado(nuevoEstado);
        subtipoClienteRepository.saveAndFlush(subtipo);
        registrarCambioSubtipo(actorUuid, "CHANGE_CUSTOMER_SUBTYPE_STATUS", subtipo, ipOrigen);
        return mapper.toResponse(subtipo);
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

        Map<String, String> representativeNames = resolveLegalRepresentativeNames(juridicos.values());

        List<CustomerBasicResponse> customers = result.getContent().stream()
                .map(cliente -> {
                    Optional<ClientePersonaJuridica> legal = Optional.ofNullable(juridicos.get(cliente.getId()));
                    String representativeName = legal
                            .map(ClientePersonaJuridica::getRepresentanteLegalUuid)
                            .map(representativeNames::get)
                            .orElse(null);
                    return mapper.toBasicResponse(
                            cliente,
                            Optional.ofNullable(naturales.get(cliente.getId())),
                            legal,
                            representativeName
                    );
                })
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
        String identificacion = request.identification().trim();
        validarIdentificacionDisponible(tipoIdentificacion, identificacion);
        SubtipoCliente subtipo = obtenerSubtipoActivo(request.subtypeCode(), TipoClienteEnum.NATURAL);
        GeneroEnum genero = request.gender() == null || request.gender().isBlank() ? null : parseGenero(request.gender());

        Cliente cliente = Cliente.crear(subtipo, TipoClienteEnum.NATURAL, tipoIdentificacion, identificacion,
                request.email().trim(), request.mobilePhone().trim(), request.address().trim(), request.identityUuid(), false);
        Cliente clienteGuardado = clienteRepository.saveAndFlush(cliente);
        naturalRepository.saveAndFlush(ClientePersonaNatural.crear(clienteGuardado, request.names(), request.lastNames(), request.birthDate(), genero, request.nationality()));

        registrarCambio(actorUuid, "CREATE_NATURAL_CUSTOMER", clienteGuardado.getUuidCliente(), ipOrigen, "NATURAL");
        return buscarDetalle(clienteGuardado.getUuidCliente());
    }

    @Transactional
    public CustomerDetailResponse crearPersonaJuridica(CreateLegalPersonCustomerRequest request, String actorUuid, String ipOrigen) {
        TipoIdentificacionEnum tipoIdentificacion = parseTipoIdentificacion(request.identificationType());
        if (tipoIdentificacion != TipoIdentificacionEnum.RUC) {
            throw new BusinessException("CUSTOMER_LEGAL_IDENTIFICATION_INVALID", "Una persona jurídica debe registrarse con RUC", HttpStatus.BAD_REQUEST);
        }
        String identificacion = request.identification().trim();
        validarIdentificacionDisponible(tipoIdentificacion, identificacion);
        SubtipoCliente subtipo = obtenerSubtipoActivo(request.subtypeCode(), TipoClienteEnum.JURIDICO);
        Cliente representanteLegal = validarRepresentanteLegal(
                request.legalRepresentativeUuid(), request.legalRepresentativeIdentification());
        boolean pagosMasivos = Boolean.TRUE.equals(request.massPaymentsEnabled());

        Cliente cliente = Cliente.crear(subtipo, TipoClienteEnum.JURIDICO, tipoIdentificacion, identificacion,
                request.email().trim(), request.mobilePhone().trim(), request.address().trim(), request.identityUuid(), pagosMasivos);
        Cliente clienteGuardado = clienteRepository.saveAndFlush(cliente);
        juridicaRepository.saveAndFlush(ClientePersonaJuridica.crear(clienteGuardado, request.businessName().trim(), normalize(request.tradeName()),
                request.incorporationDate(), normalize(request.economicActivity()), representanteLegal.getUuidCliente(),
                representanteLegal.getIdentificacion()));

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
        return toBasicResponse(cliente);
    }

    @Transactional(readOnly = true)
    public CustomerBasicResponse buscarPorIdentificacion(String identification) {
        Cliente cliente = clienteRepository.findByIdentificacion(identification)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Cliente no encontrado", HttpStatus.NOT_FOUND));
        return toBasicResponse(cliente);
    }

    @Transactional
    public CustomerDetailResponse actualizarContacto(String customerUuid, UpdateCustomerRequest request, String actorUuid, String ipOrigen) {
        Cliente cliente = obtenerCliente(customerUuid);
        cliente.actualizarDatosContacto(
                request.email().trim(),
                request.mobilePhone().trim(),
                request.address().trim());

        if (cliente.getTipoCliente() == TipoClienteEnum.NATURAL) {
            ClientePersonaNatural natural = naturalRepository.findById(cliente.getId())
                    .orElseThrow(() -> new BusinessException(
                            "CUSTOMER_NATURAL_DATA_NOT_FOUND",
                            "No se encontraron los datos de la persona natural",
                            HttpStatus.CONFLICT));
            natural.actualizarDatosPersonales(
                    requiredUpdateValue(request.names(), natural.getNombres(),
                            "CUSTOMER_NAMES_REQUIRED", "Los nombres no pueden quedar vacíos"),
                    requiredUpdateValue(request.lastNames(), natural.getApellidos(),
                            "CUSTOMER_LAST_NAMES_REQUIRED", "Los apellidos no pueden quedar vacíos"),
                    request.nationality() == null ? natural.getNacionalidad() : normalize(request.nationality()));
            naturalRepository.save(natural);
        } else {
            ClientePersonaJuridica juridica = juridicaRepository.findById(cliente.getId())
                    .orElseThrow(() -> new BusinessException(
                            "CUSTOMER_LEGAL_DATA_NOT_FOUND",
                            "No se encontraron los datos de la persona jurídica",
                            HttpStatus.CONFLICT));
            juridica.actualizarDatosCorporativos(
                    requiredUpdateValue(request.businessName(), juridica.getRazonSocial(),
                            "CUSTOMER_BUSINESS_NAME_REQUIRED", "La razón social no puede quedar vacía"),
                    request.tradeName() == null ? juridica.getNombreComercial() : normalize(request.tradeName()),
                    request.economicActivity() == null ? juridica.getActividadEconomica() : normalize(request.economicActivity()));
            juridicaRepository.save(juridica);
        }

        clienteRepository.save(cliente);
        registrarCambio(actorUuid, "UPDATE_CUSTOMER_DATA", customerUuid, ipOrigen,
                "Datos generales y de contacto actualizados");
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

    private CustomerBasicResponse toBasicResponse(Cliente cliente) {
        Optional<ClientePersonaNatural> natural = naturalRepository.findById(cliente.getId());
        Optional<ClientePersonaJuridica> legal = juridicaRepository.findById(cliente.getId());
        String representativeName = legal
                .map(ClientePersonaJuridica::getRepresentanteLegalUuid)
                .flatMap(clienteRepository::findByUuidCliente)
                .flatMap(representative -> naturalRepository.findById(representative.getId()))
                .map(representative -> representative.getNombres() + " " + representative.getApellidos())
                .orElse(null);
        return mapper.toBasicResponse(cliente, natural, legal, representativeName);
    }

    private Map<String, String> resolveLegalRepresentativeNames(Iterable<ClientePersonaJuridica> legalCustomers) {
        Set<String> representativeUuids = new java.util.HashSet<>();
        for (ClientePersonaJuridica legal : legalCustomers) {
            if (legal.getRepresentanteLegalUuid() != null && !legal.getRepresentanteLegalUuid().isBlank()) {
                representativeUuids.add(legal.getRepresentanteLegalUuid());
            }
        }
        if (representativeUuids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Cliente> representatives = clienteRepository.findByUuidClienteIn(representativeUuids);
        Map<Long, ClientePersonaNatural> naturalById = naturalRepository.findAllById(
                        representatives.stream().map(Cliente::getId).toList())
                .stream()
                .collect(Collectors.toMap(ClientePersonaNatural::getId, Function.identity()));

        return representatives.stream()
                .filter(representative -> naturalById.containsKey(representative.getId()))
                .collect(Collectors.toMap(
                        Cliente::getUuidCliente,
                        representative -> {
                            ClientePersonaNatural natural = naturalById.get(representative.getId());
                            return natural.getNombres() + " " + natural.getApellidos();
                        }
                ));
    }

    private Cliente obtenerCliente(String customerUuid) {
        return clienteRepository.findByUuidCliente(customerUuid)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Cliente no encontrado", HttpStatus.NOT_FOUND));
    }

    private SubtipoCliente obtenerSubtipo(String codigo) {
        return subtipoClienteRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new BusinessException("CUSTOMER_SUBTYPE_NOT_FOUND", "Subtipo de cliente no encontrado", HttpStatus.NOT_FOUND));
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

    private Cliente validarRepresentanteLegal(String representativeUuid, String representativeIdentification) {
        String uuid = representativeUuid.trim();
        String identification = representativeIdentification.trim();
        Cliente representante = clienteRepository.findByUuidCliente(uuid)
                .orElseThrow(() -> new BusinessException(
                        "CUSTOMER_LEGAL_REPRESENTATIVE_NOT_FOUND",
                        "El representante legal indicado no existe",
                        HttpStatus.NOT_FOUND));
        if (representante.getTipoCliente() != TipoClienteEnum.NATURAL) {
            throw new BusinessException(
                    "CUSTOMER_LEGAL_REPRESENTATIVE_NOT_NATURAL",
                    "El representante legal debe ser una persona natural",
                    HttpStatus.CONFLICT);
        }
        if (!representante.getIdentificacion().equals(identification)) {
            throw new BusinessException(
                    "CUSTOMER_LEGAL_REPRESENTATIVE_MISMATCH",
                    "La identificación no corresponde al UUID del representante legal",
                    HttpStatus.CONFLICT);
        }
        return representante;
    }

    private void registrarCambioSubtipo(String actorUuid, String accion, SubtipoCliente subtipo, String ipOrigen) {
        String payload = "{\"code\":\"" + subtipo.getCodigo()
                + "\",\"customerType\":\"" + subtipo.getTipoCliente().name()
                + "\",\"status\":\"" + subtipo.getEstado().name() + "\"}";
        auditoriaService.registrar(actorUuid, accion, "SUBTIPO_CLIENTE", subtipo.getCodigo(),
                ResultadoAuditoriaCustomerEnum.OK, ipOrigen, payload);
        outboxEventService.registrarEvento("CUSTOMER_SUBTYPE_CHANGED", "SUBTIPO_CLIENTE", subtipo.getCodigo(), payload);
    }

    private String requiredUpdateValue(String requestedValue, String currentValue,
                                       String errorCode, String errorMessage) {
        if (requestedValue == null) return currentValue;
        if (requestedValue.isBlank()) {
            throw new BusinessException(errorCode, errorMessage, HttpStatus.BAD_REQUEST);
        }
        return requestedValue.trim();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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

    private EstadoSubtipoClienteEnum parseEstadoSubtipo(String value) {
        try { return EstadoSubtipoClienteEnum.valueOf(value.trim().toUpperCase()); }
        catch (Exception e) { throw new BusinessException("CUSTOMER_SUBTYPE_STATUS_INVALID", "Estado de subtipo inválido", HttpStatus.BAD_REQUEST); }
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
