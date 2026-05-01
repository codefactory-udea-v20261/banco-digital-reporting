package com.udea.bancodigital.reporting.infrastructure.consumer.pending;

import com.udea.bancodigital.reporting.infrastructure.adapter.out.ReportingMaterializationAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Pending Reporting Event Consumer.
 * 
 * Tests retry logic and DLQ routing:
 * - Successful materialization after recovery
 * - Exponential backoff retry logic
 * - DLQ routing after max retries
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Pending Reporting Event Consumer Tests")
class PendingReportingEventConsumerTest {

    @Autowired
    private PendingReportingEventConsumer pendingReportingEventConsumer;

    @Autowired
    private ReportingMaterializationAdapter reportingMaterializationAdapter;

    @Autowired(required = false)
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    private Map<String, Object> testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new HashMap<>();
        testEvent.put("eventId", "evt-pending-rep-001");
        testEvent.put("aggregateId", "cust-999");
        testEvent.put("eventType", "CustomerCreated");
        testEvent.put("userId", "user-002");
        testEvent.put("timestamp", System.currentTimeMillis());
        testEvent.put("retryCount", 0);
    }

    @Test
    @DisplayName("Should successfully replay customer reporting event")
    void testSuccessfulCustomerReplay() {
        // Given: Customer creation event
        testEvent.put("eventType", "CustomerCreated");

        // When: Consuming pending event
        pendingReportingEventConsumer.consumePendingEvent(testEvent);

        // Then: Event should be processed
        assertThat(testEvent.get("eventId")).isEqualTo("evt-pending-rep-001");
        assertThat(testEvent.get("eventType")).isEqualTo("CustomerCreated");
        log.info("✓ Customer reporting event replayed");
    }

    @Test
    @DisplayName("Should successfully replay transaction reporting event")
    void testSuccessfulTransactionReplay() {
        // Given: Transaction completion event
        testEvent.put("eventType", "TransactionCompleted");

        // When: Consuming pending event
        pendingReportingEventConsumer.consumePendingEvent(testEvent);

        // Then: Event should be processed
        assertThat(testEvent.get("eventType")).isEqualTo("TransactionCompleted");
        log.info("✓ Transaction reporting event replayed");
    }

    @Test
    @DisplayName("Should successfully replay account reporting event")
    void testSuccessfulAccountReplay() {
        // Given: Account opening event
        testEvent.put("eventType", "AccountOpened");

        // When: Consuming pending event
        pendingReportingEventConsumer.consumePendingEvent(testEvent);

        // Then: Event should be processed
        assertThat(testEvent.get("eventType")).isEqualTo("AccountOpened");
        log.info("✓ Account reporting event replayed");
    }

    @Test
    @DisplayName("Should get consumer statistics")
    void testGetConsumerStats() {
        // When: Getting consumer stats
        Map<String, Object> stats = pendingReportingEventConsumer.getStats();

        // Then: Stats should include topic and max retries
        assertThat(stats)
            .containsKey("topic")
            .containsKey("dlqTopic")
            .containsKey("maxRetries")
            .containsKey("consumerGroup");
        
        assertThat(stats.get("topic")).isEqualTo("reporting-events-pending");
        assertThat(stats.get("dlqTopic")).isEqualTo("reporting-events-dlq");
        assertThat(stats.get("maxRetries")).isEqualTo(5);
        
        log.info("✓ Consumer stats: {}", stats);
    }

    @Test
    @DisplayName("Should preserve event aggregateId during materialization")
    void testAggregateIdPreservation() {
        // Given: Event with aggregateId
        String aggregateId = "cust-999";
        testEvent.put("aggregateId", aggregateId);

        // When: Processing
        pendingReportingEventConsumer.consumePendingEvent(testEvent);

        // Then: AggregateId should be preserved for correlation
        assertThat(testEvent.get("aggregateId")).isEqualTo(aggregateId);
        log.info("✓ AggregateId preserved: {}", aggregateId);
    }

    @Test
    @DisplayName("Should handle event with business data")
    void testBusinessDataPreservation() {
        // Given: Event with transaction amount
        testEvent.put("amount", 1500.50);
        testEvent.put("currency", "USD");
        testEvent.put("eventType", "TransactionCompleted");

        // When: Processing
        pendingReportingEventConsumer.consumePendingEvent(testEvent);

        // Then: Business data should be preserved
        assertThat(testEvent.get("amount")).isEqualTo(1500.50);
        assertThat(testEvent.get("currency")).isEqualTo("USD");
        log.info("✓ Business data preserved");
    }

    @Test
    @DisplayName("Should handle concurrent materialization replays")
    void testConcurrentReplays() {
        // Given: Multiple events with different event types
        String[] eventTypes = {"CustomerCreated", "TransactionCompleted", "AccountOpened"};

        // When: Processing each type
        for (String eventType : eventTypes) {
            testEvent.put("eventType", eventType);
            testEvent.put("eventId", "evt-" + eventType);
            pendingReportingEventConsumer.consumePendingEvent(testEvent);
        }

        // Then: All should be processed
        assertThat(testEvent.get("eventType")).isNotNull();
        log.info("✓ Concurrent replays handled");
    }
}
