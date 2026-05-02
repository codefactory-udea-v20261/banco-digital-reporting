package com.udea.bancodigital.shared.util;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class SharedUtilTest {

    @Test
    void testAuditableEntityPrePersist() {
        TestAuditableEntity entity = new TestAuditableEntity();
        
        entity.prePersist();

        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertEquals("SYSTEM", entity.getCreatedBy());
        assertEquals("SYSTEM", entity.getUpdatedBy());
    }

    @Test
    void testAuditableEntityPreUpdate() {
        TestAuditableEntity entity = new TestAuditableEntity();
        entity.setCreatedBy("admin");
        entity.prePersist();
        
        entity.setUpdatedBy(null);
        entity.preUpdate();

        assertNotNull(entity.getUpdatedAt());
        assertEquals("SYSTEM", entity.getUpdatedBy());
    }

    private static class TestAuditableEntity extends AuditableEntity {
        @Override public void prePersist() { super.prePersist(); }
        @Override public void preUpdate() { super.preUpdate(); }
    }
}
