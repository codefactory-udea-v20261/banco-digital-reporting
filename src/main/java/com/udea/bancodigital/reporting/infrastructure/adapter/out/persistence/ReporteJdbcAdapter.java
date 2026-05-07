package com.udea.bancodigital.reporting.infrastructure.adapter.out.persistence;

import com.udea.bancodigital.reporting.application.dto.CuentaReporteResponseDto;
import com.udea.bancodigital.reporting.application.dto.MovimientoReporteResponseDto;
import com.udea.bancodigital.reporting.application.dto.ResumenMovimientosResponseDto;
import com.udea.bancodigital.reporting.domain.model.MovimientoReporte;
import com.udea.bancodigital.reporting.domain.port.out.BuscarMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.out.ListarCuentasClientePort;
import com.udea.bancodigital.reporting.domain.port.out.ObtenerMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.out.ObtenerResumenMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.out.ObtenerSaldoCuentasPort;
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
public class ReporteJdbcAdapter implements
        ObtenerMovimientosPort,
        ObtenerSaldoCuentasPort,
        BuscarMovimientosPort,
        ObtenerResumenMovimientosPort,
        ListarCuentasClientePort {

    // Categorisation reused by the resumen query: incomes vs outgoings.
    private static final List<String> TIPOS_INGRESO = List.of("DEPOSITO", "TRANSFERENCIA_CREDITO");
    private static final List<String> TIPOS_EGRESO  = List.of("RETIRO", "TRANSFERENCIA_DEBITO");

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

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("clienteId", clienteId)
                .addValue("fechaInicio", fechaInicio.atStartOfDay())
                .addValue("fechaFinPlusOneDay", fechaFin.plusDays(1).atStartOfDay());

        return jdbcTemplate.query(sql, params, this::mapRowToMovimientoReporte);
    }

    @Override
    public BigDecimal obtenerSaldoConsolidadoPorCliente(UUID clienteId) {
        String sql = """
                SELECT COALESCE(SUM(saldo), 0)
                FROM cuenta
                WHERE cliente_id = :clienteId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("clienteId", clienteId);

        BigDecimal saldo = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);
        return saldo != null ? saldo : BigDecimal.ZERO;
    }

    @Override
    public List<MovimientoReporteResponseDto> buscar(
            UUID clienteId,
            UUID cuentaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String tipo
    ) {
        // The dynamic predicates use IS NULL guards so a single SQL plan handles all
        // optional-filter combinations without string concatenation.
        String sql = """
                SELECT DISTINCT
                    t.id,
                    COALESCE(t.cuenta_origen_id, t.cuenta_destino_id) AS cuenta_id,
                    tt.nombre AS tipo,
                    t.monto,
                    t.descripcion,
                    t.created_at
                FROM transaccion t
                JOIN tipo_transaccion tt ON t.tipo_id = tt.id
                LEFT JOIN cuenta co ON t.cuenta_origen_id = co.id
                LEFT JOIN cuenta cd ON t.cuenta_destino_id = cd.id
                WHERE (co.cliente_id = :clienteId OR cd.cliente_id = :clienteId)
                  AND t.created_at >= :fechaDesde
                  AND t.created_at <  :fechaHastaExclusive
                  AND (:cuentaId IS NULL OR t.cuenta_origen_id = :cuentaId OR t.cuenta_destino_id = :cuentaId)
                  AND (:tipo IS NULL OR tt.nombre = :tipo)
                  AND t.estado = 'COMPLETADA'
                ORDER BY t.created_at DESC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("clienteId", clienteId)
                .addValue("cuentaId", cuentaId)
                .addValue("tipo", tipo)
                .addValue("fechaDesde", fechaDesde.atStartOfDay())
                .addValue("fechaHastaExclusive", fechaHasta.plusDays(1).atStartOfDay());

        return jdbcTemplate.query(sql, params, this::mapRowToMovimientoDto);
    }

    @Override
    public ResumenMovimientosResponseDto obtener(UUID clienteId, LocalDate fechaDesde, LocalDate fechaHasta) {
        String sql = """
                SELECT
                    COALESCE(SUM(CASE WHEN tt.nombre IN (:tiposIngreso) THEN t.monto ELSE 0 END), 0) AS total_ingresos,
                    COALESCE(SUM(CASE WHEN tt.nombre IN (:tiposEgreso)  THEN t.monto ELSE 0 END), 0) AS total_egresos,
                    COUNT(DISTINCT t.id) AS cantidad
                FROM transaccion t
                JOIN tipo_transaccion tt ON t.tipo_id = tt.id
                LEFT JOIN cuenta co ON t.cuenta_origen_id = co.id
                LEFT JOIN cuenta cd ON t.cuenta_destino_id = cd.id
                WHERE (co.cliente_id = :clienteId OR cd.cliente_id = :clienteId)
                  AND t.created_at >= :fechaDesde
                  AND t.created_at <  :fechaHastaExclusive
                  AND t.estado = 'COMPLETADA'
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("clienteId", clienteId)
                .addValue("tiposIngreso", TIPOS_INGRESO)
                .addValue("tiposEgreso", TIPOS_EGRESO)
                .addValue("fechaDesde", fechaDesde.atStartOfDay())
                .addValue("fechaHastaExclusive", fechaHasta.plusDays(1).atStartOfDay());

        return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> ResumenMovimientosResponseDto.builder()
                .totalIngresos(rs.getBigDecimal("total_ingresos"))
                .totalEgresos(rs.getBigDecimal("total_egresos"))
                .cantidadMovimientos(rs.getInt("cantidad"))
                .build());
    }

    @Override
    public List<CuentaReporteResponseDto> listar(UUID clienteId) {
        String sql = """
                SELECT
                    c.id,
                    c.numero_cuenta,
                    tc.nombre AS tipo,
                    c.estado,
                    c.saldo
                FROM cuenta c
                LEFT JOIN tipo_cuenta tc ON c.tipo_cuenta_id = tc.id
                WHERE c.cliente_id = :clienteId
                ORDER BY c.created_at DESC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("clienteId", clienteId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> CuentaReporteResponseDto.builder()
                .cuentaId(rs.getObject("id", UUID.class))
                .numeroCuenta(rs.getString("numero_cuenta"))
                .tipoCuenta(rs.getString("tipo"))
                .estado(rs.getString("estado"))
                .saldoActual(rs.getBigDecimal("saldo"))
                .build());
    }

    private MovimientoReporte mapRowToMovimientoReporte(ResultSet rs, int rowNum) throws SQLException {
        return new MovimientoReporte(
                rs.getString("id"),
                rs.getString("cuenta_origen_id"),
                rs.getString("cuenta_destino_id"),
                rs.getString("tipo"),
                rs.getBigDecimal("monto"),
                rs.getBigDecimal("saldo_posterior"),
                rs.getString("descripcion"),
                rs.getString("referencia"),
                rs.getString("estado"),
                rs.getObject("created_at", OffsetDateTime.class)
        );
    }

    private MovimientoReporteResponseDto mapRowToMovimientoDto(ResultSet rs, int rowNum) throws SQLException {
        UUID cuentaId = rs.getObject("cuenta_id", UUID.class);
        OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
        return MovimientoReporteResponseDto.builder()
                .movimientoId(rs.getObject("id", UUID.class))
                .cuentaId(cuentaId)
                .tipoMovimiento(rs.getString("tipo"))
                .monto(rs.getBigDecimal("monto"))
                .descripcion(rs.getString("descripcion"))
                .fecha(createdAt != null ? createdAt.toInstant() : null)
                .build();
    }
}
