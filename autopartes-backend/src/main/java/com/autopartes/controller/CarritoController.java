package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.carrito.CarritoRequest;
import com.autopartes.dto.carrito.CarritoResponse;
import com.autopartes.dto.itemcarrito.ItemCarritoResponse;
import com.autopartes.model.Carrito;
import com.autopartes.model.ItemCarrito;
import com.autopartes.service.CarritoService;
import com.autopartes.service.ItemCarritoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    private final CarritoService carritoService;
    private final ItemCarritoService itemService;

    public CarritoController(CarritoService carritoService, ItemCarritoService itemService) {
        this.carritoService = carritoService;
        this.itemService = itemService;
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<CarritoResponse>> obtener(@PathVariable UUID usuarioId,
                                                                 @RequestHeader("Authorization") String authHeader) {
        UUID tokenUsuarioId = com.autopartes.util.Auth.extraerId(authHeader.replace("Bearer ", ""));
        if (!tokenUsuarioId.equals(usuarioId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("No autorizado"));
        }
        Carrito carrito = carritoService.obtenerOCrearCarrito(usuarioId);
        return ResponseEntity.ok(ApiResponse.ok(mapToResponse(carrito)));
    }

    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<Void>> vaciar(@PathVariable UUID usuarioId,
                                                     @RequestHeader("Authorization") String authHeader) {
        UUID tokenUsuarioId = com.autopartes.util.Auth.extraerId(authHeader.replace("Bearer ", ""));
        if (!tokenUsuarioId.equals(usuarioId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("No autorizado"));
        }
        var carritoOpt = carritoService.buscarPorUsuario(usuarioId);
        if (carritoOpt.isPresent()) {
            carritoService.vaciarCarrito(carritoOpt.get().getId());
        }
        return ResponseEntity.noContent().build();
    }

    private CarritoResponse mapToResponse(Carrito c) {
        CarritoResponse r = new CarritoResponse();
        r.setId(c.getId());
        List<ItemCarrito> items = itemService.obtenerItems(c.getId());
        r.setItems(items.stream().map(this::mapItemToResponse).toList());
        r.setTotalItems(carritoService.calcularTotalItems(c.getId()));
        r.setTotal(carritoService.calcularTotal(c.getId()));
        r.setMoneda("PEN");
        return r;
    }

    private ItemCarritoResponse mapItemToResponse(ItemCarrito i) {
        ItemCarritoResponse r = new ItemCarritoResponse();
        r.setId(i.getId());
        r.setProductoId(i.getProductoId());
        r.setCantidad(i.getCantidad());
        r.setPrecioUnitario(i.getPrecioUnitario());
        r.setSubtotal(i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())));
        return r;
    }
}