package com.udea.bancodigital.reporting.infrastructure.adapter.in.web;

import com.udea.bancodigital.reporting.application.dto.CuentaReporteResponseDto;
import com.udea.bancodigital.reporting.application.dto.MovimientoReporteResponseDto;
import com.udea.bancodigital.reporting.application.dto.ResumenMovimientosResponseDto;
import com.udea.bancodigital.reporting.application.dto.SaldoTotalClienteResponseDto;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarCuentasClientePort;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarResumenMovimientosPort;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarSaldoTotalClientePort;
import com.udea.bancodigital.shared.security.AuthenticatedClientProvider;
import com.udea.bancodigital.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "API de consultas analiticas y reportes del dominio bancario")
public class ReporteController {

    private final ConsultarSaldoTotalClientePort consultarSaldoTotalClientePort;
    private final ConsultarMovimientosPort consultarMovimientosPort;
    private final ConsultarResumenMovimientosPort consultarResumenMovimientosPort;
    private final ConsultarCuentasClientePort consultarCuentasClientePort;
    private final AuthenticatedClientProvider authenticatedClientProvider;

    @GetMapping("/saldo-total")
    @Operation(summary = "Consultar saldo total consolidado")
    public ResponseEntity<ApiResponse<SaldoTotalClienteResponseDto>> consultarSaldoTotal() {
        UUID clienteId = authenticatedClientProvider.getClienteId();
        SaldoTotalClienteResponseDto response = consultarSaldoTotalClientePort.consultarSaldoTotal(clienteId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/movimientos")
    @Operation(summary = "Consultar movimientos del cliente con filtros opcionales")
    public ResponseEntity<ApiResponse<List<MovimientoReporteResponseDto>>> consultarMovimientos(
            @RequestParam(required = false) UUID cuentaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String tipo
    ) {
        UUID clienteId = authenticatedClientProvider.getClienteId();
        List<MovimientoReporteResponseDto> response =
                consultarMovimientosPort.consultar(clienteId, cuentaId, fechaDesde, fechaHasta, tipo);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/resumen-movimientos")
    @Operation(summary = "Consultar resumen de ingresos, egresos y volumen de movimientos")
    public ResponseEntity<ApiResponse<ResumenMovimientosResponseDto>> consultarResumenMovimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta
    ) {
        UUID clienteId = authenticatedClientProvider.getClienteId();
        ResumenMovimientosResponseDto response =
                consultarResumenMovimientosPort.consultar(clienteId, fechaDesde, fechaHasta);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/cuentas")
    @Operation(summary = "Consultar cuentas del cliente para reportes")
    public ResponseEntity<ApiResponse<List<CuentaReporteResponseDto>>> consultarCuentasCliente() {
        UUID clienteId = authenticatedClientProvider.getClienteId();
        List<CuentaReporteResponseDto> response = consultarCuentasClientePort.consultar(clienteId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
