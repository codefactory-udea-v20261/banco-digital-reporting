package com.udea.bancodigital.reporting.infrastructure.consumer;

import com.udea.bancodigital.reporting.infrastructure.adapter.out.ReportingMaterializationAdapter;
import com.udea.bancodigital.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer for reporting events.
 * Listens to the main event topic and materializes reporting views.
 * 
 * With Circuit Breaker protection: if reporting database fails,
 * events are queued to Kafka for later materialization.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportingEventConsumer {

    private static final String EVENTS_TOPIC = "banco-digital-events";
    private static final String CONSUMER_GROUP = "reporting-service";

    private final ReportingMaterializationAdapter reportingMaterializationAdapter;

    /**
     * Consumes events from the main event bus and updates reporting views with resilience.
     */
    @KafkaListener(
            topics = EVENTS_TOPIC,
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEvent(
            @Payload DomainEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            log.info("Received event for reporting: {} (id: {}) from topic: {}, partition: {}, offset: {}",
                    event.getEventType(),
                    event.getEventId(),
                    topic,
                    partition,
                    offset);

            processEventForReporting(event);

            log.debug("Successfully processed event for reporting: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to process reporting event {}: {}", event.getEventId(), e.getMessage(), e);
            // Resilience4j circuit breaker handles fallback via Kafka queueing
        }
    }

    /**
     * Routes the event to the appropriate reporting handler based on event type.
     */
    private void processEventForReporting(DomainEvent event) {
        switch (event.getEventType()) {
            case "CustomerCreated":
                updateCustomerReporting(event);
                break;
            case "TransactionCompleted":
                updateTransactionReporting(event);
                break;
            case "AccountOpened":
                updateAccountReporting(event);
                break;
            default:
                log.warn("Unknown event type for reporting: {}", event.getEventType());
        }
    }

    /**
     * Update customer analytics when a new customer is created.
     * Materialized with circuit breaker protection.
     */
    private void updateCustomerReporting(DomainEvent event) {
        log.info("[REPORTING] Updating customer analytics for: {}", event.getAggregateId());
        
        // Materialize customer reporting views with circuit breaker protection
        Map<String, Object> reportingEvent = eventToMap(event);
        reportingMaterializationAdapter.materializeReportingView(reportingEvent, "CustomerCreated");
    }

    /**
     * Update transaction analytics when a transaction completes.
     * Materialized with circuit breaker protection.
     */
    private void updateTransactionReporting(DomainEvent event) {
        log.info("[REPORTING] Updating transaction analytics for: {}", event.getAggregateId());
        
        // Materialize transaction reporting views with circuit breaker protection
        Map<String, Object> reportingEvent = eventToMap(event);
        reportingMaterializationAdapter.materializeReportingView(reportingEvent, "TransactionCompleted");
    }

    /**
     * Update account analytics when an account is opened.
     * Materialized with circuit breaker protection.
     */
    private void updateAccountReporting(DomainEvent event) {
        log.info("[REPORTING] Updating account analytics for: {}", event.getAggregateId());
        
        // Materialize account reporting views with circuit breaker protection
        Map<String, Object> reportingEvent = eventToMap(event);
        reportingMaterializationAdapter.materializeReportingView(reportingEvent, "AccountOpened");
    }

    /**
     * Convert DomainEvent to Map for materialization.
     */
    private Map<String, Object> eventToMap(DomainEvent event) {
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("eventId", event.getEventId());
        eventMap.put("aggregateId", event.getAggregateId());
        eventMap.put("eventType", event.getEventType());
        eventMap.put("userId", event.getUserId());
        eventMap.put("timestamp", event.getOccurredAt());
        eventMap.put("version", event.getVersion());
        return eventMap;
    }
}
