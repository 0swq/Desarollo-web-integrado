package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.itemcarrito.ItemCarritoRequest;
import com.autopartes.dto.itemcarrito.ItemCarritoResponse;
import com.autopartes.model.ItemCarrito;
import com.autopartes.service.ItemCarritoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Items del carrito", description = "Items dentro de un carrito")
@RequestMapping("/api/carritos/{carritoId}/items")
public class ItemCarritoController {

    private final ItemCarritoService service;

    public ItemCarritoController(ItemCarritoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar items del carrito", description = "Devuelve los items de un carrito")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemCarritoResponse>>> listar(@PathVariable UUID carritoId) {
        List<ItemCarritoResponse> response = service.obtenerItems(carritoId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Agregar item al carrito", description = "Agrega un producto al carrito validando stock y disponibilidad")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recurso creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ItemCarritoResponse>> agregar(@PathVariable UUID carritoId,
                                                                     @Valid @RequestBody ItemCarritoRequest request) {
        ItemCarrito item = service.agregarItem(carritoId, request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Item agregado", mapToResponse(item)));
    }

    @Operation(summary = "Actualizar cantidad", description = "Cambia la cantidad de un item validando el stock disponible")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemCarritoResponse>> actualizar(@PathVariable UUID carritoId,
                                                                        @PathVariable UUID itemId,
                                                                        @RequestBody ActualizarCantidadRequest request) {
        ItemCarrito item = service.actualizarCantidad(carritoId, itemId, request.getCantidad());
        return ResponseEntity.ok(ApiResponse.ok("Cantidad actualizada", mapToResponse(item)));
    }

    @Operation(summary = "Eliminar item del carrito", description = "Elimina un item del carrito")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
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