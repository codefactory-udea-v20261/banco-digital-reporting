package com.udea.bancodigital.reporting.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DataMigrationUseCase implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataMigrationUseCase.class);

    private final JdbcTemplate reportingJdbcTemplate;
    private final JdbcTemplate coreJdbcTemplate;

    public DataMigrationUseCase(
            JdbcTemplate reportingJdbcTemplate,
            @Qualifier("coreJdbcTemplate") JdbcTemplate coreJdbcTemplate
    ) {
        this.reportingJdbcTemplate = reportingJdbcTemplate;
        this.coreJdbcTemplate = coreJdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting data migration from banco_digital_core to banco_digital_reporting...");

        try {
            migrateClientes();
            migrateCuentas();
            migrateTransacciones();
            logger.info("Data migration completed successfully.");
        } catch (Exception e) {
            logger.error("Error during data migration: {}", e.getMessage(), e);
        }
    }

    private void migrateClientes() {
        logger.info("Migrating clientes...");
        String selectSql = "SELECT id, numero_cedula, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, email, telefono, fecha_nacimiento, activo FROM cliente";
        List<Map<String, Object>> clientes = coreJdbcTemplate.queryForList(selectSql);

        for (Map<String, Object> cliente : clientes) {
            String insertSql = """
                INSERT INTO cliente (id, numero_cedula, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, email, telefono, fecha_nacimiento, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (numero_cedula) DO NOTHING
                """;
            reportingJdbcTemplate.update(insertSql,
                    cliente.get("id"),
                    cliente.get("numero_cedula"),
                    cliente.get("primer_nombre"),
                    cliente.get("segundo_nombre"),
                    cliente.get("primer_apellido"),
                    cliente.get("segundo_apellido"),
                    cliente.get("email"),
                    cliente.get("telefono"),
                    cliente.get("fecha_nacimiento"),
                    cliente.get("activo")
            );
        }
    }

    private void migrateCuentas() {
        logger.info("Migrating cuentas...");
        String selectSql = "SELECT id, numero_cuenta, cliente_id, tipo_cuenta_id, saldo, estado, fecha_apertura FROM cuenta";
        List<Map<String, Object>> cuentas = coreJdbcTemplate.queryForList(selectSql);

        for (Map<String, Object> cuenta : cuentas) {
            String insertSql = """
                INSERT INTO cuenta (id, numero_cuenta, cliente_id, tipo_cuenta_id, saldo, estado, fecha_apertura)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (numero_cuenta) DO NOTHING
                """;
            reportingJdbcTemplate.update(insertSql,
                    cuenta.get("id"),
                    cuenta.get("numero_cuenta"),
                    cuenta.get("cliente_id"),
                    cuenta.get("tipo_cuenta_id"),
                    cuenta.get("saldo"),
                    cuenta.get("estado"),
                    cuenta.get("fecha_apertura")
            );
        }
    }

    private void migrateTransacciones() {
        logger.info("Migrating transacciones...");
        String selectSql = "SELECT id, cuenta_origen_id, cuenta_destino_id, tipo_id, monto, saldo_anterior, saldo_posterior, descripcion, referencia, estado, created_at FROM transaccion";
        List<Map<String, Object>> transacciones = coreJdbcTemplate.queryForList(selectSql);

        for (Map<String, Object> t : transacciones) {
            String insertSql = """
                INSERT INTO transaccion (id, cuenta_origen_id, cuenta_destino_id, tipo_id, monto, saldo_anterior, saldo_posterior, descripcion, referencia, estado, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (referencia) DO NOTHING
                """;
            reportingJdbcTemplate.update(insertSql,
                    t.get("id"),
                    t.get("cuenta_origen_id"),
                    t.get("cuenta_destino_id"),
                    t.get("tipo_id"),
                    t.get("monto"),
                    t.get("saldo_anterior"),
                    t.get("saldo_posterior"),
                    t.get("descripcion"),
                    t.get("referencia"),
                    t.get("estado"),
                    t.get("created_at")
            );
        }
    }
}
