package com.udea.bancodigital.reporting.application.usecase;

import com.udea.bancodigital.reporting.domain.exception.RangoFechasInvalidoException;
import com.udea.bancodigital.reporting.domain.model.MovimientoReporte;
import com.udea.bancodigital.reporting.domain.model.ReporteActividad;
import com.udea.bancodigital.reporting.domain.port.out.ObtenerMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.out.ObtenerSaldoCuentasPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerarReporteActividadUseCaseTest {

    @Mock
    private ObtenerMovimientosPort obtenerMovimientosPort;

    @Mock
    private ObtenerSaldoCuentasPort obtenerSaldoCuentasPort;

    private GenerarReporteActividadUseCase useCase;

    private final UUID clienteId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GenerarReporteActividadUseCase(obtenerMovimientosPort, obtenerSaldoCuentasPort);
    }

    @Test
    void generar_DebeCalcularTotalesCorrectamente_CuandoHayMovimientos() {

        LocalDate inicio = LocalDate.now().minusMonths(1);
        LocalDate fin = LocalDate.now();

        List<MovimientoReporte> movimientos = List.of(
                new MovimientoReporte("1", "cta1", "cta2", "DEPOSITO", new BigDecimal("1000.00"),
                        new BigDecimal("1000.00"), "Dep", "R1", "COMPLETADA", OffsetDateTime.now()),
                new MovimientoReporte("2", "cta1", null, "RETIRO", new BigDecimal("200.00"), new BigDecimal("800.00"),
                        "Ret", "R2", "COMPLETADA", OffsetDateTime.now()),
                new MovimientoReporte("3", "cta1", "cta3", "TRANSFERENCIA_DEBITO", new BigDecimal("300.00"),
                        new BigDecimal("500.00"), "Tra", "R3", "COMPLETADA", OffsetDateTime.now()));

        when(obtenerMovimientosPort.obtenerPorClienteYFechas(eq(clienteId), any(), any()))
                .thenReturn(movimientos);
        when(obtenerSaldoCuentasPort.obtenerSaldoConsolidadoPorCliente(clienteId))
                .thenReturn(new BigDecimal("500.00"));

        ReporteActividad reporte = useCase.generar(clienteId, inicio, fin);

        assertNotNull(reporte);
        assertEquals(0, new BigDecimal("1000.00").compareTo(reporte.totales().depositos()));
        assertEquals(0, new BigDecimal("200.00").compareTo(reporte.totales().retiros()));
        assertEquals(0, new BigDecimal("300.00").compareTo(reporte.totales().transferencias()));
        assertEquals(0, new BigDecimal("500.00").compareTo(reporte.saldoFinal()));
        assertEquals("Reporte generado exitosamente", reporte.mensaje());
    }

    @Test
    void generar_DebeRetornarReporteVacio_CuandoNoHayMovimientos() {

        LocalDate inicio = LocalDate.now().minusMonths(1);
        LocalDate fin = LocalDate.now();

        when(obtenerMovimientosPort.obtenerPorClienteYFechas(eq(clienteId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(obtenerSaldoCuentasPort.obtenerSaldoConsolidadoPorCliente(clienteId))
                .thenReturn(BigDecimal.ZERO);

        ReporteActividad reporte = useCase.generar(clienteId, inicio, fin);

        assertTrue(reporte.movimientos().isEmpty());
        assertEquals(BigDecimal.ZERO, reporte.totales().depositos());
        assertEquals("No hay movimientos en ese período", reporte.mensaje());
    }

    @Test
    void generar_DebeLanzarExcepcion_CuandoFechaInicioEsPosteriorAFin() {

        LocalDate inicio = LocalDate.now();
        LocalDate fin = LocalDate.now().minusDays(1);

        assertThrows(RangoFechasInvalidoException.class, () -> useCase.generar(clienteId, inicio, fin));
    }

    @Test
    void generar_DebeLanzarExcepcion_CuandoRangoExcede6Meses() {
        // Given
        LocalDate inicio = LocalDate.now().minusMonths(7);
        LocalDate fin = LocalDate.now();

        assertThrows(RangoFechasInvalidoException.class, () -> useCase.generar(clienteId, inicio, fin));
    }
}
