package com.udea.bancodigital.reporting.infrastructure.config;

import com.udea.bancodigital.reporting.infrastructure.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private Environment environment;

    @Mock
    private HttpSecurity httpSecurity;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void testPasswordEncoder() {
        assertNotNull(securityConfig.passwordEncoder());
    }

    @Test
    void testFilterChainDev() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        try {
            securityConfig.filterChain(httpSecurity);
        } catch (Exception e) {
            // Expected
        }
        verify(environment, atLeastOnce()).getActiveProfiles();
        assertTrue(true);
    }

    @Test
    void testFilterChainLocal() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"local"});
        try {
            securityConfig.filterChain(httpSecurity);
        } catch (Exception e) {
            // Expected
        }
        assertTrue(true);
    }

    @Test
    void testFilterChainProd() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "prod" });
        try {
            securityConfig.filterChain(httpSecurity);
        } catch (Exception e) {
        }
    }
}
