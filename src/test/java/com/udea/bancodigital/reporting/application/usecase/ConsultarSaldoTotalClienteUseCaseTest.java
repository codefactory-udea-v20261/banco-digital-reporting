package com.udea.bancodigital.reporting.application.usecase;

import com.udea.bancodigital.reporting.application.dto.SaldoTotalClienteResponseDto;
import com.udea.bancodigital.reporting.domain.port.out.SaldoTotalClienteQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarSaldoTotalClienteUseCaseTest {

    @Mock
    private SaldoTotalClienteQueryPort queryPort;

    private ConsultarSaldoTotalClienteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarSaldoTotalClienteUseCase(queryPort);
    }

    @Test
    void consultarSaldoTotal_DebeRetornarSaldoCorrecto() {

        UUID clienteId = UUID.randomUUID();
        BigDecimal saldoEsperado = new BigDecimal("1500.50");
        when(queryPort.obtenerSaldoTotalCliente(clienteId)).thenReturn(saldoEsperado);

        SaldoTotalClienteResponseDto response = useCase.consultarSaldoTotal(clienteId);

        assertNotNull(response);
        assertEquals(clienteId, response.getClienteId());
        assertEquals(saldoEsperado, response.getSaldoTotal());
    }
}
