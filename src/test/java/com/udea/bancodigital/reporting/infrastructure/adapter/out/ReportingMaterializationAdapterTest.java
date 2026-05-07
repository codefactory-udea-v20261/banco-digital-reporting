package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportingMaterializationAdapterTest {

    @Mock
    private NamedParameterJdbcTemplate jdbc;

    @Mock
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @InjectMocks
    private ReportingMaterializationAdapter adapter;

    @Nested
    class CustomerCreatedHandler {

        @Test
        void materializeReportingView_givenCustomerCreated_issuesUpsertWithSplitName() {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", "evt-1");
            event.put("aggregateId", "agg-1");
            event.put("customer_id", "11111111-1111-1111-1111-111111111111");
            event.put("email", "alice@bank.com");
            event.put("full_name", "Alice Maria Smith");
            event.put("document_number", "CC-9001");
            event.put("phone", "+57-300-1234567");

            adapter.materializeReportingView(event, "CustomerCreated");

            ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(jdbc).update(contains("INTO cliente"), captor.capture());

            Map<String, Object> values = captor.getValue().getValues();
            assertThat(values)
                    .containsEntry("id", "11111111-1111-1111-1111-111111111111")
                    .containsEntry("numero_cedula", "CC-9001")
                    .containsEntry("email", "alice@bank.com")
                    .containsEntry("telefono", "+57-300-1234567")
                    .containsEntry("primer_nombre", "Alice")
                    .containsEntry("primer_apellido", "Maria Smith");
        }

        @Test
        void materializeReportingView_givenSingleWordName_putsItInPrimerNombreAndLeavesApellidoNull() {
            Map<String, Object> event = baseCustomerEvent();
            event.put("full_name", "Alice");

            adapter.materializeReportingView(event, "CustomerCreated");

            ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(jdbc).update(anyString(), captor.capture());
            Map<String, Object> values = captor.getValue().getValues();
            assertThat(values)
                    .containsEntry("primer_nombre", "Alice")
                    .containsEntry("primer_apellido", null);
        }

        @Test
        void materializeReportingView_givenMissingCustomerId_fallsBackToAggregateId() {
            Map<String, Object> event = new HashMap<>();
            event.put("aggregateId", "agg-fallback");
            event.put("email", "bob@bank.com");

            adapter.materializeReportingView(event, "CustomerCreated");

            ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(jdbc).update(anyString(), captor.capture());
            assertThat(captor.getValue().getValues()).containsEntry("id", "agg-fallback");
        }

        @Test
        void materializeReportingView_givenNoIdAtAll_skipsTheInsert() {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", "evt-9");

            adapter.materializeReportingView(event, "CustomerCreated");

            verify(jdbc, never()).update(anyString(), any(MapSqlParameterSource.class));
        }
    }

    @Nested
    class AccountOpenedHandler {

        @Test
        void materializeReportingView_givenAccountOpened_mapsAllFieldsIncludingTipoLookup() {
            Map<String, Object> event = new HashMap<>();
            event.put("aggregateId", "agg-acc");
            event.put("account_id", "22222222-2222-2222-2222-222222222222");
            event.put("customer_id", "11111111-1111-1111-1111-111111111111");
            event.put("account_number", "0102-9988");
            event.put("account_type", "AHORRO");
            event.put("initial_balance", new BigDecimal("150.00"));
            event.put("status", "ACTIVA");

            adapter.materializeReportingView(event, "AccountOpened");

            ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(jdbc).update(contains("INTO cuenta"), captor.capture());

            Map<String, Object> values = captor.getValue().getValues();
            assertThat(values)
                    .containsEntry("id", "22222222-2222-2222-2222-222222222222")
                    .containsEntry("cliente_id", "11111111-1111-1111-1111-111111111111")
                    .containsEntry("numero_cuenta", "0102-9988")
                    .containsEntry("tipo_cuenta_id", 1)
                    .containsEntry("estado", "ACTIVA")
                    .containsEntry("saldo", new BigDecimal("150.00"));
        }

        @Test
        void materializeReportingView_givenAccountOpenedMissingId_skipsTheInsert() {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", "evt-noid");

            adapter.materializeReportingView(event, "AccountOpened");

            verify(jdbc, never()).update(anyString(), any(MapSqlParameterSource.class));
        }
    }

    @Nested
    class TransactionCompletedHandler {

        @ParameterizedTest
        @CsvSource({
                "DEPOSITO, 1",
                "RETIRO, 2",
                "TRANSFERENCIA_DEBITO, 3",
                "TRANSFERENCIA_CREDITO, 4",
                "DEPOSIT, 1",
                "WITHDRAWAL, 2",
                "TRANSFER_DEBIT, 3",
                "TRANSFER_CREDIT, 4"
        })
        void materializeReportingView_givenTransactionType_mapsTipoIdCorrectly(String transactionType, int expectedTipoId) {
            Map<String, Object> event = baseTransactionEvent();
            event.put("transaction_type", transactionType);

            adapter.materializeReportingView(event, "TransactionCompleted");

            ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(jdbc).update(contains("INTO transaccion"), captor.capture());
            assertThat(captor.getValue().getValues()).containsEntry("tipo_id", expectedTipoId);
        }

        @Test
        void materializeReportingView_givenUnknownTransactionType_leavesTipoIdNull() {
            Map<String, Object> event = baseTransactionEvent();
            event.put("transaction_type", "MAGIC_TYPE");

            adapter.materializeReportingView(event, "TransactionCompleted");

            ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(jdbc).update(anyString(), captor.capture());
            assertThat(captor.getValue().getValues()).containsEntry("tipo_id", null);
        }

        @Test
        void materializeReportingView_givenAmountAsNumber_convertsToBigDecimal() {
            Map<String, Object> event = baseTransactionEvent();
            event.put("amount", 250);

            adapter.materializeReportingView(event, "TransactionCompleted");

            ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(jdbc).update(anyString(), captor.capture());
            assertThat(captor.getValue().getValues()).containsEntry("monto", new BigDecimal("250"));
        }

        @Test
        void materializeReportingView_givenTransactionMissingId_skipsTheInsert() {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", "evt-noid");

            adapter.materializeReportingView(event, "TransactionCompleted");

            verify(jdbc, never()).update(anyString(), any(MapSqlParameterSource.class));
        }
    }

    @Test
    void materializeReportingView_givenUnknownEventType_writesNothing() {
        Map<String, Object> event = baseCustomerEvent();

        adapter.materializeReportingView(event, "SomeUnknownEvent");

        verify(jdbc, never()).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    void materializeFallback_onJdbcFailure_queuesEventToPendingTopic() {
        Map<String, Object> event = baseCustomerEvent();

        ReflectionTestUtils.invokeMethod(adapter, "materializeFallback", event,
                "CustomerCreated", new RuntimeException("db down"));

        verify(kafkaTemplate).send(eq("reporting-events-pending"), eq(String.valueOf(event.get("aggregateId"))), any());
    }

    @Test
    void materializeFallback_givenKafkaAlsoDown_doesNotPropagate() {
        Map<String, Object> event = baseCustomerEvent();
        doThrow(new RuntimeException("kafka down"))
                .when(kafkaTemplate).send(anyString(), anyString(), any());

        ReflectionTestUtils.invokeMethod(adapter, "materializeFallback", event,
                "CustomerCreated", new RuntimeException("db down"));
    }

    @Test
    void getStatus_returnsCircuitBreakerName() {
        Map<String, Object> status = adapter.getStatus();

        assertThat(status).containsEntry("circuitBreakerName", "reporting-database");
    }

    private Map<String, Object> baseCustomerEvent() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", "evt-base");
        event.put("aggregateId", "agg-base");
        event.put("customer_id", "11111111-1111-1111-1111-111111111111");
        event.put("email", "user@bank.com");
        event.put("full_name", "User Test");
        return event;
    }

    private Map<String, Object> baseTransactionEvent() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", "evt-tx");
        event.put("aggregateId", "agg-tx");
        event.put("transaction_id", "33333333-3333-3333-3333-333333333333");
        event.put("from_account_id", "22222222-2222-2222-2222-222222222222");
        event.put("to_account_id", "44444444-4444-4444-4444-444444444444");
        event.put("amount", new BigDecimal("100.00"));
        event.put("status", "COMPLETADA");
        event.put("description", "Test movement");
        return event;
    }
}

@ExtendWith(MockitoExtension.class)
class ReportingMaterializationAdapterAccountTypeMappingTest {
    @Mock
    private NamedParameterJdbcTemplate jdbc;
    @Mock
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    @InjectMocks
    private ReportingMaterializationAdapter adapter;

    @ParameterizedTest
    @CsvSource({
            "AHORRO, 1",
            "AHORROS, 1",
            "SAVINGS, 1",
            "CORRIENTE, 2",
            "CHECKING, 2"
    })
    void materializeReportingView_accountType_mapsTipoCuentaIdCorrectly(String accountType, int expected) {
        Map<String, Object> event = new HashMap<>();
        event.put("aggregateId", "agg");
        event.put("account_id", "55555555-5555-5555-5555-555555555555");
        event.put("account_type", accountType);

        adapter.materializeReportingView(event, "AccountOpened");

        ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).update(anyString(), captor.capture());
        assertThat(captor.getValue().getValues()).containsEntry("tipo_cuenta_id", expected);
    }

    @Test
    void materializeReportingView_unknownAccountType_leavesTipoCuentaIdNull() {
        Map<String, Object> event = new HashMap<>();
        event.put("aggregateId", "agg");
        event.put("account_id", "55555555-5555-5555-5555-555555555555");
        event.put("account_type", "MYSTERY");

        adapter.materializeReportingView(event, "AccountOpened");

        ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).update(anyString(), captor.capture());
        assertThat(captor.getValue().getValues()).containsEntry("tipo_cuenta_id", null);
    }
}
