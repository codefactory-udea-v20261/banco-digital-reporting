package com.udea.bancodigital.reporting.application.usecase;

import com.udea.bancodigital.reporting.domain.port.in.GenerarReporteActividadPort;
import com.udea.bancodigital.reporting.domain.port.out.ObtenerMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.out.ObtenerSaldoCuentasPort;
import com.udea.bancodigital.reporting.domain.model.MovimientoReporte;
import com.udea.bancodigital.reporting.domain.model.ReporteActividad;
import com.udea.bancodigital.reporting.domain.model.TotalMovimientos;
import com.udea.bancodigital.reporting.domain.exception.RangoFechasInvalidoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class GenerarReporteActividadUseCase implements GenerarReporteActividadPort {

    private final ObtenerMovimientosPort obtenerMovimientosPort;
    private final ObtenerSaldoCuentasPort obtenerSaldoCuentasPort;

    public GenerarReporteActividadUseCase(
            ObtenerMovimientosPort obtenerMovimientosPort,
            ObtenerSaldoCuentasPort obtenerSaldoCuentasPort
    ) {
        this.obtenerMovimientosPort = obtenerMovimientosPort;
        this.obtenerSaldoCuentasPort = obtenerSaldoCuentasPort;
    }

    @Override
    @Transactional(readOnly = true, timeout = 30)
    public ReporteActividad generar(UUID clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new RangoFechasInvalidoException("El rango de fechas no es válido. La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        if (ChronoUnit.MONTHS.between(fechaInicio, fechaFin) >= 6) {
            throw new RangoFechasInvalidoException("El rango de fechas no es válido. El reporte no puede exceder los 6 meses para garantizar el rendimiento del sistema.");
        }

        List<MovimientoReporte> movimientos = obtenerMovimientosPort.obtenerPorClienteYFechas(clienteId, fechaInicio, fechaFin);
        BigDecimal saldoFinal = obtenerSaldoCuentasPort.obtenerSaldoConsolidadoPorCliente(clienteId);

        if (movimientos.isEmpty()) {
            return new ReporteActividad(
                    fechaInicio,
                    fechaFin,
                    Collections.emptyList(),
                    TotalMovimientos.empty(),
                    saldoFinal,
                    "No hay movimientos en ese período"
            );
        }

        TotalMovimientos totales = calcularTotales(movimientos);

        return new ReporteActividad(
                fechaInicio,
                fechaFin,
                movimientos,
                totales,
                saldoFinal,
                "Reporte generado exitosamente"
        );
    }

    private TotalMovimientos calcularTotales(List<MovimientoReporte> movimientos) {
        BigDecimal depositos = BigDecimal.ZERO;
        BigDecimal retiros = BigDecimal.ZERO;
        BigDecimal transferencias = BigDecimal.ZERO;
        BigDecimal pagos = BigDecimal.ZERO;

        for (MovimientoReporte mov : movimientos) {
            switch (mov.tipo()) {
                case "DEPOSITO" -> depositos = depositos.add(mov.monto());
                case "RETIRO" -> retiros = retiros.add(mov.monto());
                case "TRANSFERENCIA_DEBITO", "TRANSFERENCIA_CREDITO" -> transferencias = transferencias.add(mov.monto());
                case "PAGO", "PAGO_SERVICIO", "PAGO_NOMINA" -> pagos = pagos.add(mov.monto());
                default -> { /* tipo de movimiento no contabilizado */ }
            }
        }

        return new TotalMovimientos(depositos, retiros, transferencias, pagos);
    }
}
