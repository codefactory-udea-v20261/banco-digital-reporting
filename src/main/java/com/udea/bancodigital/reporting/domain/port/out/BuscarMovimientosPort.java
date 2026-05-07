package com.udea.bancodigital.reporting.domain.port.out;

import com.udea.bancodigital.reporting.application.dto.MovimientoReporteResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BuscarMovimientosPort {
    List<MovimientoReporteResponseDto> buscar(
            UUID clienteId,
            UUID cuentaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String tipo
    );
}
