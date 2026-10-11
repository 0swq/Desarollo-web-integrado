package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.carrito.CarritoResponse;
import com.autopartes.dto.itemcarrito.ItemCarritoResponse;
import com.autopartes.model.Carrito;
import com.autopartes.model.ItemCarrito;
import com.autopartes.service.CarritoService;
import com.autopartes.service.ItemCarritoService;
import com.autopartes.service.UsuarioSecurityDetails;
import com.autopartes.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Carritos", description = "Carrito del usuario")
@RequestMapping("/api/carritos")
public class CarritoController {

    private final CarritoService carritoService;
    private final ItemCarritoService itemService;
    private final UsuarioService usuarioService;

    public CarritoController(CarritoService carritoService, ItemCarritoService itemService, UsuarioService usuarioService) {
        this.carritoService = carritoService;
        this.itemService = itemService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Obtener o crear carrito", description = "Devuelve el carrito del usuario y lo crea si no existe. Requiere autenticacion")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado para este recurso")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<CarritoResponse>> obtener(@PathVariable UUID usuarioId,
                                                                @AuthenticationPrincipal UsuarioSecurityDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("No autenticado"));
        }

        var usuarioOpt = usuarioService.buscarPorCorreo(userDetails.getUsername());
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getId().equals(usuarioId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("No autorizado"));
        }

        Carrito carrito = carritoService.obtenerOCrearCarrito(usuarioId);
        return ResponseEntity.ok(ApiResponse.ok(mapToResponse(carrito)));
    }

    @Operation(summary = "Vaciar carrito", description = "Elimina todos los items del carrito. Requiere autenticacion")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado para este recurso")
    })
    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<Void>> vaciar(@PathVariable UUID usuarioId,
                                                    @AuthenticationPrincipal UsuarioSecurityDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        var usuarioOpt = usuarioService.buscarPorCorreo(userDetails.getUsername());
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getId().equals(usuarioId)) {
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