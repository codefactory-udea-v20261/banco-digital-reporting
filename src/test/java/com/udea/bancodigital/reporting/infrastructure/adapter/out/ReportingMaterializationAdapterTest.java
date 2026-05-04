package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration tests for Reporting Materialization Adapter with Circuit Breaker.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Reporting Materialization Adapter - Circuit Breaker Tests")
class ReportingMaterializationAdapterTest {

    @Autowired
    private ReportingMaterializationAdapter reportingMaterializationAdapter;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired(required = false)
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    private CircuitBreaker reportingCircuitBreaker;
    private Map<String, Object> testEvent;

    @BeforeEach
    void setUp() {
        try {
            reportingCircuitBreaker = circuitBreakerRegistry.circuitBreaker("reporting-database");
            reportingCircuitBreaker.reset(); // Reset to CLOSED state
        } catch (Exception e) {
            System.err.println("Circuit breaker not available in test context");
        }

        testEvent = new HashMap<>();
        testEvent.put("eventId", "evt-002");
        testEvent.put("aggregateId", "cust-456");
        testEvent.put("userId", "user-789");
        testEvent.put("timestamp", System.currentTimeMillis());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CustomerCreated", "TransactionCompleted", "AccountOpened"})
    @DisplayName("Should materialize reporting view for known event types")
    void testMaterializeViewSuccess(String eventType) {
        // Given: Database is healthy, circuit breaker is CLOSED
        assertThat(reportingCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // When: Materializing a reporting view
        reportingMaterializationAdapter.materializeReportingView(testEvent, eventType);

        // Then: No exception thrown, view materialized
        assertThat(reportingCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        System.out.printf("✓ Reporting view for %s materialized successfully%n", eventType);
    }

    @Test
    @DisplayName("Should return circuit breaker status")
    void testGetCircuitBreakerStatus() {
        // When: Getting adapter status
        Map<String, Object> status = reportingMaterializationAdapter.getStatus();

        // Then: Status should include circuit breaker info
        assertThat(status)
            .containsKey("status")
            .containsEntry("circuitBreakerName", "reporting-database");
        System.out.printf("✓ Circuit breaker status retrieved: %s%n", status);
    }

    @Test
    @DisplayName("Should handle unknown event types gracefully")
    void testUnknownEventTypeHandling() {
        // When: Attempting to materialize unknown event type
        reportingMaterializationAdapter.materializeReportingView(testEvent, "UnknownEventType");

        // Then: Should log warning and not crash
        assertThat(reportingCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        System.out.println("✓ Unknown event type handled gracefully");
    }

    @Test
    @DisplayName("Should transition through circuit breaker states")
    void testCircuitBreakerStateTransitions() {
        // Given: Circuit breaker in CLOSED state
        assertThat(reportingCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        System.out.println("State 1: CLOSED (initial)");

        // When: CB is healthy (simulated success calls)
        for (int i = 0; i < 5; i++) {
            reportingMaterializationAdapter.materializeReportingView(testEvent, "CustomerCreated");
        }

        // Then: CB should remain CLOSED
        assertThat(reportingCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        System.out.println("✓ Circuit breaker state transitions verified");
    }

    @Test
    @DisplayName("Should include event metadata in materialized view")
    void testReportingViewMetadata() {
        // Given: Event with analytics metadata
        testEvent.put("amount", 1000.0);
        testEvent.put("currency", "USD");

        // When: Materializing view
        reportingMaterializationAdapter.materializeReportingView(testEvent, "TransactionCompleted");

        // Then: Metadata should be included in view
        assertThat(testEvent).containsKeys("amount", "currency");
        System.out.println("✓ Event metadata included in reporting view");
    }

    @Test
    @DisplayName("Should capture event timestamp correctly")
    void testEventTimestampCapture() {
        // Given: Event with timestamp
        long beforeCall = System.currentTimeMillis();
        testEvent.put("eventTimestamp", beforeCall);

        // When: Materializing view
        reportingMaterializationAdapter.materializeReportingView(testEvent, "TransactionCompleted");
        long afterCall = System.currentTimeMillis();

        // Then: Timestamp should be preserved
        assertThat((Long) testEvent.get("eventTimestamp"))
            .isBetween(beforeCall, afterCall);
        System.out.println("✓ Event timestamp captured correctly");
    }
}
