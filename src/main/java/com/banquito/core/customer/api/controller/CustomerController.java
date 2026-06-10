package com.banquito.core.customer.api.controller;

import com.banquito.core.customer.api.dto.api.*;
import com.banquito.core.customer.application.service.CustomerService;
import com.banquito.core.customer.domain.enums.TipoClienteEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;


    @GetMapping
    public CustomerListResponse listCustomers(@RequestParam(required = false) String customerType,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) String search,
                                              @RequestParam(defaultValue = "0") Integer page,
                                              @RequestParam(defaultValue = "20") Integer size) {
        return customerService.listarClientes(customerType, status, search, page, size);
    }

    @GetMapping("/subtypes")
    public List<SubtypeCustomerResponse> getSubtypes(@RequestParam String customerType) {
        return customerService.obtenerSubtipos(TipoClienteEnum.valueOf(customerType));
    }

    @PostMapping("/natural-persons")
    public ResponseEntity<CustomerDetailResponse> createNaturalPerson(@Valid @RequestBody CreateNaturalPersonCustomerRequest request,
                                                                      Authentication authentication,
                                                                      HttpServletRequest httpRequest) {
        CustomerDetailResponse response = customerService.crearPersonaNatural(request, actor(authentication), clientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/legal-persons")
    public ResponseEntity<CustomerDetailResponse> createLegalPerson(@Valid @RequestBody CreateLegalPersonCustomerRequest request,
                                                                    Authentication authentication,
                                                                    HttpServletRequest httpRequest) {
        CustomerDetailResponse response = customerService.crearPersonaJuridica(request, actor(authentication), clientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerUuid}")
    public CustomerDetailResponse getCustomer(@PathVariable String customerUuid) {
        return customerService.buscarDetalle(customerUuid);
    }

    @GetMapping("/by-identification/{identification}")
    public CustomerBasicResponse getCustomerByIdentification(@PathVariable String identification) {
        return customerService.buscarPorIdentificacion(identification);
    }

    @PatchMapping("/{customerUuid}")
    public CustomerDetailResponse updateCustomer(@PathVariable String customerUuid,
                                                 @Valid @RequestBody UpdateCustomerRequest request,
                                                 Authentication authentication,
                                                 HttpServletRequest httpRequest) {
        return customerService.actualizarContacto(customerUuid, request, actor(authentication), clientIp(httpRequest));
    }

    @PatchMapping("/{customerUuid}/status")
    public CustomerDetailResponse changeStatus(@PathVariable String customerUuid,
                                               @Valid @RequestBody ChangeCustomerStatusRequest request,
                                               Authentication authentication,
                                               HttpServletRequest httpRequest) {
        return customerService.cambiarEstado(customerUuid, request, actor(authentication), clientIp(httpRequest));
    }

    @PatchMapping("/{customerUuid}/mass-payments-status")
    public CustomerDetailResponse changeMassPaymentsStatus(@PathVariable String customerUuid,
                                                           @Valid @RequestBody ChangeMassPaymentsStatusRequest request,
                                                           Authentication authentication,
                                                           HttpServletRequest httpRequest) {
        return customerService.cambiarEstadoPagosMasivos(customerUuid, request, actor(authentication), clientIp(httpRequest));
    }

    @PostMapping("/{customerUuid}/relationships")
    public ResponseEntity<CustomerRelationshipResponse> createRelationship(@PathVariable String customerUuid,
                                                                           @Valid @RequestBody CreateCustomerRelationshipRequest request,
                                                                           Authentication authentication,
                                                                           HttpServletRequest httpRequest) {
        CustomerRelationshipResponse response = customerService.crearRelacion(customerUuid, request, actor(authentication), clientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerUuid}/relationships")
    public List<CustomerRelationshipResponse> getRelationships(@PathVariable String customerUuid,
                                                               @RequestParam(required = false) String relationshipType) {
        return customerService.obtenerRelaciones(customerUuid, relationshipType);
    }


    @GetMapping("/natural-persons")
    public List<CustomerBasicResponse> listNaturalPersons(@RequestParam(required = false) Boolean activeOnly) {
        return customerService.listarClientesCompatibles(TipoClienteEnum.NATURAL.name(), activeOnly);
    }

    @GetMapping("/legal-persons")
    public List<CustomerBasicResponse> listLegalPersons(@RequestParam(required = false) Boolean activeOnly) {
        return customerService.listarClientesCompatibles(TipoClienteEnum.JURIDICO.name(), activeOnly);
    }

    @GetMapping("/all")
    public List<CustomerBasicResponse> listAllCustomers() {
        return customerService.listarClientesCompatibles(null, false);
    }

    private String actor(Authentication authentication) {
        return authentication == null ? null : String.valueOf(authentication.getPrincipal());
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
