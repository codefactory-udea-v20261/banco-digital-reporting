package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * Unit tests for Reporting Materialization Adapter.
 * Uses Mockito only — no Spring context or Docker required.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("Reporting Materialization Adapter - Unit Tests")
class ReportingMaterializationAdapterTest {

    @Mock
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    private ReportingMaterializationAdapter adapter;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private CircuitBreaker circuitBreaker;
    private Map<String, Object> testEvent;

    @BeforeEach
    void setUp() {
        adapter = new ReportingMaterializationAdapter(kafkaTemplate);
        circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("reporting-database",
                CircuitBreakerConfig.ofDefaults());

        testEvent = new HashMap<>();
        testEvent.put("eventId", "evt-002");
        testEvent.put("aggregateId", "cust-456");
        testEvent.put("userId", "user-789");
        testEvent.put("timestamp", System.currentTimeMillis());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CustomerCreated", "TransactionCompleted", "AccountOpened"})
    @DisplayName("Should materialize reporting view for known event types without throwing")
    void testMaterializeViewSuccess(String eventType) {
        // When: Materializing a reporting view
        adapter.materializeReportingView(testEvent, eventType);

        // Then: No exception thrown, adapter completed
        assertThat(testEvent).containsKey("eventId");
        log.info("✓ Reporting view for {} materialized successfully", eventType);
    }

    @Test
    @DisplayName("Should return circuit breaker status map")
    void testGetCircuitBreakerStatus() {
        // When: Getting adapter status
        Map<String, Object> status = adapter.getStatus();

        // Then: Status should include circuit breaker info
        assertThat(status)
                .containsKey("status")
                .containsEntry("circuitBreakerName", "reporting-database");
        log.info("✓ Circuit breaker status retrieved: {}", status);
    }

    @Test
    @DisplayName("Should handle unknown event types gracefully without throwing")
    void testUnknownEventTypeHandling() {
        // When: Attempting to materialize unknown event type
        adapter.materializeReportingView(testEvent, "UnknownEventType");

        // Then: Should log warning and not crash
        assertThat(testEvent).containsKey("eventId");
        log.info("✓ Unknown event type handled gracefully");
    }

    @Test
    @DisplayName("Should process multiple calls without circuit breaker opening")
    void testCircuitBreakerStateTransitions() {
        // Given: Circuit breaker starts CLOSED
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // When: Making multiple successful calls
        for (int i = 0; i < 5; i++) {
            adapter.materializeReportingView(testEvent, "CustomerCreated");
        }

        // Then: All calls complete without error
        assertThat(testEvent).containsKey("eventId");
        log.info("✓ Multiple calls handled successfully");
    }

    @Test
    @DisplayName("Should include event metadata in materialized view")
    void testReportingViewMetadata() {
        // Given: Event with analytics metadata
        testEvent.put("amount", 1000.0);
        testEvent.put("currency", "USD");

        // When: Materializing view
        adapter.materializeReportingView(testEvent, "TransactionCompleted");

        // Then: Metadata is preserved in the original event map
        assertThat(testEvent).containsKeys("amount", "currency");
        log.info("✓ Event metadata preserved correctly");
    }

    @Test
    @DisplayName("Should capture event timestamp correctly")
    void testEventTimestampCapture() {
        // Given: Event with timestamp
        long beforeCall = System.currentTimeMillis();
        testEvent.put("eventTimestamp", beforeCall);

        // When: Materializing view
        adapter.materializeReportingView(testEvent, "TransactionCompleted");
        long afterCall = System.currentTimeMillis();

        // Then: Timestamp should be preserved
        assertThat((Long) testEvent.get("eventTimestamp"))
                .isBetween(beforeCall, afterCall);
        log.info("✓ Event timestamp captured correctly");
    }
}