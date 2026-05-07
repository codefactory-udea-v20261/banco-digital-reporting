package com.udea.bancodigital.reporting.application.usecase;

import com.udea.bancodigital.reporting.application.dto.MovimientoReporteResponseDto;
import com.udea.bancodigital.reporting.domain.exception.RangoFechasInvalidoException;
import com.udea.bancodigital.reporting.domain.port.out.BuscarMovimientosPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarMovimientosUseCaseTest {

    @Mock
    private BuscarMovimientosPort buscarMovimientosPort;

    @InjectMocks
    private ConsultarMovimientosUseCase useCase;

    private final UUID clienteId = UUID.randomUUID();

    @Test
    void consultar_givenAllParameters_delegatesToPortAndReturnsResults() {
        UUID cuentaId = UUID.randomUUID();
        LocalDate desde = LocalDate.of(2026, 4, 1);
        LocalDate hasta = LocalDate.of(2026, 4, 30);
        List<MovimientoReporteResponseDto> expected = List.of(MovimientoReporteResponseDto.builder().build());
        when(buscarMovimientosPort.buscar(clienteId, cuentaId, desde, hasta, "DEPOSITO"))
                .thenReturn(expected);

        List<MovimientoReporteResponseDto> result =
                useCase.consultar(clienteId, cuentaId, desde, hasta, "DEPOSITO");

        assertThat(result).isSameAs(expected);
    }

    @Test
    void consultar_givenNullDates_appliesDefaultLast30Days() {
        when(buscarMovimientosPort.buscar(any(), any(), any(), any(), any())).thenReturn(List.of());

        useCase.consultar(clienteId, null, null, null, null);

        verify(buscarMovimientosPort).buscar(eq(clienteId), eq(null),
                argThat((LocalDate d) -> !d.isAfter(LocalDate.now())),
                argThat((LocalDate d) -> !d.isBefore(LocalDate.now().minusDays(1))),
                eq(null));
    }

    @Test
    void consultar_givenInvertedDateRange_throwsRangoInvalido() {
        LocalDate desde = LocalDate.of(2026, 5, 10);
        LocalDate hasta = LocalDate.of(2026, 5, 1);

        assertThatThrownBy(() -> useCase.consultar(clienteId, null, desde, hasta, null))
                .isInstanceOf(RangoFechasInvalidoException.class);

        verify(buscarMovimientosPort, never()).buscar(any(), any(), any(), any(), any());
    }

    @Test
    void consultar_givenRangeOf12MonthsOrMore_throwsRangoInvalido() {
        LocalDate hasta = LocalDate.of(2026, 5, 1);
        LocalDate desde = hasta.minusMonths(13);

        assertThatThrownBy(() -> useCase.consultar(clienteId, null, desde, hasta, null))
                .isInstanceOf(RangoFechasInvalidoException.class);

        verify(buscarMovimientosPort, never()).buscar(any(), any(), any(), any(), any());
    }

}
