package com.udea.bancodigital.reporting.infrastructure.adapter.out.persistence;

import com.udea.bancodigital.reporting.domain.port.out.ObtenerMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.out.ObtenerSaldoCuentasPort;
import com.udea.bancodigital.reporting.domain.model.MovimientoReporte;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class ReporteJdbcAdapter implements ObtenerMovimientosPort, ObtenerSaldoCuentasPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ReporteJdbcAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MovimientoReporte> obtenerPorClienteYFechas(UUID clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        String sql = """
                SELECT DISTINCT
                    t.id,
                    t.cuenta_origen_id,
                    t.cuenta_destino_id,
                    tt.nombre AS tipo,
                    t.monto,
                    t.saldo_posterior,
                    t.descripcion,
                    t.referencia,
                    t.estado,
                    t.created_at
                FROM transaccion t
                JOIN tipo_transaccion tt ON t.tipo_id = tt.id
                LEFT JOIN cuenta co ON t.cuenta_origen_id = co.id
                LEFT JOIN cuenta cd ON t.cuenta_destino_id = cd.id
                WHERE (co.cliente_id = :clienteId OR cd.cliente_id = :clienteId)
                  AND t.created_at >= :fechaInicio
                  AND t.created_at < :fechaFinPlusOneDay
                  AND t.estado = 'COMPLETADA'
                ORDER BY t.created_at DESC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("clienteId", clienteId);
        params.addValue("fechaInicio", fechaInicio.atStartOfDay());
        params.addValue("fechaFinPlusOneDay", fechaFin.plusDays(1).atStartOfDay());

        return jdbcTemplate.query(sql, params, this::mapRowToMovimientoReporte);
    }

    @Override
    public BigDecimal obtenerSaldoConsolidadoPorCliente(UUID clienteId) {
        String sql = """
                SELECT COALESCE(SUM(saldo), 0)
                FROM cuenta
                WHERE cliente_id = :clienteId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("clienteId", clienteId);

        BigDecimal saldo = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);
        return saldo != null ? saldo : BigDecimal.ZERO;
    }

    private MovimientoReporte mapRowToMovimientoReporte(ResultSet rs, int rowNum) throws SQLException {
        String cuentaOrigen = rs.getString("cuenta_origen_id");
        String cuentaDestino = rs.getString("cuenta_destino_id");

        return new MovimientoReporte(
                rs.getString("id"),
                cuentaOrigen,
                cuentaDestino,
                rs.getString("tipo"),
                rs.getBigDecimal("monto"),
                rs.getBigDecimal("saldo_posterior"),
                rs.getString("descripcion"),
                rs.getString("referencia"),
                rs.getString("estado"),
                rs.getObject("created_at", OffsetDateTime.class)
        );
    }
}
