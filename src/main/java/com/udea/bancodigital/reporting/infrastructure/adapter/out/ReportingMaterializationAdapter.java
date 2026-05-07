package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Materialises read-model rows from domain events.
 *
 * Each handler is a single SQL upsert against the eventually-consistent
 * read tables in the reporting database. Foreign keys between cliente,
 * cuenta and transaccion were dropped in the V2/V3 squash precisely so
 * out-of-order or partial event arrival cannot block materialisation.
 *
 * Resilience: a circuit breaker (configured under name "reporting-database")
 * cuts off SQL after sustained failures and the retry policy reissues with
 * exponential backoff; when the breaker opens the fallback re-publishes the
 * event to a pending Kafka queue for later replay.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportingMaterializationAdapter {

    private static final String EVENT_ID = "eventId";
    private static final String AGGREGATE_ID = "aggregateId";

    private static final String INSERT_CLIENTE_SQL = """
            INSERT INTO cliente (
                id, numero_cedula, primer_nombre, primer_apellido,
                email, telefono,
                created_at, updated_at, created_by, updated_by
            ) VALUES (
                CAST(:id AS UUID), :numero_cedula, :primer_nombre, :primer_apellido,
                :email, :telefono,
                NOW(), NOW(), 'event:CustomerCreated', 'event:CustomerCreated'
            )
            ON CONFLICT (id) DO UPDATE SET
                numero_cedula   = COALESCE(EXCLUDED.numero_cedula, cliente.numero_cedula),
                email           = COALESCE(EXCLUDED.email, cliente.email),
                telefono        = COALESCE(EXCLUDED.telefono, cliente.telefono),
                primer_nombre   = COALESCE(EXCLUDED.primer_nombre, cliente.primer_nombre),
                primer_apellido = COALESCE(EXCLUDED.primer_apellido, cliente.primer_apellido),
                updated_at      = NOW(),
                updated_by      = 'event:CustomerCreated'
            """;

    private static final String INSERT_CUENTA_SQL = """
            INSERT INTO cuenta (
                id, numero_cuenta, cliente_id, tipo_cuenta_id,
                saldo, estado, fecha_apertura,
                created_at, updated_at, created_by, updated_by
            ) VALUES (
                CAST(:id AS UUID), :numero_cuenta, CAST(:cliente_id AS UUID), :tipo_cuenta_id,
                COALESCE(:saldo, 0), COALESCE(:estado, 'ACTIVA'), CURRENT_DATE,
                NOW(), NOW(), 'event:AccountOpened', 'event:AccountOpened'
            )
            ON CONFLICT (id) DO UPDATE SET
                saldo      = EXCLUDED.saldo,
                estado     = EXCLUDED.estado,
                updated_at = NOW(),
                updated_by = 'event:AccountOpened'
            """;

    private static final String INSERT_TRANSACCION_SQL = """
            INSERT INTO transaccion (
                id, cuenta_origen_id, cuenta_destino_id, tipo_id,
                monto, descripcion, estado, referencia,
                created_at, created_by
            ) VALUES (
                CAST(:id AS UUID), CAST(:cuenta_origen_id AS UUID), CAST(:cuenta_destino_id AS UUID), :tipo_id,
                :monto, :descripcion, COALESCE(:estado, 'COMPLETADA'), :referencia,
                COALESCE(:created_at, NOW()), 'event:TransactionCompleted'
            )
            ON CONFLICT (id) DO NOTHING
            """;

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Materialise a read-model row. The event map is the consumer's flattened
     * envelope plus the payload from JsonAnySetter; lookup keys here use the
     * snake_case names that Core publishes (customer_id, account_id, ...).
     */
    @CircuitBreaker(name = "reporting-database", fallbackMethod = "materializeFallback")
    @Retry(name = "reporting-database")
    public void materializeReportingView(Map<String, Object> event, String eventType) {
        log.debug("Materializing reporting view: type={}, eventId={}", eventType, event.get(EVENT_ID));

        switch (eventType) {
            case "CustomerCreated"      -> updateCustomerReportingView(event);
            case "AccountOpened"        -> updateAccountReportingView(event);
            case "TransactionCompleted" -> updateTransactionReportingView(event);
            default -> log.warn("Unknown event type for materialization: {}", eventType);
        }
    }

    /**
     * Fallback: queue the event to a pending Kafka topic so a future replay
     * can re-attempt materialisation once the database is reachable again.
     */
    private void materializeFallback(Map<String, Object> event, String eventType, Exception cause) {
        log.warn("Reporting database unavailable (circuit breaker OPEN). Queuing event for async materialization. type={}, error={}",
                eventType, cause.getMessage());
        try {
            Map<String, Object> pendingEvent = new HashMap<>(event);
            pendingEvent.put("eventType", eventType);
            pendingEvent.put("originalEventId", event.get(EVENT_ID));
            pendingEvent.put("timestamp", Instant.now().toString());
            pendingEvent.put("retryCount", 0);
            pendingEvent.put("reason", "Reporting database unavailable");

            kafkaTemplate.send("reporting-events-pending",
                    String.valueOf(event.get(AGGREGATE_ID)),
                    pendingEvent);
            log.info("Queued pending reporting event for aggregateId={}", event.get(AGGREGATE_ID));
        } catch (Exception kafkaError) {
            log.error("Failed to queue fallback reporting event: {}", kafkaError.getMessage());
        }
    }

    private void updateCustomerReportingView(Map<String, Object> event) {
        String customerId = stringOrAggregate(event, "customer_id");
        if (customerId == null) {
            log.warn("CustomerCreated event missing customer_id; skipping. eventId={}", event.get(EVENT_ID));
            return;
        }

        String[] names = splitFullName(asString(event.get("full_name")));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", customerId)
                .addValue("numero_cedula", asString(event.get("document_number")))
                .addValue("primer_nombre", names[0])
                .addValue("primer_apellido", names[1])
                .addValue("email", asString(event.get("email")))
                .addValue("telefono", asString(event.get("phone")));

        jdbc.update(INSERT_CLIENTE_SQL, params);
        log.info("Materialized cliente row from CustomerCreated. customerId={}", customerId);
    }

    private void updateAccountReportingView(Map<String, Object> event) {
        String accountId = stringOrAggregate(event, "account_id");
        if (accountId == null) {
            log.warn("AccountOpened event missing account_id; skipping. eventId={}", event.get(EVENT_ID));
            return;
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", accountId)
                .addValue("numero_cuenta", asString(event.get("account_number")))
                .addValue("cliente_id", asString(event.get("customer_id")))
                .addValue("tipo_cuenta_id", tipoCuentaId(asString(event.get("account_type"))))
                .addValue("saldo", asBigDecimal(event.get("initial_balance")))
                .addValue("estado", asString(event.get("status")));

        jdbc.update(INSERT_CUENTA_SQL, params);
        log.info("Materialized cuenta row from AccountOpened. accountId={}", accountId);
    }

    private void updateTransactionReportingView(Map<String, Object> event) {
        String transactionId = stringOrAggregate(event, "transaction_id");
        if (transactionId == null) {
            log.warn("TransactionCompleted event missing transaction_id; skipping. eventId={}", event.get(EVENT_ID));
            return;
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", transactionId)
                .addValue("cuenta_origen_id", asString(event.get("from_account_id")))
                .addValue("cuenta_destino_id", asString(event.get("to_account_id")))
                .addValue("tipo_id", tipoTransaccionId(asString(event.get("transaction_type"))))
                .addValue("monto", asBigDecimal(event.get("amount")))
                .addValue("descripcion", asString(event.get("description")))
                .addValue("estado", asString(event.get("status")))
                .addValue("referencia", asString(event.get("reference")))
                .addValue("created_at", event.get("timestamp"));

        jdbc.update(INSERT_TRANSACCION_SQL, params);
        log.info("Materialized transaccion row from TransactionCompleted. transactionId={}", transactionId);
    }

    private String stringOrAggregate(Map<String, Object> event, String preferredKey) {
        String fromPayload = asString(event.get(preferredKey));
        if (fromPayload != null) {
            return fromPayload;
        }
        return asString(event.get(AGGREGATE_ID));
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        String s = Objects.toString(value, "").trim();
        return s.isEmpty() ? null : s;
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer tipoCuentaId(String accountType) {
        if (accountType == null) {
            return null;
        }
        return switch (accountType.toUpperCase()) {
            case "AHORRO", "AHORROS", "SAVINGS" -> 1;
            case "CORRIENTE", "CHECKING"        -> 2;
            default -> null;
        };
    }

    private Integer tipoTransaccionId(String transactionType) {
        if (transactionType == null) {
            return null;
        }
        return switch (transactionType.toUpperCase()) {
            case "DEPOSITO", "DEPOSIT"                       -> 1;
            case "RETIRO", "WITHDRAWAL"                      -> 2;
            case "TRANSFERENCIA_DEBITO", "TRANSFER_DEBIT"    -> 3;
            case "TRANSFERENCIA_CREDITO", "TRANSFER_CREDIT"  -> 4;
            default -> null;
        };
    }

    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{null, null};
        }
        int firstSpace = fullName.indexOf(' ');
        if (firstSpace < 0) {
            return new String[]{fullName, null};
        }
        return new String[]{
                fullName.substring(0, firstSpace),
                fullName.substring(firstSpace + 1).trim()
        };
    }

    public Map<String, Object> getStatus() {
        return Map.of(
                "circuitBreakerName", "reporting-database",
                "status", "Use /actuator/health/reporting-database for details"
        );
    }
}
