package com.udea.bancodigital.reporting;

import com.udea.bancodigital.shared.event.DomainEvent;
import com.udea.bancodigital.shared.util.AuditableEntity;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CoverageBoosterTest {

    @Test
    void testMainMethod() {
        // Cobertura de la clase principal sin arrancar servidor real
        ReportingApplication.main(new String[]{
            "--spring.main.web-application-type=none",
            "--spring.flyway.enabled=false",
            "--spring.cloud.vault.enabled=false",
            "--eureka.client.enabled=false"
        });
    }

    @Test
    void testDomainEventFullCoverage() {
        DomainEvent e1 = DomainEvent.builder().eventId("1").eventType("type").aggregateId("agg").correlationId("corr").sagaId("saga").occurredAt(LocalDateTime.now()).sourceService("src").version(1).userId("user").build();
        DomainEvent e2 = DomainEvent.builder().eventId("1").eventType("type").aggregateId("agg").correlationId("corr").sagaId("saga").occurredAt(e1.getOccurredAt()).sourceService("src").version(1).userId("user").build();
        DomainEvent e3 = DomainEvent.builder().eventId("2").build();

        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertNotEquals(e1, null);
        assertNotEquals(e1, new Object());
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotNull(e1.toString());
        
        // Manual setters for more branches
        e1.setEventId("new");
        assertEquals("new", e1.getEventId());
    }

    @Test
    void testAuditableEntityLifecycle() {
        TestEntity entity = new TestEntity();
        
        // PrePersist with nulls
        entity.prePersist();
        assertNotNull(entity.getCreatedAt());
        assertEquals("SYSTEM", entity.getCreatedBy());
        
        // PrePersist with partial data
        TestEntity entity2 = new TestEntity();
        entity2.setCreatedBy("ADMIN");
        entity2.prePersist();
        assertEquals("ADMIN", entity2.getCreatedBy());
        assertEquals("ADMIN", entity2.getUpdatedBy());

        // PreUpdate
        entity2.setUpdatedBy(null);
        entity2.preUpdate();
        assertEquals("SYSTEM", entity2.getUpdatedBy());
    }

    private static class TestEntity extends AuditableEntity {
        public void prePersist() { super.prePersist(); }
        public void preUpdate() { super.preUpdate(); }
    }
}
