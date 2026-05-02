package com.udea.bancodigital.reporting.infrastructure.consumer.dlq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class ReportingDLQConsumerTest {

    @InjectMocks
    private ReportingDLQConsumer consumer;

    @Test
    void consumeDLQEvent_ShouldProcessSuccessfully() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "CustomerCreated");
        event.put("retryCount", 3);
        event.put("failureReason", "Database timeout");

        assertDoesNotThrow(() -> consumer.consumeDLQEvent(event));
    }

    @Test
    void consumeDLQEvent_ShouldHandleMissingFields() {
        Map<String, Object> event = new HashMap<>();
        // missing fields should be handled by String.valueOf or getOrDefault
        assertDoesNotThrow(() -> consumer.consumeDLQEvent(event));
    }
}
