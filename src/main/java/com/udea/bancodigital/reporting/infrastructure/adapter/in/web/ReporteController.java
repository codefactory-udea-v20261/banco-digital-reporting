package com.udea.bancodigital.reporting.infrastructure.adapter.in.web;

import com.udea.bancodigital.reporting.application.dto.CuentaReporteResponseDto;
import com.udea.bancodigital.reporting.application.dto.MovimientoReporteResponseDto;
import com.udea.bancodigital.reporting.application.dto.ResumenMovimientosResponseDto;
import com.udea.bancodigital.reporting.application.dto.SaldoTotalClienteResponseDto;
import com.udea.bancodigital.reporting.domain.port.in.ConsultarSaldoTotalClientePort;
import com.udea.bancodigital.shared.security.AuthenticatedClientProvider;
import com.udea.bancodigital.shared.web.ApiError;
import com.udea.bancodigital.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private final AuthenticatedClientProvider authenticatedClientProvider;

    @GetMapping("/saldo-total")
    @Operation(
            summary = "Consultar saldo total consolidado",
            description = "Ejecuta una consulta analitica para consolidar el saldo de todas las cuentas activas del cliente autenticado."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente"
            )
    })
    public ResponseEntity<ApiResponse<SaldoTotalClienteResponseDto>> consultarSaldoTotal() {
        UUID clienteId = authenticatedClientProvider.getClienteId();
        SaldoTotalClienteResponseDto response = consultarSaldoTotalClientePort.consultarSaldoTotal(clienteId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/movimientos")
    @Operation(
            summary = "Consultar movimientos del cliente",
            description = "Endpoint reservado para el historial de movimientos con filtros de cuenta, fecha y tipo. La estructura queda disponible para la siguiente iteracion."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "501",
                    description = "Estructura disponible, implementacion pendiente"
            )
    })
    public ResponseEntity<ApiResponse<List<MovimientoReporteResponseDto>>> consultarMovimientos(
            @RequestParam(required = false) UUID cuentaId,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) String tipo) {
        return notImplemented("La consulta de movimientos quedo reservada para la siguiente iteracion.");
    }

    @GetMapping("/resumen-movimientos")
    @Operation(
            summary = "Consultar resumen de movimientos",
            description = "Endpoint reservado para consolidar ingresos, egresos y volumen de movimientos del cliente."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "501",
                    description = "Estructura disponible, implementacion pendiente"
            )
    })
    public ResponseEntity<ApiResponse<ResumenMovimientosResponseDto>> consultarResumenMovimientos(
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta) {
        return notImplemented("El resumen de movimientos se deja definido, pero su implementacion queda pendiente.");
    }

    @GetMapping("/cuentas")
    @Operation(
            summary = "Consultar cuentas del cliente para reportes",
            description = "Endpoint reservado para exponer el consolidado de productos y estados de cuenta desde la API de reportes."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "501",
                    description = "Estructura disponible, implementacion pendiente"
            )
    })
    public ResponseEntity<ApiResponse<List<CuentaReporteResponseDto>>> consultarCuentasCliente() {
        return notImplemented("La consulta consolidada de cuentas quedo definida para que pueda implementarse despues.");
    }

    private <T> ResponseEntity<ApiResponse<T>> notImplemented(String message) {
        ApiError error = ApiError.builder()
                .errorCode("NOT_IMPLEMENTED")
                .message(message)
                .httpStatus(HttpStatus.NOT_IMPLEMENTED.value())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(ApiResponse.error(error));
    }
}
