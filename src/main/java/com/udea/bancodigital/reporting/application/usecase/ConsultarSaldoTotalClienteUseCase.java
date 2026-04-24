package com.udea.bancodigital.reporting.application.usecase;

import com.udea.bancodigital.reporting.application.dto.SaldoTotalClienteResponseDto;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarSaldoTotalClientePort;
import com.udea.bancodigital.reporting.domain.port.out.SaldoTotalClienteQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultarSaldoTotalClienteUseCase implements ConsultarSaldoTotalClientePort {

    private final SaldoTotalClienteQueryPort saldoTotalClienteQueryPort;

    @Override
    public SaldoTotalClienteResponseDto consultarSaldoTotal(UUID clienteId) {
        log.info("Generando reporte de saldo consolidado para cliente {}", clienteId);
        BigDecimal saldoTotal = saldoTotalClienteQueryPort.obtenerSaldoTotalCliente(clienteId);

        return SaldoTotalClienteResponseDto.builder()
                .clienteId(clienteId)
                .saldoTotal(saldoTotal)
                .build();
    }
}
