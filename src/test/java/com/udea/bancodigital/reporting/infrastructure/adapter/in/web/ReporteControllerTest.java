package com.udea.bancodigital.reporting.infrastructure.adapter.in.web;

import com.udea.bancodigital.reporting.application.dto.CuentaReporteResponseDto;
import com.udea.bancodigital.reporting.application.dto.MovimientoReporteResponseDto;
import com.udea.bancodigital.reporting.application.dto.ResumenMovimientosResponseDto;
import com.udea.bancodigital.reporting.application.dto.SaldoTotalClienteResponseDto;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarCuentasClientePort;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarResumenMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarSaldoTotalClientePort;
import com.udea.bancodigital.reporting.infrastructure.config.SecurityConfig;
import com.udea.bancodigital.reporting.infrastructure.security.IdentityServiceClient;
import com.udea.bancodigital.shared.security.AuthenticatedClientProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private ConsultarMovimientosPort consultarMovimientosPort;

    @MockBean
    private ConsultarResumenMovimientosPort consultarResumenMovimientosPort;

    @MockBean
    private ConsultarCuentasClientePort consultarCuentasClientePort;

    @MockBean
    private AuthenticatedClientProvider authenticatedClientProvider;

    @MockBean
    private IdentityServiceClient identityServiceClient;

    @Test
    @WithMockUser(authorities = {"PERM_GENERATE_OWN_REPORTS", "PERM_READ_OWN_BALANCE"})
    void getSaldoTotal_returnsOkWithBalanceFromUseCase() throws Exception {
        UUID clienteId = UUID.randomUUID();
        SaldoTotalClienteResponseDto response = SaldoTotalClienteResponseDto.builder()
                .saldoTotal(new BigDecimal("1000.00"))
                .build();
        when(authenticatedClientProvider.getClienteId()).thenReturn(clienteId);
        when(consultarSaldoTotalClientePort.consultarSaldoTotal(clienteId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/reportes/saldo-total").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.saldoTotal").value(1000.00));
    }

    @Test
    @WithMockUser(authorities = {"PERM_GENERATE_OWN_REPORTS"})
    void getMovimientos_returnsListFromUseCase() throws Exception {
        UUID clienteId = UUID.randomUUID();
        when(authenticatedClientProvider.getClienteId()).thenReturn(clienteId);
        when(consultarMovimientosPort.consultar(eq(clienteId), any(), any(), any(), any()))
                .thenReturn(List.of(MovimientoReporteResponseDto.builder().tipoMovimiento("DEPOSITO").build()));

        mockMvc.perform(get("/api/v1/reportes/movimientos").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tipoMovimiento").value("DEPOSITO"));
    }

    @Test
    @WithMockUser(authorities = {"PERM_GENERATE_OWN_REPORTS"})
    void getResumenMovimientos_returnsTotalsFromUseCase() throws Exception {
        UUID clienteId = UUID.randomUUID();
        when(authenticatedClientProvider.getClienteId()).thenReturn(clienteId);
        when(consultarResumenMovimientosPort.consultar(eq(clienteId), any(), any()))
                .thenReturn(ResumenMovimientosResponseDto.builder()
                        .totalIngresos(new BigDecimal("500"))
                        .totalEgresos(new BigDecimal("120"))
                        .cantidadMovimientos(7)
                        .build());

        mockMvc.perform(get("/api/v1/reportes/resumen-movimientos").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIngresos").value(500))
                .andExpect(jsonPath("$.data.cantidadMovimientos").value(7));
    }

    @Test
    @WithMockUser(authorities = {"PERM_GENERATE_OWN_REPORTS"})
    void getCuentas_returnsAccountListFromUseCase() throws Exception {
        UUID clienteId = UUID.randomUUID();
        when(authenticatedClientProvider.getClienteId()).thenReturn(clienteId);
        when(consultarCuentasClientePort.consultar(clienteId)).thenReturn(List.of(
                CuentaReporteResponseDto.builder().numeroCuenta("0001").build(),
                CuentaReporteResponseDto.builder().numeroCuenta("0002").build()
        ));

        mockMvc.perform(get("/api/v1/reportes/cuentas").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].numeroCuenta").value("0001"))
                .andExpect(jsonPath("$.data[1].numeroCuenta").value("0002"));
    }
}
