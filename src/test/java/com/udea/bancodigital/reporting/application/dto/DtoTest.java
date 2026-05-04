package com.udea.bancodigital.reporting.application.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    @Test
    void testMovimientoReporteResponseDto() {
        UUID id = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();
        Instant date = Instant.now();
        MovimientoReporteResponseDto dto = MovimientoReporteResponseDto.builder()
                .movimientoId(id)
                .cuentaId(cuentaId)
                .tipoMovimiento("DEPOSITO")
                .monto(BigDecimal.TEN)
                .descripcion("Test")
                .fecha(date)
                .build();

        assertThat(dto.getMovimientoId()).isEqualTo(id);
        assertThat(dto.getCuentaId()).isEqualTo(cuentaId);
        assertThat(dto.getTipoMovimiento()).isEqualTo("DEPOSITO");
        assertThat(dto.getMonto()).isEqualTo(BigDecimal.TEN);
        assertThat(dto.getFecha()).isEqualTo(date);

        dto.setTipoMovimiento("RETIRO");
        assertThat(dto.getTipoMovimiento()).isEqualTo("RETIRO");

        assertThat(dto.toString()).isNotBlank();
        assertThat(dto.hashCode()).isNotZero();
        assertThat(dto).isEqualTo(dto);
        assertThat(dto).isNotEqualTo(new Object());
    }

    @Test
    void testResumenMovimientosResponseDto() {
        ResumenMovimientosResponseDto dto = ResumenMovimientosResponseDto.builder()
                .totalIngresos(BigDecimal.TEN)
                .totalEgresos(BigDecimal.ONE)
                .cantidadMovimientos(2)
                .build();

        assertThat(dto.getTotalIngresos()).isEqualTo(BigDecimal.TEN);
        assertThat(dto.getTotalEgresos()).isEqualTo(BigDecimal.ONE);
        assertThat(dto.getCantidadMovimientos()).isEqualTo(2);

        dto.setCantidadMovimientos(5);
        assertThat(dto.getCantidadMovimientos()).isEqualTo(5);

        assertThat(dto.toString()).isNotBlank();
        assertThat(dto.hashCode()).isNotZero();
        assertThat(dto).isEqualTo(dto);
        assertThat(dto).isNotEqualTo(new Object());
    }

    @Test
    void testSaldoTotalClienteResponseDto() {
        UUID clienteId = UUID.randomUUID();
        SaldoTotalClienteResponseDto dto = SaldoTotalClienteResponseDto.builder()
                .clienteId(clienteId)
                .saldoTotal(BigDecimal.TEN)
                .build();

        assertThat(dto.getClienteId()).isEqualTo(clienteId);
        assertThat(dto.getSaldoTotal()).isEqualTo(BigDecimal.TEN);

        UUID newClienteId = UUID.randomUUID();
        dto.setClienteId(newClienteId);
        assertThat(dto.getClienteId()).isEqualTo(newClienteId);

        assertThat(dto.toString()).isNotBlank();
        assertThat(dto.hashCode()).isNotZero();
        assertThat(dto).isEqualTo(dto);
        assertThat(dto).isNotEqualTo(new Object());
    }
}
