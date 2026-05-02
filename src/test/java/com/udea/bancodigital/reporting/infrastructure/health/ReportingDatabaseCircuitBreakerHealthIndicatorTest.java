package com.udea.bancodigital.reporting.infrastructure.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportingDatabaseCircuitBreakerHealthIndicatorTest {

    @Mock
    private CircuitBreakerRegistry registry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private ReportingDatabaseCircuitBreakerHealthIndicator healthIndicator;

    @Mock
    private CircuitBreaker.Metrics metrics;

    @BeforeEach
    void setUp() {
        when(registry.circuitBreaker("reporting-database")).thenReturn(circuitBreaker);
        when(circuitBreaker.getMetrics()).thenReturn(metrics);
        when(metrics.getFailureRate()).thenReturn(0.0f);
        when(metrics.getNumberOfBufferedCalls()).thenReturn(0);
        when(metrics.getNumberOfFailedCalls()).thenReturn(0);
        when(metrics.getNumberOfSuccessfulCalls()).thenReturn(0);
        when(metrics.getNumberOfSlowCalls()).thenReturn(0);
        when(metrics.getSlowCallRate()).thenReturn(0.0f);
    }

    @Test
    void health_ShouldReturnUp_WhenStateIsClosed() {
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);
        Health health = healthIndicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals("CLOSED", health.getDetails().get("state"));
    }

    @Test
    void health_ShouldReturnOutOfService_WhenStateIsOpen() {
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);
        Health health = healthIndicator.health();
        assertEquals(Status.OUT_OF_SERVICE, health.getStatus());
        assertEquals("OPEN", health.getDetails().get("state"));
    }

    @Test
    void health_ShouldReturnDown_WhenStateIsHalfOpen() {
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.HALF_OPEN);
        Health health = healthIndicator.health();
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("HALF_OPEN", health.getDetails().get("state"));
    }

    @Test
    void health_ShouldReturnUp_WhenStateIsDisabled() {
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.DISABLED);
        Health health = healthIndicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals("DISABLED", health.getDetails().get("state"));
    }

    @Test
    void health_ShouldReturnDown_WhenRegistryThrowsException() {
        when(registry.circuitBreaker("reporting-database")).thenThrow(new RuntimeException("Registry error"));
        
        // Need a new healthIndicator because setUp is called before each test
        ReportingDatabaseCircuitBreakerHealthIndicator errorIndicator = new ReportingDatabaseCircuitBreakerHealthIndicator(registry);
        Health health = errorIndicator.health();
        
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Registry error", health.getDetails().get("reason"));
    }
}
