package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.pago.PagoRequest;
import com.autopartes.dto.pago.PagoResponse;
import com.autopartes.model.EstadoPago;
import com.autopartes.model.Pago;
import com.autopartes.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listar() {
        List<PagoResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Pago no encontrado")));
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<ApiResponse<PagoResponse>> porOrden(@PathVariable UUID ordenId) {
        return service.buscarPorOrden(ordenId)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Pago no encontrado")));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> porEstado(@PathVariable EstadoPago estado) {
        List<PagoResponse> response = service.buscarPorEstado(estado).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PagoResponse>> crear(@Valid @RequestBody PagoRequest request) {
        Pago p = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Pago creado", mapToResponse(p)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<PagoResponse>> actualizarEstado(@PathVariable UUID id,
                                                                       @RequestBody ActualizarEstadoRequest request) {
        Pago p = service.actualizarEstado(id, request.getEstado());
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", mapToResponse(p)));
    }

    @PatchMapping("/{id}/mercado-pago")
    public ResponseEntity<ApiResponse<PagoResponse>> actualizarDatosMercadoPago(@PathVariable UUID id,
                                                                                 @RequestBody MercadoPagoRequest request) {
        Pago p = service.actualizarDatosMercadoPago(id, request.getMercadoPagoPagoId(), request.getMercadoPagoPreferenciaId());
        return ResponseEntity.ok(ApiResponse.ok("Datos MercadoPago actualizados", mapToResponse(p)));
    }

    private static class ActualizarEstadoRequest {
        private EstadoPago estado;
        public EstadoPago getEstado() { return estado; }
        public void setEstado(EstadoPago estado) { this.estado = estado; }
    }

    private static class MercadoPagoRequest {
        private String mercadoPagoPagoId;
        private String mercadoPagoPreferenciaId;
        public String getMercadoPagoPagoId() { return mercadoPagoPagoId; }
        public void setMercadoPagoPagoId(String mercadoPagoPagoId) { this.mercadoPagoPagoId = mercadoPagoPagoId; }
        public String getMercadoPagoPreferenciaId() { return mercadoPagoPreferenciaId; }
        public void setMercadoPagoPreferenciaId(String mercadoPagoPreferenciaId) { this.mercadoPagoPreferenciaId = mercadoPagoPreferenciaId; }
    }

    private PagoResponse mapToResponse(Pago p) {
        PagoResponse r = new PagoResponse();
        r.setId(p.getId());
        r.setOrdenId(p.getOrdenId());
        r.setMonto(p.getMonto());
        r.setMoneda(p.getMoneda());
        r.setEstado(p.getEstado());
        r.setMetodoPago(p.getMetodoPago());
        r.setMercadoPagoPagoId(p.getMercadoPagoPagoId());
        r.setMercadoPagoPreferenciaId(p.getMercadoPagoPreferenciaId());
        r.setFechaCreacion(p.getFechaCreacion());
        return r;
    }
}