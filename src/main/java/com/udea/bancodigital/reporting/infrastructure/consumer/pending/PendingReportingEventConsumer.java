package com.udea.bancodigital.reporting.infrastructure.consumer.pending;

import com.udea.bancodigital.reporting.infrastructure.adapter.out.ReportingMaterializationAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.Map;

/**
 * Pending Event Consumer for Reporting Service.
 *
 * Handles retry and replay of failed reporting materialization:
 * - Listens to: reporting-events-pending
 * - Retry logic: exponential backoff
 * - DLQ: reporting-events-dlq (after max retries)
 * - Auto-recovery: When Reporting Database comes back online
 *
 * Flow:
 * 1. Event queued to pending topic when BD_REPORT is down
 * 2. PendingReportingEventConsumer picks it up
 * 3. Attempts to materialize view via ReportingMaterializationAdapter
 * 4. If fails: retries with exponential backoff
 * 5. After maxRetries: moved to DLQ for investigation
 * 6. When BD_REPORT recovers: events automatically materialized
 */
@Slf4j
@Profile("!prod")
@Component
@RequiredArgsConstructor
public class PendingReportingEventConsumer {

    private static final String PENDING_TOPIC = "reporting-events-pending";
    private static final String DLQ_TOPIC = "reporting-events-dlq";
    private static final String CONSUMER_GROUP = "reporting-pending";
    private static final int MAX_RETRIES = 5;

    private final ReportingMaterializationAdapter reportingMaterializationAdapter;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    /**
     * Consume pending reporting events and retry materialization.
     */
    @KafkaListener(
            topics = PENDING_TOPIC,
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePendingEvent(Map<String, Object> event) {
        try {
            String eventId = String.valueOf(event.get("eventId"));
            String eventType = String.valueOf(event.get("eventType"));
            int retryCount = (int) event.getOrDefault("retryCount", 0);

            log.info("Processing pending reporting event: "
                + "eventId={}, type={}, retryCount={}/{}",
                eventId, eventType, retryCount, MAX_RETRIES);

            // Attempt to materialize reporting view
            reportingMaterializationAdapter.materializeReportingView(event, eventType);

            log.info("Successfully replayed reporting event: eventId={}, type={}", 
                eventId, eventType);

        } catch (Exception e) {
            handleRetry(event, e);
        }
    }

    /**
     * Handle retry logic with exponential backoff and DLQ routing.
     */
    private void handleRetry(Map<String, Object> event, Exception e) {
        int retryCount = (int) event.getOrDefault("retryCount", 0);

        if (retryCount >= MAX_RETRIES) {
            // Max retries reached, move to DLQ
            moveToDLQ(event, "Max retries exceeded: " + e.getMessage());
        } else {
            // Retry with exponential backoff
            retryWithBackoff(event, retryCount);
        }
    }

    /**
     * Retry with exponential backoff.
     * Backoff: attempt 1 = 1s, attempt 2 = 2s, attempt 3 = 4s, etc.
     */
    private void retryWithBackoff(Map<String, Object> event, int retryCount) {
        long backoffMs = (long) Math.pow(2, retryCount) * 1000; // 1s, 2s, 4s, 8s, 16s
        int nextRetry = retryCount + 1;

        log.warn("Retry failed for eventId={}. Scheduling retry {} of {}, "
            + "backoff={}ms",
            event.get("eventId"), nextRetry, MAX_RETRIES, backoffMs);

        // Update retry metadata
        event.put("retryCount", nextRetry);
        event.put("lastRetryAt", Instant.now().toString());
        event.put("nextRetryScheduledAt", 
            Instant.now().plusMillis(backoffMs).toString());

        // Re-queue to pending topic
        kafkaTemplate.send(PENDING_TOPIC, String.valueOf(event.get("eventId")), event);
    }

    /**
     * Move event to DLQ after max retries exceeded.
     */
    private void moveToDLQ(Map<String, Object> event, String reason) {
        log.error("Moving reporting event to DLQ. "
            + "eventId={}, reason={}",
            event.get("eventId"), reason);

        // Add failure metadata
        event.put("failedAt", Instant.now().toString());
        event.put("failureReason", reason);
        event.put("movedToDLQAt", Instant.now().toString());

        // Send to DLQ
        kafkaTemplate.send(DLQ_TOPIC, String.valueOf(event.get("eventId")), event);

        log.info("Event moved to DLQ for investigation. "
            + "Topic={}, eventId={}",
            DLQ_TOPIC, event.get("eventId"));
    }

    /**
     * Get consumer statistics.
     */
    public Map<String, Object> getStats() {
        return Map.of(
            "topic", PENDING_TOPIC,
            "dlqTopic", DLQ_TOPIC,
            "maxRetries", MAX_RETRIES,
            "consumerGroup", CONSUMER_GROUP
        );
    }
}
