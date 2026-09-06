package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.stock.StockMovementRequest;
import com.autopartes.dto.stock.StockMovementResponse;
import com.autopartes.dto.stock.StockResponse;
import com.autopartes.dto.stock.StockUpdateRequest;
import com.autopartes.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
@Tag(name = "Inventario y Kardex", description = "Control de stock, movimientos de almacén y alertas de inventario")
@SecurityRequirement(name = "bearerAuth")
public class StockController {

    private final StockService stockService;

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(summary = "Obtener stock y ubicación de una autoparte (Admin/Empleado)")
    public ResponseEntity<ApiResponse<StockResponse>> getStockByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.getStockByProductId(productId)));
    }

    @PutMapping("/product/{productId}/settings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar stock mínimo y ubicación física en almacén (solo ADMIN)")
    public ResponseEntity<ApiResponse<StockResponse>> updateStockSettings(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Configuración de stock actualizada",
                stockService.updateStockSettings(productId, request)));
    }

    @PostMapping("/movement")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(summary = "Registrar movimiento manual de stock: ENTRADA, SALIDA o AJUSTE (Kardex)")
    public ResponseEntity<ApiResponse<StockMovementResponse>> recordMovement(
            @Valid @RequestBody StockMovementRequest request,
            Principal principal) {
        String userEmail = principal != null ? principal.getName() : "Sistema";
        return ResponseEntity.ok(ApiResponse.ok("Movimiento de inventario registrado",
                stockService.recordMovement(request, userEmail)));
    }

    @GetMapping("/low-alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(summary = "Listar alertas de autopartes con stock bajo o agotado (Admin/Empleado)")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getLowStockAlerts() {
        return ResponseEntity.ok(ApiResponse.ok(stockService.getLowStockAlerts()));
    }

    @GetMapping("/movements/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(summary = "Historial Kardex de movimientos de una autoparte específica")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getMovementsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.getMovementsByProductId(productId)));
    }

    @GetMapping("/movements/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(summary = "Últimos movimientos de almacén de toda la tienda (Kardex global)")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getRecentMovements() {
        return ResponseEntity.ok(ApiResponse.ok(stockService.getRecentMovements()));
    }
}
