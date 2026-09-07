package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.itemcarrito.ItemCarritoRequest;
import com.autopartes.dto.itemcarrito.ItemCarritoResponse;
import com.autopartes.model.ItemCarrito;
import com.autopartes.service.ItemCarritoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carritos/{carritoId}/items")
public class ItemCarritoController {

    private final ItemCarritoService service;

    public ItemCarritoController(ItemCarritoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemCarritoResponse>>> listar(@PathVariable UUID carritoId) {
        List<ItemCarritoResponse> response = service.obtenerItems(carritoId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItemCarritoResponse>> agregar(@PathVariable UUID carritoId,
                                                                     @Valid @RequestBody ItemCarritoRequest request) {
        ItemCarrito item = service.agregarItem(carritoId, request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Item agregado", mapToResponse(item)));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemCarritoResponse>> actualizar(@PathVariable UUID carritoId,
                                                                        @PathVariable UUID itemId,
                                                                        @RequestBody ActualizarCantidadRequest request) {
        ItemCarrito item = service.actualizarCantidad(carritoId, itemId, request.getCantidad());
        return ResponseEntity.ok(ApiResponse.ok("Cantidad actualizada", mapToResponse(item)));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID carritoId, @PathVariable UUID itemId) {
        service.eliminarItem(carritoId, itemId);
        return ResponseEntity.noContent().build();
    }

    private static class ActualizarCantidadRequest {
        private Integer cantidad;
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }

    private ItemCarritoResponse mapToResponse(ItemCarrito i) {
        ItemCarritoResponse r = new ItemCarritoResponse();
        r.setId(i.getId());
        r.setProductoId(i.getProductoId());
        r.setCantidad(i.getCantidad());
        r.setPrecioUnitario(i.getPrecioUnitario());
        r.setSubtotal(i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())));
        return r;
    }
}