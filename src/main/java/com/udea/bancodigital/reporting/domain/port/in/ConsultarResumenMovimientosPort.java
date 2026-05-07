package com.udea.bancodigital.reporting.domain.port.in;

import com.udea.bancodigital.reporting.application.dto.ResumenMovimientosResponseDto;

import java.time.LocalDate;
import java.util.UUID;

public interface ConsultarResumenMovimientosPort {
    ResumenMovimientosResponseDto consultar(
            UUID clienteId,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    );
}
