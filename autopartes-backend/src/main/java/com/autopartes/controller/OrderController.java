package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.order.CreateOrderRequest;
import com.autopartes.dto.order.DashboardSummaryDTO;
import com.autopartes.dto.order.OrderResponse;
import com.autopartes.dto.order.UpdateOrderStatusRequest;
import com.autopartes.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Órdenes y Ventas", description = "Endpoints para el proceso de compra, pedidos y reportes del dashboard")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Crear orden de compra a partir de los ítems del carrito del cliente",
               description = "Descuenta stock automáticamente, calcula 18% IGV y genera registro de venta en Soles (PEN)")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                                   Principal principal) {
        OrderResponse order = orderService.createOrderFromCart(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Orden generada exitosamente", order));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Listar el historial de compras del cliente autenticado")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrdersByUserEmail(principal.getName())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una orden por ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(id)));
    }

    @GetMapping("/number/{numeroOrden}")
    @Operation(summary = "Buscar orden por su código (ej: ORD-20260904-XXXXXX)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumeroOrden(@PathVariable String numeroOrden) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderByNumeroOrden(numeroOrden)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(summary = "Listar todas las órdenes del sistema paginadas (Admin/Empleado)")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders(pageable)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(summary = "Actualizar estado de la orden (si se cancela, se restituye el stock automáticamente)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Estado de orden actualizado",
                orderService.updateOrderStatus(id, request.getEstado(), principal.getName())));
    }

    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @Operation(summary = "Métricas y KPIs del Dashboard del Panel de Administración",
               description = "Total ventas en PEN, conteo de órdenes, alertas de stock bajo y totales de productos y clientes")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getDashboardSummary()));
    }
}
