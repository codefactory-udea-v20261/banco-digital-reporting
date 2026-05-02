package com.udea.bancodigital.shared.util;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class SharedUtilTest {

    @Test
    void testAuditableEntityPrePersist_Default() {
        TestAuditableEntity entity = new TestAuditableEntity();
        
        entity.prePersist();

        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertEquals("SYSTEM", entity.getCreatedBy());
        assertEquals("SYSTEM", entity.getUpdatedBy());
    }

    @Test
    void testAuditableEntityPrePersist_FieldsAlreadySet() {
        TestAuditableEntity entity = new TestAuditableEntity();
        Instant manualCreatedAt = Instant.now().minusSeconds(3600);
        entity.setCreatedAt(manualCreatedAt);
        entity.setCreatedBy("USER1");
        entity.setUpdatedBy("USER2");
        
        entity.prePersist();

        assertEquals(manualCreatedAt, entity.getCreatedAt());
        assertEquals("USER1", entity.getCreatedBy());
        assertEquals("USER2", entity.getUpdatedBy());
    }

    @Test
    void testAuditableEntityPrePersist_BlankValues() {
        TestAuditableEntity entity = new TestAuditableEntity();
        entity.setCreatedBy("  ");
        entity.setUpdatedBy("");
        
        entity.prePersist();

        assertEquals("SYSTEM", entity.getCreatedBy());
        assertEquals("SYSTEM", entity.getUpdatedBy());
    }

    @Test
    void testAuditableEntityPreUpdate_Default() {
        TestAuditableEntity entity = new TestAuditableEntity();
        entity.setUpdatedBy(null);
        
        entity.preUpdate();

        assertNotNull(entity.getUpdatedAt());
        assertEquals("SYSTEM", entity.getUpdatedBy());
    }

    @Test
    void testAuditableEntityPreUpdate_WithUser() {
        TestAuditableEntity entity = new TestAuditableEntity();
        entity.setUpdatedBy("ADMIN");
        
        entity.preUpdate();

        assertEquals("ADMIN", entity.getUpdatedBy());
    }

    @Test
    void testAuditableEntityPreUpdate_Blank() {
        TestAuditableEntity entity = new TestAuditableEntity();
        entity.setUpdatedBy(" ");
        
        entity.preUpdate();

        assertEquals("SYSTEM", entity.getUpdatedBy());
    }

    private static class TestAuditableEntity extends AuditableEntity {
        @Override public void prePersist() { super.prePersist(); }
        @Override public void preUpdate() { super.preUpdate(); }
    }
}
