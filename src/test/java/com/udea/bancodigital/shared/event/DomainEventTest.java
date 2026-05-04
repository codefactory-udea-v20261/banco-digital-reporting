package com.udea.bancodigital.shared.event;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventTest {

    @Test
    void testDomainEvent() {
        String eventId = UUID.randomUUID().toString();
        String aggId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        DomainEvent event = DomainEvent.builder()
                .eventId(eventId)
                .eventType("TEST")
                .aggregateId(aggId)
                .correlationId("corr")
                .sagaId("saga")
                .occurredAt(now)
                .sourceService("test-service")
                .userId(userId)
                .version(1)
                .build();

        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getEventType()).isEqualTo("TEST");
        assertThat(event.getAggregateId()).isEqualTo(aggId);
        assertThat(event.getCorrelationId()).isEqualTo("corr");
        assertThat(event.getSagaId()).isEqualTo("saga");
        assertThat(event.getOccurredAt()).isEqualTo(now);
        assertThat(event.getSourceService()).isEqualTo("test-service");
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getVersion()).isEqualTo(1);

        event.setEventType("TEST2");
        assertThat(event.getEventType()).isEqualTo("TEST2");

        assertThat(event.toString()).isNotBlank();
        assertThat(event.hashCode()).isNotZero();
        assertThat(event).isEqualTo(event);
        assertThat(event).isNotEqualTo(new Object());
    }
}
