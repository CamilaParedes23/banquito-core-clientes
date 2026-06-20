package com.banquito.core.customer.shared.exception;

import com.banquito.core.customer.api.dto.api.ErrorResponse;
import com.banquito.core.customer.shared.tracing.CorrelationIdHolder;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new ErrorResponse(
                LocalDateTime.now(),
                CorrelationIdHolder.get(),
                exception.getCode(),
                exception.getMessage(),
                List.of()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                LocalDateTime.now(),
                CorrelationIdHolder.get(),
                "VALIDATION_ERROR",
                "Solicitud inválida. Revise los campos enviados.",
                details
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                LocalDateTime.now(),
                CorrelationIdHolder.get(),
                "VALIDATION_ERROR",
                "Solicitud inválida. Revise los parámetros enviados.",
                exception.getConstraintViolations().stream().map(Object::toString).toList()
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                LocalDateTime.now(),
                CorrelationIdHolder.get(),
                "REQUEST_BODY_INVALID",
                "El cuerpo de la solicitud no tiene un formato JSON válido o contiene valores incompatibles.",
                List.of(exception.getClass().getSimpleName())
        ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                LocalDateTime.now(),
                CorrelationIdHolder.get(),
                "REQUEST_PARAMETER_REQUIRED",
                "Falta un parámetro obligatorio en la solicitud.",
                List.of(exception.getParameterName())
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                LocalDateTime.now(),
                CorrelationIdHolder.get(),
                "REQUEST_PARAMETER_INVALID",
                "Uno de los parámetros enviados tiene un tipo o valor inválido.",
                List.of(exception.getName())
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception) {
        String correlationId = CorrelationIdHolder.get();
        Throwable mostSpecificCause = exception.getMostSpecificCause();
        String sqlCause = mostSpecificCause == null ? exception.getMessage() : mostSpecificCause.getMessage();

        log.error("Data integrity violation in customer-service. correlationId={}, sqlCause={}",
                correlationId, sqlCause, exception);

        if (sqlCause != null && sqlCause.contains("UK_CLIENTE_IDENT")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                    LocalDateTime.now(),
                    correlationId,
                    "CUSTOMER_IDENTIFICATION_ALREADY_EXISTS",
                    "Ya existe un cliente con esa identificación",
                    List.of()
            ));
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                LocalDateTime.now(),
                correlationId,
                "DATA_INTEGRITY_VIOLATION",
                "La operación no puede completarse porque viola una regla de integridad de datos.",
                List.of("Consulte los logs internos mediante X-Correlation-Id")
        ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                LocalDateTime.now(),
                CorrelationIdHolder.get(),
                "RESOURCE_NOT_FOUND",
                "El recurso solicitado no existe.",
                List.of(exception.getResourcePath())
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                LocalDateTime.now(),
                CorrelationIdHolder.get(),
                "SECURITY_ACCESS_DENIED",
                "Acceso denegado. El token no posee permisos para este recurso.",
                List.of()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Unhandled exception in customer-service. correlationId={}", CorrelationIdHolder.get(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                LocalDateTime.now(),
                CorrelationIdHolder.get(),
                "INTERNAL_ERROR",
                "Error interno no controlado",
                List.of(exception.getClass().getSimpleName())
        ));
    }
}
