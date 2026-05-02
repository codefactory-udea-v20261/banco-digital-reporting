package com.udea.bancodigital.reporting.application.dto;

import org.junit.jupiter.api.Test;
import com.udea.bancodigital.shared.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReportingDtoTest {

    @Test
    void testCuentaReporteResponseDto() {
        UUID id = UUID.randomUUID();
        CuentaReporteResponseDto dto1 = CuentaReporteResponseDto.builder()
                .cuentaId(id)
                .numeroCuenta("123456")
                .tipoCuenta("AHORROS")
                .estado("ACTIVA")
                .saldoActual(new BigDecimal("1000.00"))
                .build();

        CuentaReporteResponseDto dto2 = new CuentaReporteResponseDto(id, "123456", "AHORROS", "ACTIVA", new BigDecimal("1000.00"));
        CuentaReporteResponseDto dto3 = new CuentaReporteResponseDto();
        dto3.setCuentaId(id);
        dto3.setNumeroCuenta("123456");
        dto3.setTipoCuenta("AHORROS");
        dto3.setEstado("ACTIVA");
        dto3.setSaldoActual(new BigDecimal("1000.00"));

        assertEquals(dto1, dto2);
        assertEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertEquals(dto1.toString(), dto2.toString());
        assertEquals(id, dto1.getCuentaId());
        assertEquals("123456", dto1.getNumeroCuenta());
        assertEquals("AHORROS", dto1.getTipoCuenta());
        assertEquals("ACTIVA", dto1.getEstado());
        assertEquals(new BigDecimal("1000.00"), dto1.getSaldoActual());

        assertNotEquals(dto1, null);
        assertNotEquals(dto1, new Object());
        
        assertNotEquals(dto1, CuentaReporteResponseDto.builder().cuentaId(UUID.randomUUID()).numeroCuenta("123456").tipoCuenta("AHORROS").estado("ACTIVA").saldoActual(new BigDecimal("1000.00")).build());
        assertNotEquals(dto1, CuentaReporteResponseDto.builder().cuentaId(id).numeroCuenta("OTHER").tipoCuenta("AHORROS").estado("ACTIVA").saldoActual(new BigDecimal("1000.00")).build());
    }

    @Test
    void testMovimientoReporteResponseDtoExhaustive() {
        UUID id = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();
        Instant now = Instant.now();
        MovimientoReporteResponseDto dto = MovimientoReporteResponseDto.builder()
                .movimientoId(id)
                .cuentaId(cuentaId)
                .tipoMovimiento("DEBITO")
                .monto(new BigDecimal("100.00"))
                .fecha(now)
                .descripcion("Test")
                .build();

        assertEquals(dto, dto); // NOSONAR
        assertNotEquals(null, dto);
        assertNotEquals("string", dto);
        
        // Test each field for inequality
        assertNotEquals(MovimientoReporteResponseDto.builder().movimientoId(UUID.randomUUID()).cuentaId(cuentaId).tipoMovimiento("DEBITO").monto(new BigDecimal("100.00")).fecha(now).descripcion("Test").build(), dto);
        assertNotEquals(MovimientoReporteResponseDto.builder().movimientoId(id).cuentaId(UUID.randomUUID()).tipoMovimiento("DEBITO").monto(new BigDecimal("100.00")).fecha(now).descripcion("Test").build(), dto);
        assertNotEquals(MovimientoReporteResponseDto.builder().movimientoId(id).cuentaId(cuentaId).tipoMovimiento("CREDIT").monto(new BigDecimal("100.00")).fecha(now).descripcion("Test").build(), dto);
        assertNotEquals(MovimientoReporteResponseDto.builder().movimientoId(id).cuentaId(cuentaId).tipoMovimiento("DEBITO").monto(new BigDecimal("200.00")).fecha(now).descripcion("Test").build(), dto);
        assertNotEquals(MovimientoReporteResponseDto.builder().movimientoId(id).cuentaId(cuentaId).tipoMovimiento("DEBITO").monto(new BigDecimal("100.00")).fecha(now.plusSeconds(1)).descripcion("Test").build(), dto);
        assertNotEquals(MovimientoReporteResponseDto.builder().movimientoId(id).cuentaId(cuentaId).tipoMovimiento("DEBITO").monto(new BigDecimal("100.00")).fecha(now).descripcion("Other").build(), dto);
    }

    @Test
    void testResumenMovimientosResponseDtoExhaustive() {
        ResumenMovimientosResponseDto dto = ResumenMovimientosResponseDto.builder()
                .cantidadMovimientos(5)
                .totalIngresos(new BigDecimal("500.00"))
                .totalEgresos(new BigDecimal("200.00"))
                .build();

        assertEquals(dto, dto); // NOSONAR
        assertNotEquals(ResumenMovimientosResponseDto.builder().cantidadMovimientos(6).totalIngresos(new BigDecimal("500.00")).totalEgresos(new BigDecimal("200.00")).build(), dto);
        assertNotEquals(ResumenMovimientosResponseDto.builder().cantidadMovimientos(5).totalIngresos(new BigDecimal("501.00")).totalEgresos(new BigDecimal("200.00")).build(), dto);
        assertNotEquals(ResumenMovimientosResponseDto.builder().cantidadMovimientos(5).totalIngresos(new BigDecimal("500.00")).totalEgresos(new BigDecimal("201.00")).build(), dto);
    }

    @Test
    void testSaldoTotalClienteResponseDtoExhaustive() {
        UUID clienteId = UUID.randomUUID();
        SaldoTotalClienteResponseDto dto = SaldoTotalClienteResponseDto.builder()
                .clienteId(clienteId)
                .saldoTotal(new BigDecimal("5000.00"))
                .build();

        assertEquals(dto, dto); // NOSONAR
        assertNotEquals(dto, SaldoTotalClienteResponseDto.builder().clienteId(UUID.randomUUID()).saldoTotal(new BigDecimal("5000.00")).build());
        assertNotEquals(dto, SaldoTotalClienteResponseDto.builder().clienteId(clienteId).saldoTotal(new BigDecimal("5001.00")).build());
    }

    @Test
    void testDomainEvent() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        DomainEvent event1 = DomainEvent.builder()
                .eventId("1")
                .eventType("TestEvent")
                .aggregateId("Agg1")
                .occurredAt(now)
                .build();
        
        DomainEvent event2 = DomainEvent.builder()
                .eventId("1")
                .eventType("TestEvent")
                .aggregateId("Agg1")
                .occurredAt(now)
                .build();

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotNull(event1.toString());
        assertEquals("1", event1.getEventId());
        assertEquals("TestEvent", event1.getEventType());
    }

    @Test
    void testCuentaReporteResponseDtoInequality() {
        UUID id = UUID.randomUUID();
        CuentaReporteResponseDto dto = CuentaReporteResponseDto.builder()
                .cuentaId(id).numeroCuenta("1").tipoCuenta("A").estado("S").saldoActual(BigDecimal.ONE).build();
        
        assertNotEquals(dto, CuentaReporteResponseDto.builder().cuentaId(UUID.randomUUID()).numeroCuenta("1").tipoCuenta("A").estado("S").saldoActual(BigDecimal.ONE).build());
        assertNotEquals(dto, CuentaReporteResponseDto.builder().cuentaId(id).numeroCuenta("2").tipoCuenta("A").estado("S").saldoActual(BigDecimal.ONE).build());
        assertNotEquals(dto, CuentaReporteResponseDto.builder().cuentaId(id).numeroCuenta("1").tipoCuenta("B").estado("S").saldoActual(BigDecimal.ONE).build());
        assertNotEquals(dto, CuentaReporteResponseDto.builder().cuentaId(id).numeroCuenta("1").tipoCuenta("A").estado("X").saldoActual(BigDecimal.ONE).build());
        assertNotEquals(dto, CuentaReporteResponseDto.builder().cuentaId(id).numeroCuenta("1").tipoCuenta("A").estado("S").saldoActual(BigDecimal.ZERO).build());
    }

    @Test
    void testNoArgsAndHashCode() {
        assertNotNull(new CuentaReporteResponseDto());
        assertNotNull(new MovimientoReporteResponseDto());
        assertNotNull(new ResumenMovimientosResponseDto());
        assertNotNull(new SaldoTotalClienteResponseDto());
        
        CuentaReporteResponseDto dto1 = new CuentaReporteResponseDto();
        CuentaReporteResponseDto dto2 = new CuentaReporteResponseDto();
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        
        dto1.setNumeroCuenta("123");
        assertNotEquals(dto1, dto2);
        dto2.setNumeroCuenta("123");
        assertEquals(dto1, dto2);
        
        ResumenMovimientosResponseDto r1 = new ResumenMovimientosResponseDto();
        ResumenMovimientosResponseDto r2 = new ResumenMovimientosResponseDto();
        assertEquals(r1, r2);
        r1.setCantidadMovimientos(1);
        assertNotEquals(r1, r2);
        
        SaldoTotalClienteResponseDto s1 = new SaldoTotalClienteResponseDto();
        SaldoTotalClienteResponseDto s2 = new SaldoTotalClienteResponseDto();
        assertEquals(s1, s2);
        s1.setSaldoTotal(BigDecimal.TEN);
        assertNotEquals(s1, s2);
    }

    @Test
    void testDomainEventNoArgs() {
        assertNotNull(new DomainEvent());
        DomainEvent e1 = new DomainEvent();
        DomainEvent e2 = new DomainEvent();
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotNull(e1.toString());
        
        e1.setEventId("id1");
        assertNotEquals(e1, e2);
        e2.setEventId("id1");
        assertEquals(e1, e2);
        
        e1.setEventType("T1");
        assertNotEquals(e1, e2);
    }
}
