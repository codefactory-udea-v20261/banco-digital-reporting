package com.udea.bancodigital.reporting.domain.port.in;

import com.udea.bancodigital.reporting.application.dto.MovimientoReporteResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ConsultarMovimientosPort {
    List<MovimientoReporteResponseDto> consultar(
            UUID clienteId,
            UUID cuentaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String tipo
    );
}
