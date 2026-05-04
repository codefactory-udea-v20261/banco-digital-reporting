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
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    // Use a fixed secret string long enough for HS256 (32+ bytes)
    private static final String SECRET = "cb02f71c3a9cb8b9f36c06f88582553925e03f30b867ae89450c7254737026d8";
    private SecretKey key;

    @BeforeEach
    void setUp() {
        // The filter does Keys.hmacShaKeyFor(jwtSecret.getBytes())
        // so we must use the same approach to build the signing key
        key = Keys.hmacShaKeyFor(SECRET.getBytes());
        ReflectionTestUtils.setField(filter, "jwtSecret", SECRET);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidToken() throws Exception {
        String token = Jwts.builder()
                .subject("user123")
                .claim("roles", List.of("ADMIN"))
                .signWith(key)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        assertEquals("user123", user.username());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithValidTokenExistingRolePrefix() throws Exception {
        String token = Jwts.builder()
                .subject("user123")
                .claim("roles", List.of("ROLE_USER"))
                .signWith(key)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void doFilterInternal_WithValidTokenNoRoles() throws Exception {
        String token = Jwts.builder()
                .subject("user123")
                .signWith(key)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")));
    }

    @Test
    void doFilterInternal_NoToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic 123");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_NoSecretConfigured() throws Exception {
        ReflectionTestUtils.setField(filter, "jwtSecret", "");
        String token = "any.token.here";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
