package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import com.udea.bancodigital.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceAdapterTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceAdapter authServiceAdapter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getClienteId_DebeRetornarUuid_CuandoUsuarioEstaAutenticado() {

        UUID clienteId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "testuser", clienteId);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);

        UUID result = authServiceAdapter.getClienteId();

        assertEquals(clienteId, result);
    }

    @Test
    void getClienteId_DebeLanzarExcepcion_CuandoNoHayAutenticacion() {

        when(securityContext.getAuthentication()).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authServiceAdapter.getClienteId());
        assertEquals("No hay un usuario autenticado en el contexto de seguridad", exception.getMessage());
    }

    @Test
    void getClienteId_DebeLanzarExcepcion_CuandoClienteIdEsNull() {

        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "testuser", null);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authServiceAdapter.getClienteId());
        assertEquals("El usuario autenticado no tiene un cliente asociado", exception.getMessage());
    }
}
