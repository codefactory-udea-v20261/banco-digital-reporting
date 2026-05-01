package com.udea.bancodigital.reporting.infrastructure.consumer.dlq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Dead Letter Queue Consumer for Reporting Service.
 *
 * Monitors and alerts on failed reporting materialization that exhausted retries.
 * 
 * In production, should integrate with:
 * - Slack for alerts (non-critical, but should be tracked)
 * - Database for DLQ event persistence
 * - Metrics for tracking DLQ growth
 * - Analytics on missing reports
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportingDLQConsumer {

    private static final String DLQ_TOPIC = "reporting-events-dlq";
    private static final String CONSUMER_GROUP = "reporting-dlq";

    /**
     * Consume reporting DLQ events.
     * These are reporting events that failed to materialize after max retries.
     */
    @KafkaListener(
            topics = DLQ_TOPIC,
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDLQEvent(Map<String, Object> event) {
        String eventId = String.valueOf(event.get("eventId"));
        String eventType = String.valueOf(event.get("eventType"));
        int retryCount = (int) event.getOrDefault("retryCount", 0);
        String reason = String.valueOf(event.get("failureReason"));

        log.error("REPORTING DLQ EVENT - eventId={}, type={}, retries={}, reason={}",
            eventId, eventType, retryCount, reason);

        // In production: send alert to Slack
        // alertingService.sendAlert(
        //     "Reporting Materialization Failed",
        //     String.format("EventId: %s, Type: %s, Retries: %d, Reason: %s",
        //         eventId, eventType, retryCount, reason)
        // );

        // In production: persist for analysis
        // reportingDLQPersistenceService.recordFailedMaterialization(event);

        // Increment metrics
        // meterRegistry.counter("dlq.reporting-events").increment();
    }
}
