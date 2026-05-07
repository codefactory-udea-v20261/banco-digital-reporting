package com.udea.bancodigital.reporting.application.usecase;

import com.udea.bancodigital.reporting.application.dto.CuentaReporteResponseDto;
import com.udea.bancodigital.reporting.domain.port.out.ListarCuentasClientePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarCuentasClienteUseCaseTest {

    @Mock
    private ListarCuentasClientePort listarCuentasClientePort;

    @InjectMocks
    private ConsultarCuentasClienteUseCase useCase;

    @Test
    void consultar_givenClienteId_returnsAccountsFromPort() {
        UUID clienteId = UUID.randomUUID();
        List<CuentaReporteResponseDto> expected = List.of(
                CuentaReporteResponseDto.builder().numeroCuenta("0001").build(),
                CuentaReporteResponseDto.builder().numeroCuenta("0002").build()
        );
        when(listarCuentasClientePort.listar(clienteId)).thenReturn(expected);

        List<CuentaReporteResponseDto> result = useCase.consultar(clienteId);

        assertThat(result).isSameAs(expected);
    }
}
