package com.udea.bancodigital.reporting.infrastructure.consumer.pending;

import com.udea.bancodigital.reporting.infrastructure.adapter.out.ReportingMaterializationAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingReportingEventConsumerTest {

    @Mock
    private ReportingMaterializationAdapter reportingMaterializationAdapter;

    @Mock
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @InjectMocks
    private PendingReportingEventConsumer consumer;

    private Map<String, Object> event;

    @BeforeEach
    void setUp() {
        event = new HashMap<>();
        event.put("eventId", "evt-123");
        event.put("eventType", "AccountOpened");
        event.put("retryCount", 0);
    }

    @Test
    void consumePendingEvent_Success() {
        consumer.consumePendingEvent(event);
        
        verify(reportingMaterializationAdapter).materializeReportingView(event, "AccountOpened");
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void consumePendingEvent_Failure_Retry() {
        doThrow(new RuntimeException("DB Error"))
            .when(reportingMaterializationAdapter).materializeReportingView(any(), anyString());

        consumer.consumePendingEvent(event);

        verify(kafkaTemplate).send(eq("reporting-events-pending"), eq("evt-123"), any());
        assertEquals(1, event.get("retryCount"));
        assertNotNull(event.get("lastRetryAt"));
        assertNotNull(event.get("nextRetryScheduledAt"));
    }

    @Test
    void consumePendingEvent_MaxRetries_MoveToDLQ() {
        event.put("retryCount", 5); // MAX_RETRIES = 5
        doThrow(new RuntimeException("Final Error"))
            .when(reportingMaterializationAdapter).materializeReportingView(any(), anyString());

        consumer.consumePendingEvent(event);

        verify(kafkaTemplate).send(eq("reporting-events-dlq"), eq("evt-123"), any());
        assertNotNull(event.get("failedAt"));
        assertEquals("Max retries exceeded: Final Error", event.get("failureReason"));
    }

    @Test
    void getStats() {
        Map<String, Object> stats = consumer.getStats();
        assertEquals("reporting-events-pending", stats.get("topic"));
        assertEquals("reporting-events-dlq", stats.get("dlqTopic"));
        assertEquals(5, stats.get("maxRetries"));
        assertEquals("reporting-pending", stats.get("consumerGroup"));
    }
}
