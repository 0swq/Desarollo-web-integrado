package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.cart.AddToCartRequest;
import com.autopartes.dto.cart.CartResponse;
import com.autopartes.dto.cart.UpdateCartItemRequest;
import com.autopartes.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Carrito de Compras", description = "Endpoints para el carrito de compras del cliente")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Obtener el carrito actual del cliente autenticado")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCartByUserEmail(principal.getName())));
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar autoparte al carrito (verifica stock disponible)")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(@Valid @RequestBody AddToCartRequest request,
                                                                   Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Producto agregado al carrito",
                cartService.addItemToCart(principal.getName(), request)));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Actualizar cantidad de una autoparte en el carrito")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(@PathVariable Long itemId,
                                                                    @Valid @RequestBody UpdateCartItemRequest request,
                                                                    Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Carrito actualizado",
                cartService.updateCartItem(principal.getName(), itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Eliminar un ítem del carrito")
    public ResponseEntity<ApiResponse<CartResponse>> removeItemFromCart(@PathVariable Long itemId,
                                                                        Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Ítem eliminado del carrito",
                cartService.removeItemFromCart(principal.getName(), itemId)));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Vaciar todo el carrito")
    public ResponseEntity<ApiResponse<Void>> clearCart(Principal principal) {
        cartService.clearCart(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Carrito vaciado", null));
    }
}
