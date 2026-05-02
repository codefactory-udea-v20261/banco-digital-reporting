package com.udea.bancodigital.reporting.application.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ExhaustiveDtoTest {

    @Test
    void testCuentaReporteResponseDtoExhaustive() {
        UUID id = UUID.randomUUID();
        CuentaReporteResponseDto base = new CuentaReporteResponseDto(id, "123", "A", "ACT", BigDecimal.TEN);
        CuentaReporteResponseDto same = new CuentaReporteResponseDto(id, "123", "A", "ACT", BigDecimal.TEN);
        
        // Equals/HashCode branches
        assertTrue(base.equals(base)); // NOSONAR: Required for branch coverage
        assertEquals(base, same);
        assertNotEquals(null, base);
        assertNotEquals(new Object(), base);
        assertEquals(base.hashCode(), same.hashCode());
        
        // Branch coverage: one field null in 'this'
        assertNotEquals(new CuentaReporteResponseDto(null, "123", "A", "ACT", BigDecimal.TEN), base);
        assertNotEquals(new CuentaReporteResponseDto(id, null, "A", "ACT", BigDecimal.TEN), base);
        assertNotEquals(new CuentaReporteResponseDto(id, "123", null, "ACT", BigDecimal.TEN), base);
        assertNotEquals(new CuentaReporteResponseDto(id, "123", "A", null, BigDecimal.TEN), base);
        assertNotEquals(new CuentaReporteResponseDto(id, "123", "A", "ACT", null), base);
        
        // Branch coverage: one field null in 'other'
        assertNotEquals(base, new CuentaReporteResponseDto(null, "123", "A", "ACT", BigDecimal.TEN));
        assertNotEquals(base, new CuentaReporteResponseDto(id, null, "A", "ACT", BigDecimal.TEN));
        
        // Branch coverage: both null
        assertEquals(new CuentaReporteResponseDto(null, null, null, null, null), new CuentaReporteResponseDto(null, null, null, null, null));
        
        // Different values
        assertNotEquals(base, new CuentaReporteResponseDto(UUID.randomUUID(), "123", "A", "ACT", BigDecimal.TEN));
        assertNotEquals(base, new CuentaReporteResponseDto(id, "456", "A", "ACT", BigDecimal.TEN));
        
        assertNotNull(base.toString());
    }

    @Test
    void testMovimientoReporteResponseDtoExhaustive() {
        UUID mid = UUID.randomUUID();
        UUID cid = UUID.randomUUID();
        Instant now = Instant.now();
        MovimientoReporteResponseDto base = new MovimientoReporteResponseDto(mid, cid, "T", BigDecimal.TEN, "D", now);
        
        assertEquals(base, base);
        assertEquals(base, new MovimientoReporteResponseDto(mid, cid, "T", BigDecimal.TEN, "D", now));
        assertNotEquals(base, null);
        
        // Null branches
        assertNotEquals(new MovimientoReporteResponseDto(null, cid, "T", BigDecimal.TEN, "D", now), base);
        assertNotEquals(base, new MovimientoReporteResponseDto(null, cid, "T", BigDecimal.TEN, "D", now));
        
        assertNotEquals(new MovimientoReporteResponseDto(mid, null, "T", BigDecimal.TEN, "D", now), base);
        assertNotEquals(new MovimientoReporteResponseDto(mid, cid, null, BigDecimal.TEN, "D", now), base);
        assertNotEquals(new MovimientoReporteResponseDto(mid, cid, "T", null, "D", now), base);
        assertNotEquals(new MovimientoReporteResponseDto(mid, cid, "T", BigDecimal.TEN, null, now), base);
        assertNotEquals(new MovimientoReporteResponseDto(mid, cid, "T", BigDecimal.TEN, "D", null), base);
        
        assertEquals(new MovimientoReporteResponseDto(), new MovimientoReporteResponseDto());
        assertNotNull(base.toString());
    }

    @Test
    void testResumenMovimientosResponseDtoExhaustive() {
        ResumenMovimientosResponseDto base = new ResumenMovimientosResponseDto(BigDecimal.TEN, BigDecimal.ONE, 5);
        assertTrue(base.equals(base)); // NOSONAR
        assertNotEquals(null, base);
        
        assertNotEquals(base, new ResumenMovimientosResponseDto(null, BigDecimal.ONE, 5));
        assertNotEquals(base, new ResumenMovimientosResponseDto(BigDecimal.TEN, null, 5));
        assertNotEquals(base, new ResumenMovimientosResponseDto(BigDecimal.TEN, BigDecimal.ONE, null));
        
        assertNotEquals(new ResumenMovimientosResponseDto(null, BigDecimal.ONE, 5), base);
        
        assertEquals(new ResumenMovimientosResponseDto(null, null, null), new ResumenMovimientosResponseDto(null, null, null));
        assertNotNull(base.toString());
    }

    @Test
    void testSaldoTotalClienteResponseDtoExhaustive() {
        UUID cid = UUID.randomUUID();
        SaldoTotalClienteResponseDto base = new SaldoTotalClienteResponseDto(cid, BigDecimal.TEN);
        assertTrue(base.equals(base)); // NOSONAR
        
        assertNotEquals(base, new SaldoTotalClienteResponseDto(null, BigDecimal.TEN));
        assertNotEquals(base, new SaldoTotalClienteResponseDto(cid, null));
        
        assertNotEquals(null, base);
        
        assertEquals(new SaldoTotalClienteResponseDto(null, null), new SaldoTotalClienteResponseDto(null, null));
        assertNotNull(base.toString());
    }
}
