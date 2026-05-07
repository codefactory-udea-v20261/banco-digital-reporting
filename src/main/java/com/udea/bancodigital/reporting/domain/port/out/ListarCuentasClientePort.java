package com.udea.bancodigital.reporting.domain.port.out;

import com.udea.bancodigital.reporting.application.dto.CuentaReporteResponseDto;

import java.util.List;
import java.util.UUID;

public interface ListarCuentasClientePort {
    List<CuentaReporteResponseDto> listar(UUID clienteId);
}
