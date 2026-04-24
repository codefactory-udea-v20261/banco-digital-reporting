package com.udea.bancodigital.reporting.domain.port.in;

import com.udea.bancodigital.reporting.application.dto.SaldoTotalClienteResponseDto;

import java.util.UUID;

public interface ConsultarSaldoTotalClientePort {
    SaldoTotalClienteResponseDto consultarSaldoTotal(UUID clienteId);
}
