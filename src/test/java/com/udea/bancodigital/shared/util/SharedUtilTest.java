package com.udea.bancodigital.shared.util;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class SharedUtilTest {

    @Test
    void testAuditableEntity() {
        TestAuditableEntity entity = new TestAuditableEntity();
        Instant now = Instant.now();
        
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy("admin");
        entity.setUpdatedBy("admin");

        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
        assertEquals("admin", entity.getCreatedBy());
        assertEquals("admin", entity.getUpdatedBy());
    }

    private static class TestAuditableEntity extends AuditableEntity {}
}
