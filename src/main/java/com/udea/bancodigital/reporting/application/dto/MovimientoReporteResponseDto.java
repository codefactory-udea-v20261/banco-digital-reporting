package com.udea.bancodigital.reporting.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoReporteResponseDto {
    private UUID movimientoId;
    private UUID cuentaId;
    private String tipoMovimiento;
    private BigDecimal monto;
    private String descripcion;
    private Instant fecha;
}
