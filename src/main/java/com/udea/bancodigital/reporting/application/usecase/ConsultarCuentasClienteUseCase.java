package com.udea.bancodigital.reporting.application.usecase;

import com.udea.bancodigital.reporting.application.dto.CuentaReporteResponseDto;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarCuentasClientePort;
import com.udea.bancodigital.reporting.domain.port.out.ListarCuentasClientePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultarCuentasClienteUseCase implements ConsultarCuentasClientePort {

    private final ListarCuentasClientePort listarCuentasClientePort;

    @Override
    @Transactional(readOnly = true, timeout = 30)
    public List<CuentaReporteResponseDto> consultar(UUID clienteId) {
        return listarCuentasClientePort.listar(clienteId);
    }
}
