package com.udea.bancodigital.reporting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteActividad(
    LocalDate fechaInicio,
    LocalDate fechaFin,
    List<MovimientoReporte> movimientos,
    TotalMovimientos totales,
    BigDecimal saldoFinal,
    String mensaje
) {}
