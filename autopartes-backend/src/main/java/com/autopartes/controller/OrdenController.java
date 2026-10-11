package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.order.OrdenRequest;
import com.autopartes.dto.order.OrdenResponse;
import com.autopartes.model.EstadoOrden;
import com.autopartes.model.Orden;
import com.autopartes.service.OrdenService;
import com.autopartes.service.UsuarioSecurityDetails;
import com.autopartes.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Ordenes", description = "Ordenes generadas desde el carrito")
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService service;
    private final UsuarioService usuarioService;

    public OrdenController(OrdenService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar ordenes", description = "Devuelve todas las ordenes")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrdenResponse>>> listar() {
        List<OrdenResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Obtener orden", description = "Devuelve una orden por su identificador")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrdenResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(o -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(o))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Orden no encontrada")));
    }

    @Operation(summary = "Obtener orden por numero", description = "Busca una orden por su numero generado")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/numero/{numeroOrden}")
    public ResponseEntity<ApiResponse<OrdenResponse>> obtenerPorNumero(@PathVariable String numeroOrden) {
        return service.buscarPorNumeroOrden(numeroOrden)
                .map(o -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(o))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Orden no encontrada")));
    }

    @Operation(summary = "Ordenes de un usuario", description = "Devuelve las ordenes del usuario. Requiere autenticacion")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado para este recurso")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<OrdenResponse>>> porUsuario(@PathVariable UUID usuarioId,
                                                                       @AuthenticationPrincipal UsuarioSecurityDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("No autenticado"));
        }

        var usuarioOpt = usuarioService.buscarPorCorreo(userDetails.getUsername());
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getId().equals(usuarioId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("No autorizado"));
        }

        List<OrdenResponse> response = service.buscarPorUsuario(usuarioId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Ordenes por estado", description = "Filtra ordenes por estado (PENDIENTE, PAGADO, EN_PREPARACION, ENVIADO, ENTREGADO, CANCELADO)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida")
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<OrdenResponse>>> porEstado(@PathVariable EstadoOrden estado) {
        List<OrdenResponse> response = service.buscarPorEstado(estado).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Crear orden desde el carrito", description = "Convierte el carrito en orden, descuenta el stock, registra movimientos y crea el pago inicial. Requiere autenticacion")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recurso creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado para este recurso")
    })
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<OrdenResponse>> crear(@PathVariable UUID usuarioId,
                                                            @Valid @RequestBody OrdenRequest request,
                                                            @AuthenticationPrincipal UsuarioSecurityDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("No autenticado"));
        }

        var usuarioOpt = usuarioService.buscarPorCorreo(userDetails.getUsername());
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getId().equals(usuarioId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("No autorizado"));
        }

        Orden orden = service.crearOrdenDesdeCarrito(usuarioId, request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Orden creada", mapToResponse(orden)));
    }

    @Operation(summary = "Actualizar estado de orden", description = "Cambia el estado de la orden. Al cancelar restituye el stock")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<OrdenResponse>> actualizarEstado(@PathVariable UUID id,
                                                                       @RequestBody ActualizarEstadoRequest request) {
        Orden orden = service.actualizarEstado(id, request.getEstado());
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", mapToResponse(orden)));
    }

    private static class ActualizarEstadoRequest {
        private EstadoOrden estado;
        public EstadoOrden getEstado() { return estado; }
        public void setEstado(EstadoOrden estado) { this.estado = estado; }
    }

    private OrdenResponse mapToResponse(Orden o) {
        OrdenResponse r = new OrdenResponse();
        r.setId(o.getId());
        r.setNumeroOrden(o.getNumeroOrden());
        r.setUsuarioId(o.getUsuarioId());
        r.setEstado(o.getEstado());
        r.setSubtotal(o.getSubtotal());
        r.setIgv(o.getIgv());
        r.setTotal(o.getTotal());
        r.setMoneda(o.getMoneda());
        r.setDireccionEntrega(o.getDireccionEntrega());
        r.setTelefonoContacto(o.getTelefonoContacto());
        r.setNotas(o.getNotas());
        r.setFechaCreacion(o.getFechaCreacion());
        r.setFechaActualizacion(o.getFechaActualizacion());
        return r;
    }
}