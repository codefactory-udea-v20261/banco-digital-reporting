package com.udea.bancodigital.reporting.domain.port.out;

import com.udea.bancodigital.reporting.application.dto.ResumenMovimientosResponseDto;

import java.time.LocalDate;
import java.util.UUID;

public interface ObtenerResumenMovimientosPort {
    ResumenMovimientosResponseDto obtener(
            UUID clienteId,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    );
}
