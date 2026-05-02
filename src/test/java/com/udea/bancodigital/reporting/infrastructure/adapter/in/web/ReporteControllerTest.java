package com.udea.bancodigital.reporting.infrastructure.adapter.in.web;

import com.udea.bancodigital.reporting.domain.port.in.ConsultarSaldoTotalClientePort;
import com.udea.bancodigital.shared.security.AuthenticatedClientProvider;
import com.udea.bancodigital.reporting.application.dto.SaldoTotalClienteResponseDto;
import com.udea.bancodigital.reporting.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReporteController.class)
@Import(SecurityConfig.class)
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsultarSaldoTotalClientePort consultarSaldoTotalClientePort;

    @MockBean
    private AuthenticatedClientProvider authenticatedClientProvider;

    @Test
    @WithMockUser(roles = "CLIENTE")
    void getSaldoTotal_ShouldReturnOk() throws Exception {
        UUID clienteId = UUID.randomUUID();
        SaldoTotalClienteResponseDto response = SaldoTotalClienteResponseDto.builder()
                .saldoTotal(new BigDecimal("1000.00"))
                .build();

        when(authenticatedClientProvider.getClienteId()).thenReturn(clienteId);
        when(consultarSaldoTotalClientePort.consultarSaldoTotal(clienteId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reportes/saldo-total")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.saldoTotal").value(1000.00));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void getResumenMovimientos_ShouldReturnNotImplemented() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/resumen-movimientos")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotImplemented());
    }
}
