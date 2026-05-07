package com.udea.bancodigital.reporting.application.usecase;

import com.udea.bancodigital.reporting.application.dto.ResumenMovimientosResponseDto;
import com.udea.bancodigital.reporting.domain.exception.RangoFechasInvalidoException;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarResumenMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.out.ObtenerResumenMovimientosPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultarResumenMovimientosUseCase implements ConsultarResumenMovimientosPort {

    private static final long MAX_RANGE_MONTHS = 12;

    private final ObtenerResumenMovimientosPort obtenerResumenMovimientosPort;

    @Override
    @Transactional(readOnly = true, timeout = 30)
    public ResumenMovimientosResponseDto consultar(
            UUID clienteId,
            LocalDate fechaDesde,
            LocalDate fechaHasta
    ) {
        LocalDate hasta = fechaHasta != null ? fechaHasta : LocalDate.now();
        LocalDate desde = fechaDesde != null ? fechaDesde : hasta.withDayOfMonth(1);

        if (desde.isAfter(hasta)) {
            throw new RangoFechasInvalidoException(
                    "El rango de fechas no es válido. La fecha de inicio no puede ser posterior a la fecha de fin."
            );
        }
        if (ChronoUnit.MONTHS.between(desde, hasta) >= MAX_RANGE_MONTHS) {
            throw new RangoFechasInvalidoException(
                    "El rango de fechas no es válido. El reporte no puede exceder los 12 meses."
            );
        }

        return obtenerResumenMovimientosPort.obtener(clienteId, desde, hasta);
    }
}
