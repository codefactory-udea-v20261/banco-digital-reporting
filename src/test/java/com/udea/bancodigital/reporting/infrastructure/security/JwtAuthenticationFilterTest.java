package com.udea.bancodigital.reporting.infrastructure.security;

import com.udea.bancodigital.shared.security.AuthenticatedUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private IdentityServiceClient identityServiceClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        key = Keys.hmacShaKeyFor(SECRET.getBytes());
        ReflectionTestUtils.setField(filter, "jwtSecret", SECRET);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidToken() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        TokenValidationResponse validationResponse = new TokenValidationResponse();
        validationResponse.setActive(true);
        validationResponse.setSub("user123");
        validationResponse.setUid(userId.toString());
        validationResponse.setClienteId(clienteId.toString());
        validationResponse.setAuthorities(List.of("ROLE_ADMIN", "READ_ALL"));

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(identityServiceClient.validateToken("valid-token")).thenReturn(validationResponse);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        assertEquals("user123", user.username());
        assertEquals(userId, user.userId());
        assertEquals(clienteId, user.clienteId());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("READ_ALL")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InactiveToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(identityServiceClient.validateToken("invalid-token")).thenReturn(TokenValidationResponse.inactive());

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
