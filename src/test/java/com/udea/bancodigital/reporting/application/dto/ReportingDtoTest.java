package com.udea.bancodigital.reporting.application.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportingDtoTest {

    @Test
    void testCuentaReporteResponseDto() {
        UUID id = UUID.randomUUID();
        CuentaReporteResponseDto dto = CuentaReporteResponseDto.builder()
                .cuentaId(id)
                .numeroCuenta("123456")
                .tipoCuenta("AHORROS")
                .estado("ACTIVA")
                .saldoActual(new BigDecimal("1000.00"))
                .build();

        assertEquals(id, dto.getCuentaId());
        assertEquals("123456", dto.getNumeroCuenta());
        assertEquals("AHORROS", dto.getTipoCuenta());
        assertEquals("ACTIVA", dto.getEstado());
        assertEquals(new BigDecimal("1000.00"), dto.getSaldoActual());
        assertNotNull(dto.toString());
        assertEquals(dto, dto);
        assertNotNull(dto.hashCode());
    }

    @Test
    void testMovimientoReporteResponseDto() {
        UUID id = UUID.randomUUID();
        UUID cId = UUID.randomUUID();
        Instant now = Instant.now();
        MovimientoReporteResponseDto dto = MovimientoReporteResponseDto.builder()
                .movimientoId(id)
                .cuentaId(cId)
                .tipoMovimiento("DEBITO")
                .monto(new BigDecimal("50.00"))
                .fecha(now)
                .descripcion("Test")
                .build();

        assertEquals(id, dto.getMovimientoId());
        assertEquals(cId, dto.getCuentaId());
        assertEquals("DEBITO", dto.getTipoMovimiento());
        assertEquals(new BigDecimal("50.00"), dto.getMonto());
        assertEquals(now, dto.getFecha());
        assertEquals("Test", dto.getDescripcion());
        assertNotNull(dto.toString());
        assertEquals(dto, dto);
    }

    @Test
    void testResumenMovimientosResponseDto() {
        ResumenMovimientosResponseDto dto = ResumenMovimientosResponseDto.builder()
                .totalIngresos(new BigDecimal("2000.00"))
                .totalEgresos(new BigDecimal("500.00"))
                .cantidadMovimientos(10)
                .build();

        assertEquals(new BigDecimal("2000.00"), dto.getTotalIngresos());
        assertEquals(new BigDecimal("500.00"), dto.getTotalEgresos());
        assertEquals(10, dto.getCantidadMovimientos());
        assertNotNull(dto.toString());
        assertEquals(dto, dto);
    }

    @Test
    void testSaldoTotalClienteResponseDto() {
        SaldoTotalClienteResponseDto dto = SaldoTotalClienteResponseDto.builder()
                .saldoTotal(new BigDecimal("5000.00"))
                .build();

        assertEquals(new BigDecimal("5000.00"), dto.getSaldoTotal());
    }
}
