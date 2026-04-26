package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import com.udea.bancodigital.shared.security.AuthenticatedUser;
import com.udea.bancodigital.shared.security.AuthenticatedClientProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthServiceAdapter implements AuthenticatedClientProvider {

    @Override
    public UUID getClienteId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new IllegalStateException("No hay un usuario autenticado en el contexto de seguridad");
        }

        if (principal.clienteId() == null) {
            throw new IllegalStateException("El usuario autenticado no tiene un cliente asociado");
        }

        return principal.clienteId();
    }
}
