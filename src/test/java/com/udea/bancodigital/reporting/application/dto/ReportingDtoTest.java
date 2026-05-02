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
        CuentaReporteResponseDto dto = CuentaReporteResponseDto.builder()
                .numeroCuenta("123456")
                .tipoCuenta("AHORROS")
                .saldoActual(new BigDecimal("1000.00"))
                .build();

        assertEquals("123456", dto.getNumeroCuenta());
        assertEquals("AHORROS", dto.getTipoCuenta());
        assertEquals(new BigDecimal("1000.00"), dto.getSaldoActual());
        assertNotNull(dto.toString());
    }

    @Test
    void testMovimientoReporteResponseDto() {
        Instant now = Instant.now();
        MovimientoReporteResponseDto dto = MovimientoReporteResponseDto.builder()
                .tipoMovimiento("DEBITO")
                .monto(new BigDecimal("50.00"))
                .fecha(now)
                .descripcion("Test")
                .build();

        assertEquals("DEBITO", dto.getTipoMovimiento());
        assertEquals(new BigDecimal("50.00"), dto.getMonto());
        assertEquals(now, dto.getFecha());
        assertEquals("Test", dto.getDescripcion());
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
    }

    @Test
    void testSaldoTotalClienteResponseDto() {
        SaldoTotalClienteResponseDto dto = SaldoTotalClienteResponseDto.builder()
                .saldoTotal(new BigDecimal("5000.00"))
                .build();

        assertEquals(new BigDecimal("5000.00"), dto.getSaldoTotal());
    }
}
