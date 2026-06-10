package com.banquito.core.customer.infrastructure.grpc;

import com.banquito.core.customer.api.dto.api.CustomerBasicResponse;
import com.banquito.core.customer.api.dto.api.CustomerDetailResponse;
import com.banquito.core.customer.api.dto.api.CustomerRelationshipResponse;
import com.banquito.core.customer.application.service.CustomerService;
import com.banquito.core.customer.shared.exception.BusinessException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class CustomerQueryGrpcService extends CustomerQueryServiceGrpc.CustomerQueryServiceImplBase {
    private final CustomerService customerService;

    public CustomerQueryGrpcService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void getCustomerByUuid(GetCustomerByUuidRequest request, StreamObserver<CustomerBasicGrpcResponse> responseObserver) {
        try {
            responseObserver.onNext(toGrpc(customerService.buscarBasicoPorUuid(request.getCustomerUuid())));
            responseObserver.onCompleted();
        } catch (RuntimeException ex) { fail(responseObserver, ex); }
    }

    @Override
    public void getCustomerByIdentification(GetCustomerByIdentificationRequest request, StreamObserver<CustomerBasicGrpcResponse> responseObserver) {
        try {
            responseObserver.onNext(toGrpc(customerService.buscarPorIdentificacion(request.getIdentification())));
            responseObserver.onCompleted();
        } catch (RuntimeException ex) { fail(responseObserver, ex); }
    }

    @Override
    public void validateCustomerStatus(ValidateCustomerStatusRequest request, StreamObserver<ValidationGrpcResponse> responseObserver) {
        try {
            customerService.validarClienteActivo(request.getCustomerUuid());
            responseObserver.onNext(ValidationGrpcResponse.newBuilder().setValid(true).setCode("OK").setMessage("Cliente activo").build());
            responseObserver.onCompleted();
        } catch (RuntimeException ex) { fail(responseObserver, ex); }
    }

    @Override
    public void validateMassPaymentsEnabled(ValidateMassPaymentsEnabledRequest request, StreamObserver<ValidationGrpcResponse> responseObserver) {
        try {
            customerService.validarPagosMasivos(request.getCustomerUuid());
            responseObserver.onNext(ValidationGrpcResponse.newBuilder().setValid(true).setCode("OK").setMessage("Cliente habilitado para pagos masivos").build());
            responseObserver.onCompleted();
        } catch (RuntimeException ex) { fail(responseObserver, ex); }
    }

    @Override
    public void getCustomerRelationships(GetCustomerRelationshipsRequest request, StreamObserver<GetCustomerRelationshipsResponse> responseObserver) {
        try {
            GetCustomerRelationshipsResponse.Builder builder = GetCustomerRelationshipsResponse.newBuilder();
            for (CustomerRelationshipResponse r : customerService.obtenerRelaciones(request.getCustomerUuid(), request.getRelationshipType())) {
                builder.addRelationships(CustomerRelationshipGrpcResponse.newBuilder()
                        .setRelationshipUuid(nvl(r.relationshipUuid()))
                        .setOriginCustomerUuid(nvl(r.originCustomerUuid()))
                        .setRelatedCustomerUuid(nvl(r.relatedCustomerUuid()))
                        .setRelationshipType(nvl(r.relationshipType()))
                        .setRelatedIdentification(nvl(r.relatedIdentification()))
                        .setRelatedName(nvl(r.relatedName()))
                        .setStatus(nvl(r.status()))
                        .build());
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (RuntimeException ex) { fail(responseObserver, ex); }
    }

    private CustomerBasicGrpcResponse toGrpc(CustomerBasicResponse r) {
        return CustomerBasicGrpcResponse.newBuilder()
                .setCustomerUuid(nvl(r.customerUuid()))
                .setCustomerType(nvl(r.customerType()))
                .setIdentificationType(nvl(r.identificationType()))
                .setIdentification(nvl(r.identification()))
                .setDisplayName(nvl(r.displayName()))
                .setStatus(nvl(r.status()))
                .setMassPaymentsEnabled(Boolean.TRUE.equals(r.massPaymentsEnabled()))
                .build();
    }

    private static <T> void fail(StreamObserver<T> observer, RuntimeException ex) {
        if (ex instanceof BusinessException be) {
            observer.onError(Status.FAILED_PRECONDITION.withDescription(be.getCode() + "|" + be.getMessage()).asRuntimeException());
        } else {
            observer.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    private static String nvl(String value) { return value == null ? "" : value; }
}
