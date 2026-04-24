package com.udea.bancodigital.reporting.infrastructure.adapter.out;

import com.udea.bancodigital.reporting.domain.port.out.SaldoTotalClienteQueryPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SaldoTotalClienteQueryAdapter implements SaldoTotalClienteQueryPort {

    private final EntityManager entityManager;

    @Override
    public BigDecimal obtenerSaldoTotalCliente(UUID clienteId) {
        Object result = entityManager.createNativeQuery("SELECT obtener_saldo_total_cliente(CAST(:clienteId AS UUID))")
                .setParameter("clienteId", clienteId.toString())
                .getSingleResult();

        if (result == null) {
            return BigDecimal.ZERO;
        }

        return result instanceof BigDecimal decimal
                ? decimal
                : new BigDecimal(result.toString());
    }
}
