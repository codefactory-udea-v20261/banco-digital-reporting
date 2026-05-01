package com.udea.bancodigital.reporting.infrastructure.adapter.in.rest;

import com.udea.bancodigital.reporting.domain.model.ReporteActividad;
import com.udea.bancodigital.reporting.domain.model.TotalMovimientos;
import com.udea.bancodigital.reporting.domain.port.in.GenerarReporteActividadPort;
import com.udea.bancodigital.reporting.infrastructure.adapter.out.AuthServiceAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReporteActividadController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReporteActividadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenerarReporteActividadPort generarReporteActividadPort;

    @MockBean
    private AuthServiceAdapter authServiceAdapter;

    @Test
    void generarReporteActividad_DebeRetornar200() throws Exception {

        UUID clienteId = UUID.randomUUID();
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fin = LocalDate.now();

        ReporteActividad reporte = new ReporteActividad(
                inicio, fin, Collections.emptyList(),
                TotalMovimientos.empty(), BigDecimal.ZERO, "Mensaje");

        when(authServiceAdapter.getClienteId()).thenReturn(clienteId);
        when(generarReporteActividadPort.generar(eq(clienteId), any(), any())).thenReturn(reporte);

        mockMvc.perform(get("/api/v1/reportes/actividad")
                .param("fechaInicio", inicio.toString())
                .param("fechaFin", fin.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reporte generado"));
    }
}
