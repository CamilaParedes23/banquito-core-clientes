package com.banquito.core.customer.api.controller;

import com.banquito.core.customer.api.dto.api.*;
import com.banquito.core.customer.api.dto.internal.AuthenticatedActor;
import com.banquito.core.customer.application.service.CustomerService;
import com.banquito.core.customer.domain.enums.TipoClienteEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;


    @GetMapping
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public CustomerListResponse listCustomers(@RequestParam(required = false) String customerType,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) String search,
                                              @RequestParam(defaultValue = "0") Integer page,
                                              @RequestParam(defaultValue = "20") Integer size) {
        return customerService.listarClientes(customerType, status, search, page, size);
    }

    @GetMapping("/subtypes")
    public List<SubtypeCustomerResponse> getSubtypes(@RequestParam(required = false) String customerType,
                                                     @RequestParam(required = false) String status) {
        return customerService.obtenerSubtipos(customerType, status);
    }

    @PostMapping("/subtypes")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public ResponseEntity<SubtypeCustomerResponse> createSubtype(
            @Valid @RequestBody CreateCustomerSubtypeRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                customerService.crearSubtipo(request, actor(authentication), clientIp(httpRequest)));
    }

    @PatchMapping("/subtypes/{code}")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public SubtypeCustomerResponse updateSubtype(
            @PathVariable String code,
            @Valid @RequestBody UpdateCustomerSubtypeRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return customerService.actualizarSubtipo(code, request, actor(authentication), clientIp(httpRequest));
    }

    @PatchMapping("/subtypes/{code}/status")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public SubtypeCustomerResponse changeSubtypeStatus(
            @PathVariable String code,
            @Valid @RequestBody ChangeCustomerSubtypeStatusRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return customerService.cambiarEstadoSubtipo(code, request, actor(authentication), clientIp(httpRequest));
    }

    @PostMapping("/natural-persons")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public ResponseEntity<CustomerDetailResponse> createNaturalPerson(@Valid @RequestBody CreateNaturalPersonCustomerRequest request,
                                                                      Authentication authentication,
                                                                      HttpServletRequest httpRequest) {
        CustomerDetailResponse response = customerService.crearPersonaNatural(request, actor(authentication), clientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/legal-persons")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public ResponseEntity<CustomerDetailResponse> createLegalPerson(@Valid @RequestBody CreateLegalPersonCustomerRequest request,
                                                                    Authentication authentication,
                                                                    HttpServletRequest httpRequest) {
        CustomerDetailResponse response = customerService.crearPersonaJuridica(request, actor(authentication), clientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerUuid}")
    @PreAuthorize("@customerAccessPolicy.canReadCustomer(authentication, #p0)")
    public CustomerDetailResponse getCustomer(@PathVariable String customerUuid) {
        return customerService.buscarDetalle(customerUuid);
    }

    @GetMapping("/by-identification/{identification}")
    @PreAuthorize("@customerAccessPolicy.canBackofficeOrSwitchCustomerRead(authentication)")
    public CustomerBasicResponse getCustomerByIdentification(@PathVariable String identification) {
        return customerService.buscarPorIdentificacion(identification);
    }

    @PatchMapping("/{customerUuid}")
    @PreAuthorize("@customerAccessPolicy.canUpdateContact(authentication, #p0)")
    public CustomerDetailResponse updateCustomer(@PathVariable String customerUuid,
                                                 @Valid @RequestBody UpdateCustomerRequest request,
                                                 Authentication authentication,
                                                 HttpServletRequest httpRequest) {
        return customerService.actualizarContacto(customerUuid, request, actor(authentication), clientIp(httpRequest));
    }

    @PatchMapping("/{customerUuid}/status")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public CustomerDetailResponse changeStatus(@PathVariable String customerUuid,
                                               @Valid @RequestBody ChangeCustomerStatusRequest request,
                                               Authentication authentication,
                                               HttpServletRequest httpRequest) {
        return customerService.cambiarEstado(customerUuid, request, actor(authentication), clientIp(httpRequest));
    }

    @PatchMapping("/{customerUuid}/mass-payments-status")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public CustomerDetailResponse changeMassPaymentsStatus(@PathVariable String customerUuid,
                                                           @Valid @RequestBody ChangeMassPaymentsStatusRequest request,
                                                           Authentication authentication,
                                                           HttpServletRequest httpRequest) {
        return customerService.cambiarEstadoPagosMasivos(customerUuid, request, actor(authentication), clientIp(httpRequest));
    }

    @PostMapping("/{customerUuid}/relationships")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public ResponseEntity<CustomerRelationshipResponse> createRelationship(@PathVariable String customerUuid,
                                                                           @Valid @RequestBody CreateCustomerRelationshipRequest request,
                                                                           Authentication authentication,
                                                                           HttpServletRequest httpRequest) {
        CustomerRelationshipResponse response = customerService.crearRelacion(customerUuid, request, actor(authentication), clientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerUuid}/relationships")
    @PreAuthorize("@customerAccessPolicy.canReadCustomer(authentication, #p0)")
    public List<CustomerRelationshipResponse> getRelationships(@PathVariable String customerUuid,
                                                               @RequestParam(required = false) String relationshipType) {
        return customerService.obtenerRelaciones(customerUuid, relationshipType);
    }


    @GetMapping("/natural-persons")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public List<CustomerBasicResponse> listNaturalPersons(@RequestParam(required = false) Boolean activeOnly) {
        return customerService.listarClientesCompatibles(TipoClienteEnum.NATURAL.name(), activeOnly);
    }

    @GetMapping("/legal-persons")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public List<CustomerBasicResponse> listLegalPersons(@RequestParam(required = false) Boolean activeOnly) {
        return customerService.listarClientesCompatibles(TipoClienteEnum.JURIDICO.name(), activeOnly);
    }

    @GetMapping("/all")
    @PreAuthorize("@customerAccessPolicy.canBackoffice(authentication)")
    public List<CustomerBasicResponse> listAllCustomers() {
        return customerService.listarClientesCompatibles(null, false);
    }

    private String actor(Authentication authentication) {
        if (authentication == null) return null;
        if (authentication.getPrincipal() instanceof AuthenticatedActor actor) {
            return actor.subject();
        }
        return authentication.getName();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
