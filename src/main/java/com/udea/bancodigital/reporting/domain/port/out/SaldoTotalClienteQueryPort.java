package com.udea.bancodigital.reporting.domain.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface SaldoTotalClienteQueryPort {
    BigDecimal obtenerSaldoTotalCliente(UUID clienteId);
}
