package com.udea.bancodigital.reporting.application.dto;

import org.junit.jupiter.api.Test;
import com.udea.bancodigital.shared.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportingDtoTest {

    @Test
    void testCuentaReporteResponseDto() {
        UUID id = UUID.randomUUID();
        CuentaReporteResponseDto dto1 = CuentaReporteResponseDto.builder()
                .cuentaId(id)
                .numeroCuenta("123456")
                .saldoActual(new BigDecimal("1000.00"))
                .build();
        CuentaReporteResponseDto dto2 = CuentaReporteResponseDto.builder()
                .cuentaId(id)
                .numeroCuenta("123456")
                .saldoActual(new BigDecimal("1000.00"))
                .build();
        CuentaReporteResponseDto dto3 = CuentaReporteResponseDto.builder()
                .cuentaId(id)
                .numeroCuenta("654321")
                .saldoActual(new BigDecimal("2000.00"))
                .build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1, null);
        assertNotEquals(dto1, new Object());
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
    }

    @Test
    void testMovimientoReporteResponseDto() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        MovimientoReporteResponseDto dto1 = MovimientoReporteResponseDto.builder()
                .movimientoId(id)
                .tipoMovimiento("DEBITO")
                .build();
        MovimientoReporteResponseDto dto2 = MovimientoReporteResponseDto.builder()
                .movimientoId(id)
                .tipoMovimiento("DEBITO")
                .build();
        MovimientoReporteResponseDto dto3 = MovimientoReporteResponseDto.builder()
                .movimientoId(UUID.randomUUID())
                .tipoMovimiento("CREDITO")
                .build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1, null);
        assertNotNull(dto1.toString());
    }

    @Test
    void testResumenMovimientosResponseDto() {
        ResumenMovimientosResponseDto dto1 = ResumenMovimientosResponseDto.builder().cantidadMovimientos(1).build();
        ResumenMovimientosResponseDto dto2 = ResumenMovimientosResponseDto.builder().cantidadMovimientos(1).build();
        ResumenMovimientosResponseDto dto3 = ResumenMovimientosResponseDto.builder().cantidadMovimientos(2).build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1, null);
    }

    @Test
    void testSaldoTotalClienteResponseDto() {
        SaldoTotalClienteResponseDto dto = SaldoTotalClienteResponseDto.builder()
                .saldoTotal(new BigDecimal("5000.00"))
                .build();

        assertEquals(new BigDecimal("5000.00"), dto.getSaldoTotal());
    }

    @Test
    void testDomainEvent() {
        DomainEvent event1 = DomainEvent.builder().eventId("1").build();
        DomainEvent event2 = DomainEvent.builder().eventId("1").build();
        DomainEvent event3 = DomainEvent.builder().eventId("2").build();

        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
        assertNotEquals(event1, null);
        assertNotNull(event1.toString());
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
