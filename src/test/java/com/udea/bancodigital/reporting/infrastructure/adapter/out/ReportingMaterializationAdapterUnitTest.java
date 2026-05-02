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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportingMaterializationAdapterUnitTest {

    @Mock
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @InjectMocks
    private ReportingMaterializationAdapter adapter;

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
    void testGetStatus() {
        var status = adapter.getStatus();
        org.junit.jupiter.api.Assertions.assertNotNull(status);
    }
}
