package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.order.OrdenRequest;
import com.autopartes.dto.order.OrdenResponse;
import com.autopartes.model.EstadoOrden;
import com.autopartes.model.Orden;
import com.autopartes.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService service;

    public OrdenController(OrdenService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrdenResponse>>> listar() {
        List<OrdenResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrdenResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(o -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(o))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Orden no encontrada")));
    }

    @GetMapping("/numero/{numeroOrden}")
    public ResponseEntity<ApiResponse<OrdenResponse>> obtenerPorNumero(@PathVariable String numeroOrden) {
        return service.buscarPorNumeroOrden(numeroOrden)
                .map(o -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(o))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Orden no encontrada")));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<OrdenResponse>>> porUsuario(@PathVariable UUID usuarioId,
                                                                        @RequestHeader("Authorization") String authHeader) {
        UUID tokenUsuarioId = com.autopartes.util.Auth.extraerId(authHeader.replace("Bearer ", ""));
        if (!tokenUsuarioId.equals(usuarioId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("No autorizado"));
        }
        List<OrdenResponse> response = service.buscarPorUsuario(usuarioId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<OrdenResponse>>> porEstado(@PathVariable EstadoOrden estado) {
        List<OrdenResponse> response = service.buscarPorEstado(estado).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<OrdenResponse>> crear(@PathVariable UUID usuarioId,
                                                             @Valid @RequestBody OrdenRequest request,
                                                             @RequestHeader("Authorization") String authHeader) {
        UUID tokenUsuarioId = com.autopartes.util.Auth.extraerId(authHeader.replace("Bearer ", ""));
        if (!tokenUsuarioId.equals(usuarioId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("No autorizado"));
        }
        Orden orden = service.crearOrdenDesdeCarrito(usuarioId, request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Orden creada", mapToResponse(orden)));
    }

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