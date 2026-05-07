package com.udea.bancodigital.reporting.application.usecase;

import com.udea.bancodigital.reporting.application.dto.MovimientoReporteResponseDto;
import com.udea.bancodigital.reporting.domain.exception.RangoFechasInvalidoException;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.out.BuscarMovimientosPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultarMovimientosUseCase implements ConsultarMovimientosPort {

    private static final long MAX_RANGE_MONTHS = 12;
    private static final int DEFAULT_LOOKBACK_DAYS = 30;

    private final BuscarMovimientosPort buscarMovimientosPort;

    @Override
    @Transactional(readOnly = true, timeout = 30)
    public List<MovimientoReporteResponseDto> consultar(
            UUID clienteId,
            UUID cuentaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String tipo
    ) {
        LocalDate hasta = fechaHasta != null ? fechaHasta : LocalDate.now();
        LocalDate desde = fechaDesde != null ? fechaDesde : hasta.minusDays(DEFAULT_LOOKBACK_DAYS);

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

        return buscarMovimientosPort.buscar(clienteId, cuentaId, desde, hasta, tipo);
    }
}
