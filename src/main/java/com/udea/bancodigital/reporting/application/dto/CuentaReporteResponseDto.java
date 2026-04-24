package com.udea.bancodigital.reporting.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CuentaReporteResponseDto {
    private UUID cuentaId;
    private String numeroCuenta;
    private String tipoCuenta;
    private String estado;
    private BigDecimal saldoActual;
}
