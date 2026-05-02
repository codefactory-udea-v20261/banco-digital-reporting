package com.udea.bancodigital.reporting.infrastructure.adapter.out.persistence;

import com.udea.bancodigital.reporting.domain.model.MovimientoReporte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteJdbcAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReporteJdbcAdapter adapter;

    private UUID clienteId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
    }

    @Test
    void obtenerPorClienteYFechas_Success() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        
        MovimientoReporte mov = new MovimientoReporte(
            "1", "C1", "C2", "TRANSFERENCIA", new BigDecimal("100"),
            new BigDecimal("900"), "Test", "REF1", "COMPLETADA", OffsetDateTime.now()
        );

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
            .thenReturn(List.of(mov));

        List<MovimientoReporte> result = adapter.obtenerPorClienteYFechas(clienteId, start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).id());
    }

    @Test
    void obtenerSaldoConsolidadoPorCliente_Success() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(BigDecimal.class)))
            .thenReturn(new BigDecimal("5000.00"));

        BigDecimal result = adapter.obtenerSaldoConsolidadoPorCliente(clienteId);

        assertEquals(new BigDecimal("5000.00"), result);
    }

    @Test
    void obtenerSaldoConsolidadoPorCliente_NullReturnsZero() {
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(BigDecimal.class)))
            .thenReturn(null);

        BigDecimal result = adapter.obtenerSaldoConsolidadoPorCliente(clienteId);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testMapRowToMovimientoReporte() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("id")).thenReturn("1");
        when(rs.getString("cuenta_origen_id")).thenReturn("C1");
        when(rs.getString("cuenta_destino_id")).thenReturn("C2");
        when(rs.getString("tipo")).thenReturn("TRANSFERENCIA");
        when(rs.getBigDecimal("monto")).thenReturn(new BigDecimal("100"));
        when(rs.getBigDecimal("saldo_posterior")).thenReturn(new BigDecimal("900"));
        when(rs.getString("descripcion")).thenReturn("Test");
        when(rs.getString("referencia")).thenReturn("REF1");
        when(rs.getString("estado")).thenReturn("COMPLETADA");
        OffsetDateTime now = OffsetDateTime.now();
        when(rs.getObject("created_at", OffsetDateTime.class)).thenReturn(now);

        // We use Reflection to call the private mapper method or just rely on the query test if it uses it.
        // Actually, mapRowToMovimientoReporte is passed as a method reference.
        // I can test it by calling it directly if I make it accessible or just trust the RowMapper logic.
        
        // Let's use the adapter's own method via reflection to be sure.
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(adapter, "mapRowToMovimientoReporte", rs, 1);
        
        // The method reference in query() will call this.
    }
}
