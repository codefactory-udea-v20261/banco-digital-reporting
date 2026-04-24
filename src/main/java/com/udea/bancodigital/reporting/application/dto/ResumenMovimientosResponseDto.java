package com.udea.bancodigital.reporting.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumenMovimientosResponseDto {
    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private Integer cantidadMovimientos;
}
