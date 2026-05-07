package com.udea.bancodigital.reporting.application.usecase;

import com.udea.bancodigital.reporting.application.dto.ResumenMovimientosResponseDto;
import com.udea.bancodigital.reporting.domain.exception.RangoFechasInvalidoException;
import com.udea.bancodigital.reporting.domain.port.out.ObtenerResumenMovimientosPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class ConsultarResumenMovimientosUseCaseTest {

    @Mock
    private ObtenerResumenMovimientosPort obtenerResumenMovimientosPort;

    @InjectMocks
    private ConsultarResumenMovimientosUseCase useCase;

    private final UUID clienteId = UUID.randomUUID();

    @Test
    void consultar_givenValidRange_delegatesAndReturnsTotals() {
        ResumenMovimientosResponseDto expected = ResumenMovimientosResponseDto.builder()
                .totalIngresos(new BigDecimal("100"))
                .totalEgresos(new BigDecimal("40"))
                .cantidadMovimientos(3)
                .build();
        LocalDate desde = LocalDate.of(2026, 4, 1);
        LocalDate hasta = LocalDate.of(2026, 4, 30);
        when(obtenerResumenMovimientosPort.obtener(clienteId, desde, hasta)).thenReturn(expected);

        ResumenMovimientosResponseDto result = useCase.consultar(clienteId, desde, hasta);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void consultar_givenNullDates_defaultsToCurrentMonth() {
        when(obtenerResumenMovimientosPort.obtener(any(), any(), any()))
                .thenReturn(ResumenMovimientosResponseDto.builder().build());

        useCase.consultar(clienteId, null, null);

        verify(obtenerResumenMovimientosPort).obtener(
                eq(clienteId),
                argThat((LocalDate d) -> d.getDayOfMonth() == 1),
                argThat((LocalDate d) -> !d.isAfter(LocalDate.now()))
        );
    }

    @Test
    void consultar_givenInvertedRange_throwsRangoInvalido() {
        LocalDate desde = LocalDate.of(2026, 5, 10);
        LocalDate hasta = LocalDate.of(2026, 5, 1);

        assertThatThrownBy(() -> useCase.consultar(clienteId, desde, hasta))
                .isInstanceOf(RangoFechasInvalidoException.class);

        verify(obtenerResumenMovimientosPort, never()).obtener(any(), any(), any());
    }

    @Test
    void consultar_givenRangeOf12MonthsOrMore_throwsRangoInvalido() {
        LocalDate hasta = LocalDate.of(2026, 5, 1);
        LocalDate desde = hasta.minusMonths(13);

        assertThatThrownBy(() -> useCase.consultar(clienteId, desde, hasta))
                .isInstanceOf(RangoFechasInvalidoException.class);
    }

}
