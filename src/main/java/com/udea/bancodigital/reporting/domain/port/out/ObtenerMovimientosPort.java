package com.udea.bancodigital.reporting.domain.port.out;

import com.udea.bancodigital.reporting.domain.model.MovimientoReporte;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ObtenerMovimientosPort {
    List<MovimientoReporte> obtenerPorClienteYFechas(UUID clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}
