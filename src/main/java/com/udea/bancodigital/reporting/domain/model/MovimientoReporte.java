package com.udea.bancodigital.reporting.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MovimientoReporte(
    String id,
    String cuentaOrigenId,
    String cuentaDestinoId,
    String tipo,
    BigDecimal monto,
    BigDecimal saldoPosterior,
    String descripcion,
    String referencia,
    String estado,
    OffsetDateTime createdAt
) {}
