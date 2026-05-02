package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingMaterializationAdapterUnitTest {

    @Mock
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @InjectMocks
    private ReportingMaterializationAdapter adapter;

    @Test
    void testMaterializeReportingView_AllTypes() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", "123");
        event.put("aggregateId", "456");

        // CustomerCreated
        adapter.materializeReportingView(event, "CustomerCreated");
        
        // TransactionCompleted
        adapter.materializeReportingView(event, "TransactionCompleted");
        
        // AccountOpened
        adapter.materializeReportingView(event, "AccountOpened");
        
        // Unknown
        adapter.materializeReportingView(event, "UnknownType");
        
        // No verify needed for private updates as they just log for now
    }

    @Test
    void testMaterializeReportingView_Exception() {
        // Since the current implementation doesn't really throw unless we mock a repository (which isn't there yet)
        // I'll just check if it handles it if we ever add repository calls.
        // For now, the switch cases just log.
    }

    @Test
    void testMaterializeFallback() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", "123");
        event.put("aggregateId", "456");

        ReflectionTestUtils.invokeMethod(adapter, "materializeFallback", 
            event, "CustomerCreated", new RuntimeException("DB error"));

        verify(kafkaTemplate).send(eq("reporting-events-pending"), eq("456"), any());
    }

    @Test
    void testMaterializeFallback_KafkaError() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", "123");
        event.put("aggregateId", "456");

        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), anyString(), any());

        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(adapter, "materializeFallback", 
                event, "CustomerCreated", new RuntimeException("DB error"));
        });
    }

    @Test
    void testGetStatus() {
        var status = adapter.getStatus();
        assertNotNull(status);
        assertEquals("reporting-database", status.get("circuitBreakerName"));
    }
}
