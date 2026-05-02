package com.udea.bancodigital.shared.exception;

import com.udea.bancodigital.shared.web.ApiError;
import com.udea.bancodigital.shared.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones.
 * Garantiza que TODAS las APIs devuelvan una estructura uniforme ApiResponse<T>.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), ex.getHttpStatus(), request);
    }

    @ExceptionHandler(ClienteYaExisteException.class)
    public ResponseEntity<ApiResponse<Void>> handleClienteYaExisteException(
            ClienteYaExisteException ex, HttpServletRequest request) {
        return buildErrorResponse("CLIENTE_EXISTE", ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiResponse<Void>> handleCredencialesInvalidasException(
            CredencialesInvalidasException ex, HttpServletRequest request) {
        return buildErrorResponse("AUTH_FAILED", ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ApiResponse<Void>> handleCuentaBloqueadaException(
            CuentaBloqueadaException ex, HttpServletRequest request) {
        return buildErrorResponse("ACCOUNT_LOCKED", ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(DatosIncompletosException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatosIncompletosException(
            DatosIncompletosException ex, HttpServletRequest request) {
        return buildErrorResponse("INVALID_DATA", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MfaRequeridoException.class)
    public ResponseEntity<ApiResponse<Void>> handleMfaRequeridoException(
            MfaRequeridoException ex, HttpServletRequest request) {
        return buildErrorResponse("MFA_REQUIRED", ex.getMessage(), HttpStatus.ACCEPTED, request);
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
            String errorCode, String message, HttpStatus status, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("[{}] {}: {} — path={}", traceId, errorCode, message, request.getRequestURI());
        ApiError error = ApiError.builder()
                .errorCode(errorCode)
                .message(message)
                .traceId(traceId)
                .httpStatus(status.value())
                .build();
        return ResponseEntity.status(status).body(ApiResponse.error(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        log.warn("[{}] ValidationException: {} — path={}", traceId, details, request.getRequestURI());
        ApiError error = ApiError.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Los datos enviados no son válidos")
                .details(details)
                .traceId(traceId)
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .build();
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("[{}] Unexpected error — path={}", traceId, request.getRequestURI(), ex);
        ApiError error = ApiError.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("Ocurrió un error inesperado. Contacte al administrador.")
                .traceId(traceId)
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return ResponseEntity.internalServerError().body(ApiResponse.error(error));
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
