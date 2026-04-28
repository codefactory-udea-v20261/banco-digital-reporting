package com.udea.bancodigital.reporting.infrastructure.adapter.out.persistence;

import com.udea.bancodigital.reporting.domain.model.MovimientoReporte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteJdbcAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ReporteJdbcAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ReporteJdbcAdapter(jdbcTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void obtenerPorClienteYFechas_DebeRetornarLista() {

        UUID clienteId = UUID.randomUUID();
        LocalDate inicio = LocalDate.now().minusDays(10);
        LocalDate fin = LocalDate.now();
        List<MovimientoReporte> movimientos = List.of(mock(MovimientoReporte.class));

        when(jdbcTemplate.query(any(String.class), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(movimientos);

        List<MovimientoReporte> result = adapter.obtenerPorClienteYFechas(clienteId, inicio, fin);

        assertEquals(1, result.size());
    }

    @Test
    void obtenerSaldoConsolidadoPorCliente_DebeRetornarSaldo() {

        UUID clienteId = UUID.randomUUID();
        BigDecimal saldoEsperado = new BigDecimal("5000.00");

        when(jdbcTemplate.queryForObject(any(String.class), any(SqlParameterSource.class), eq(BigDecimal.class)))
                .thenReturn(saldoEsperado);

        BigDecimal result = adapter.obtenerSaldoConsolidadoPorCliente(clienteId);

        assertEquals(saldoEsperado, result);
    }

    @Test
    void obtenerSaldoConsolidadoPorCliente_DebeRetornarCero_CuandoEsNull() {

        UUID clienteId = UUID.randomUUID();

        when(jdbcTemplate.queryForObject(any(String.class), any(SqlParameterSource.class), eq(BigDecimal.class)))
                .thenReturn(null);

        BigDecimal result = adapter.obtenerSaldoConsolidadoPorCliente(clienteId);

        assertEquals(BigDecimal.ZERO, result);
    }
}
