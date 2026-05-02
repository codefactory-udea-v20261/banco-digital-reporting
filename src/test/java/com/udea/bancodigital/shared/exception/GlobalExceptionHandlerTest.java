package com.udea.bancodigital.shared.exception;

import com.udea.bancodigital.shared.web.ApiError;
import com.udea.bancodigital.shared.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test-uri");
    }

    @Test
    void handleBusinessException_DebeRetornarUnprocessableEntity() {

        BusinessException ex = new BusinessException("BUS-001", "Error de negocio", HttpStatus.UNPROCESSABLE_ENTITY) {
        };

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error de negocio", response.getBody().getError().getMessage());
    }

    @Test
    void handleGenericException_DebeRetornarInternalError() {

        Exception ex = new Exception("Error interno");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ocurrió un error inesperado. Contacte al administrador.",
                response.getBody().getError().getMessage());
    }
    @Test
    void handleClienteYaExisteException_DebeRetornarConflict() {
        ClienteYaExisteException ex = new ClienteYaExisteException("email", "test@test.com");
        ResponseEntity<ApiResponse<Void>> response = handler.handleClienteYaExisteException(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Ya existe un cliente con email: test@test.com", response.getBody().getError().getMessage());
    }

    @Test
    void handleCredencialesInvalidasException_DebeRetornarUnauthorized() {
        CredencialesInvalidasException ex = new CredencialesInvalidasException();
        ResponseEntity<ApiResponse<Void>> response = handler.handleCredencialesInvalidasException(ex, request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleCuentaBloqueadaException_DebeRetornarForbidden() {
        CuentaBloqueadaException ex = new CuentaBloqueadaException();
        ResponseEntity<ApiResponse<Void>> response = handler.handleCuentaBloqueadaException(ex, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleDatosIncompletosException_DebeRetornarBadRequest() {
        DatosIncompletosException ex = new DatosIncompletosException("Datos incompletos");
        ResponseEntity<ApiResponse<Void>> response = handler.handleDatosIncompletosException(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Datos incompletos", response.getBody().getError().getMessage());
    }

    @Test
    void handleMfaRequeridoException_DebeRetornarAccepted() {
        MfaRequeridoException ex = new MfaRequeridoException();
        ResponseEntity<ApiResponse<Void>> response = handler.handleMfaRequeridoException(ex, request);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }
}
