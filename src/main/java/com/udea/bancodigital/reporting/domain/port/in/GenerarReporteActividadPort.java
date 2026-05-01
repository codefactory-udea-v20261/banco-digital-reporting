package com.udea.bancodigital.reporting.domain.port.in;

import com.udea.bancodigital.reporting.domain.model.ReporteActividad;
import java.time.LocalDate;
import java.util.UUID;

public interface GenerarReporteActividadPort {
    ReporteActividad generar(UUID clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}
