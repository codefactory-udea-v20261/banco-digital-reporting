package com.udea.bancodigital.reporting.infrastructure.adapter.in.rest;

import com.udea.bancodigital.reporting.domain.port.in.GenerarReporteActividadPort;
import com.udea.bancodigital.reporting.domain.model.ReporteActividad;
import com.udea.bancodigital.shared.security.AuthenticatedClientProvider;
import com.udea.bancodigital.shared.web.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteActividadController {

    private final GenerarReporteActividadPort generarReporteActividadPort;
    private final AuthenticatedClientProvider authenticatedClientProvider;

    public ReporteActividadController(
            GenerarReporteActividadPort generarReporteActividadPort,
            AuthenticatedClientProvider authenticatedClientProvider
    ) {
        this.generarReporteActividadPort = generarReporteActividadPort;
        this.authenticatedClientProvider = authenticatedClientProvider;
    }

    @GetMapping("/actividad")
    public ResponseEntity<ApiResponse<ReporteActividad>> generarReporteActividad(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        UUID clienteId = authenticatedClientProvider.getClienteId();
        ReporteActividad reporte = generarReporteActividadPort.generar(clienteId, fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.ok("Reporte generado", reporte));
    }
}
