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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Pagos", description = "Pagos asociados a una orden")
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar pagos", description = "Devuelve todos los pagos")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listar() {
        List<PagoResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Obtener pago", description = "Devuelve un pago por su identificador")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Pago no encontrado")));
    }

    @Operation(summary = "Pago de una orden", description = "Devuelve el pago asociado a una orden")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<ApiResponse<PagoResponse>> porOrden(@PathVariable UUID ordenId) {
        return service.buscarPorOrden(ordenId)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Pago no encontrado")));
    }

    @Operation(summary = "Pagos por estado", description = "Filtra pagos por estado (PENDIENTE, APROBADO, RECHAZADO, ANULADO)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida")
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> porEstado(@PathVariable EstadoPago estado) {
        List<PagoResponse> response = service.buscarPorEstado(estado).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Registrar pago", description = "Registra un pago para una orden que aun no tiene uno")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recurso creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PagoResponse>> crear(@Valid @RequestBody PagoRequest request) {
        Pago p = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Pago creado", mapToResponse(p)));
    }

    @Operation(summary = "Actualizar estado del pago", description = "Cambia el estado del pago")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<PagoResponse>> actualizarEstado(@PathVariable UUID id,
                                                                       @RequestBody ActualizarEstadoRequest request) {
        Pago p = service.actualizarEstado(id, request.getEstado());
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", mapToResponse(p)));
    }

    @Operation(summary = "Actualizar datos de Mercado Pago", description = "Guarda los identificadores de Mercado Pago del pago")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
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