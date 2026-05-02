package com.udea.bancodigital.reporting.infrastructure.consumer;

import com.udea.bancodigital.reporting.infrastructure.adapter.out.ReportingMaterializationAdapter;
import com.udea.bancodigital.shared.event.DomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingEventConsumerTest {

    @Mock
    private ReportingMaterializationAdapter reportingMaterializationAdapter;

    @InjectMocks
    private ReportingEventConsumer consumer;

    @Test
    void consumeEvent_ShouldProcessCustomerCreated() {
        DomainEvent event = createEvent("CustomerCreated");
        
        consumer.consumeEvent(event, "topic", 0, 1L);
        
        verify(reportingMaterializationAdapter).materializeReportingView(any(Map.class), eq("CustomerCreated"));
    }

    @Test
    void consumeEvent_ShouldProcessTransactionCompleted() {
        DomainEvent event = createEvent("TransactionCompleted");
        
        consumer.consumeEvent(event, "topic", 0, 1L);
        
        verify(reportingMaterializationAdapter).materializeReportingView(any(Map.class), eq("TransactionCompleted"));
    }

    @Test
    void consumeEvent_ShouldProcessAccountOpened() {
        DomainEvent event = createEvent("AccountOpened");
        
        consumer.consumeEvent(event, "topic", 0, 1L);
        
        verify(reportingMaterializationAdapter).materializeReportingView(any(Map.class), eq("AccountOpened"));
    }

    @Test
    void consumeEvent_ShouldHandleUnknownEventType() {
        DomainEvent event = createEvent("UnknownEvent");
        
        consumer.consumeEvent(event, "topic", 0, 1L);
        
        verify(reportingMaterializationAdapter, never()).materializeReportingView(any(), any());
    }

    @Test
    void consumeEvent_ShouldHandleException() {
        DomainEvent event = createEvent("CustomerCreated");
        doThrow(new RuntimeException("Error")).when(reportingMaterializationAdapter).materializeReportingView(any(), any());
        
        // Should not throw exception
        consumer.consumeEvent(event, "topic", 0, 1L);
        
        verify(reportingMaterializationAdapter).materializeReportingView(any(), any());
    }

    private DomainEvent createEvent(String type) {
        return DomainEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateId(UUID.randomUUID().toString())
                .eventType(type)
                .occurredAt(LocalDateTime.now())
                .userId(UUID.randomUUID().toString())
                .version(1)
                .build();
    }
}
