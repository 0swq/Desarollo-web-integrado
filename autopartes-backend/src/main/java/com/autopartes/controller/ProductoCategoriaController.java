package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.productocategoria.ProductoCategoriaRequest;
import com.autopartes.dto.productocategoria.ProductoCategoriaResponse;
import com.autopartes.model.ProductoCategoria;
import com.autopartes.service.ProductoCategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Producto-categoria", description = "Asociacion N:M entre productos y categorias")
@RequestMapping("/api/producto-categorias")
public class ProductoCategoriaController {

    private final ProductoCategoriaService service;

    public ProductoCategoriaController(ProductoCategoriaService service) {
        this.service = service;
    }

    @Operation(summary = "Asociar producto y categoria", description = "Crea la asociacion N:M entre un producto y una categoria")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recurso creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductoCategoriaResponse>> asociar(@Valid @RequestBody ProductoCategoriaRequest request) {
        ProductoCategoria pc = service.asociar(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Asociación creada", mapToResponse(pc)));
    }

    @Operation(summary = "Categorias de un producto", description = "Devuelve las categorias asociadas a un producto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<List<ProductoCategoriaResponse>>> porProducto(@PathVariable UUID productoId) {
        List<ProductoCategoriaResponse> response = service.buscarCategoriasPorProducto(productoId).stream()
                .map(catId -> {
                    ProductoCategoriaResponse r = new ProductoCategoriaResponse();
                    r.setProductoId(productoId);
                    r.setCategoriaId(catId);
                    return r;
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Productos de una categoria", description = "Devuelve los productos asociados a una categoria")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<List<ProductoCategoriaResponse>>> porCategoria(@PathVariable UUID categoriaId) {
        List<ProductoCategoriaResponse> response = service.buscarProductosPorCategoria(categoriaId).stream()
                .map(prodId -> {
                    ProductoCategoriaResponse r = new ProductoCategoriaResponse();
                    r.setProductoId(prodId);
                    r.setCategoriaId(categoriaId);
                    return r;
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Desasociar producto y categoria", description = "Elimina la asociacion entre un producto y una categoria")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido")
    })
    @DeleteMapping("/producto/{productoId}/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID productoId, @PathVariable UUID categoriaId) {
        service.eliminar(productoId, categoriaId);
        return ResponseEntity.noContent().build();
    }

    private ProductoCategoriaResponse mapToResponse(ProductoCategoria pc) {
        ProductoCategoriaResponse r = new ProductoCategoriaResponse();
        r.setProductoId(pc.getProductoId());
        r.setCategoriaId(pc.getCategoriaId());
        return r;
    }
}