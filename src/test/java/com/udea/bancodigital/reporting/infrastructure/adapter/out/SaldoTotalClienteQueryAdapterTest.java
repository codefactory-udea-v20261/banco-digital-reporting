package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaldoTotalClienteQueryAdapterTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SaldoTotalClienteQueryAdapter adapter;

    @Test
    void obtenerSaldoTotalCliente_SuccessBigDecimal() {
        UUID id = UUID.randomUUID();
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new BigDecimal("1000.50"));

        BigDecimal result = adapter.obtenerSaldoTotalCliente(id);

        assertEquals(new BigDecimal("1000.50"), result);
    }

    @Test
    void obtenerSaldoTotalCliente_SuccessString() {
        UUID id = UUID.randomUUID();
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn("2000.75");

        BigDecimal result = adapter.obtenerSaldoTotalCliente(id);

        assertEquals(new BigDecimal("2000.75"), result);
    }

    @Test
    void obtenerSaldoTotalCliente_NullReturnsZero() {
        UUID id = UUID.randomUUID();
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(null);

        BigDecimal result = adapter.obtenerSaldoTotalCliente(id);

        assertEquals(BigDecimal.ZERO, result);
    }
}
