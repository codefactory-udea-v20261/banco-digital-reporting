package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Reporting Materialized Views Adapter with Circuit Breaker Protection.
 *
 * Materializes reporting views from domain events with resilience:
 * - Circuit breaker: if reporting DB fails >50% of time
 * - Fallback: queue event to Kafka for later materialization
 * - Retry: exponential backoff 1s, 2s, 4s
 *
 * Pattern: Circuit Breaker + CQRS (Command Query Responsibility Segregation)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportingMaterializationAdapter {

    private static final String EVENT_ID = "eventId";
    private static final String AGGREGATE_ID = "aggregateId";


    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    /**
     * Materialize reporting view from event with circuit breaker protection.
     * Fallback: queue event to Kafka if DB is unavailable.
     */
    @CircuitBreaker(name = "reporting-database", fallbackMethod = "materializeFallback")
    @Retry(name = "reporting-database")
    public void materializeReportingView(Map<String, Object> event, String eventType) {
        log.debug("Materializing reporting view: type={}, eventId={}", 
            eventType, event.get(EVENT_ID));

        try {
            // Create materialized view entry
            Map<String, Object> reportingView = new HashMap<>(event);
            reportingView.put("eventType", eventType);
            reportingView.put("viewTimestamp", Instant.now().toString());
            reportingView.put("materializedAt", System.currentTimeMillis());

            // FUTURE: Persist to BD_REPORT via repository
            // Switch on event type to update different reporting tables
            switch (eventType) {
                case "CustomerCreated":
                    updateCustomerReportingView(reportingView);
                    break;
                case "TransactionCompleted":
                    updateTransactionReportingView(reportingView);
                    break;
                case "AccountOpened":
                    updateAccountReportingView(reportingView);
                    break;
                default:
                    log.warn("Unknown event type for materialization: {}", eventType);
            }

            log.info("Successfully materialized reporting view: type={}, eventId={}", 
                eventType, event.get(EVENT_ID));

        } catch (Exception e) {
            log.error("Database error materializing reporting view: type={}, error={}", 
                eventType, e.getMessage());
            throw e; // Let circuit breaker + retry handle it
        }
    }

    /**
     * Fallback when reporting database is down or circuit breaker is open.
     * Queue event to Kafka for later materialization with persistent queue.
     */
    private void materializeFallback(Map<String, Object> event, String eventType, Exception e) {
        log.warn("Reporting database unavailable (circuit breaker OPEN). "
            + "Queueing event for async materialization. Type={}, Error: {}",
            eventType, e.getMessage());

        try {
            // Create pending reporting event
            Map<String, Object> pendingEvent = new HashMap<>(event);
            pendingEvent.put("eventType", eventType);
            pendingEvent.put("originalEventId", event.get(EVENT_ID));
            pendingEvent.put("timestamp", Instant.now().toString());
            pendingEvent.put("retryCount", 0);
            pendingEvent.put("reason", "Reporting database unavailable");

            // Send to pending queue for replay
            kafkaTemplate.send("reporting-events-pending", 
                String.valueOf(event.get(AGGREGATE_ID)), 
                pendingEvent);

            log.info("Queued pending reporting event for aggregateId={}", event.get(AGGREGATE_ID));

        } catch (Exception kafkaError) {
            log.error("Failed to queue fallback reporting event: {}", kafkaError.getMessage());
            // Critical: log but don't throw (consumer shouldn't crash)
        }
    }

    /**
     * Update customer reporting metrics from CustomerCreated event.
     */
    private void updateCustomerReportingView(Map<String, Object> event) {
        log.debug("[REPORTING] Updating customer metrics for aggregateId={}", 
            event.get(AGGREGATE_ID));
        // FUTURE: Update customer_metrics table in BD_REPORT
    }

    /**
     * Update transaction reporting analytics from TransactionCompleted event.
     */
    private void updateTransactionReportingView(Map<String, Object> event) {
        log.debug("[REPORTING] Updating transaction metrics for aggregateId={}", 
            event.get(AGGREGATE_ID));
        // FUTURE: Update transaction_metrics table in BD_REPORT
    }

    /**
     * Update account reporting analytics from AccountOpened event.
     */
    private void updateAccountReportingView(Map<String, Object> event) {
        log.debug("[REPORTING] Updating account metrics for aggregateId={}", 
            event.get(AGGREGATE_ID));
        // FUTURE: Update account_metrics table in BD_REPORT
    }

    /**
     * Get circuit breaker health status.
     */
    public Map<String, Object> getStatus() {
        return Map.of(
            "circuitBreakerName", "reporting-database",
            "status", "Use /actuator/health/reporting-database for details"
        );
    }
}
