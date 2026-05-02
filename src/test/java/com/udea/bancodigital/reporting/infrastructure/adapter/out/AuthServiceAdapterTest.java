package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import com.udea.bancodigital.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceAdapterTest {

    private AuthServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AuthServiceAdapter();
        SecurityContextHolder.clearContext();
    }

    @Test
    void getClienteId_Success() {
        UUID userId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(userId, "test", clienteId);
        var auth = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(clienteId, adapter.getClienteId());
    }

    @Test
    void getClienteId_NoAuthentication_ThrowsException() {
        assertThrows(IllegalStateException.class, () -> adapter.getClienteId());
    }

    @Test
    void getClienteId_InvalidPrincipal_ThrowsException() {
        var auth = new UsernamePasswordAuthenticationToken("wrong principal", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(IllegalStateException.class, () -> adapter.getClienteId());
    }

    @Test
    void getClienteId_NoClienteId_ThrowsException() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(userId, "test", null);
        var auth = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(IllegalStateException.class, () -> adapter.getClienteId());
    }
}
