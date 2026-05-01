package com.udea.bancodigital.reporting.domain.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface ObtenerSaldoCuentasPort {
    BigDecimal obtenerSaldoConsolidadoPorCliente(UUID clienteId);
}
