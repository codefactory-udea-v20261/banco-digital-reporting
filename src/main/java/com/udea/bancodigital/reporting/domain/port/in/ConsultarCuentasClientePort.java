package com.udea.bancodigital.reporting.domain.port.in;

import com.udea.bancodigital.reporting.application.dto.CuentaReporteResponseDto;

import java.util.List;
import java.util.UUID;

public interface ConsultarCuentasClientePort {
    List<CuentaReporteResponseDto> consultar(UUID clienteId);
}
