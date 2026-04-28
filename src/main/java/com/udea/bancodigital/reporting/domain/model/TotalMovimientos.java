package com.udea.bancodigital.reporting.domain.model;

import java.math.BigDecimal;

public record TotalMovimientos(
    BigDecimal depositos,
    BigDecimal retiros,
    BigDecimal transferencias,
    BigDecimal pagos
) {
    public static TotalMovimientos empty() {
        return new TotalMovimientos(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
