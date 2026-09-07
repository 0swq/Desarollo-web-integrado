package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.productocategoria.ProductoCategoriaRequest;
import com.autopartes.dto.productocategoria.ProductoCategoriaResponse;
import com.autopartes.model.ProductoCategoria;
import com.autopartes.service.ProductoCategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/producto-categorias")
public class ProductoCategoriaController {

    private final ProductoCategoriaService service;

    public ProductoCategoriaController(ProductoCategoriaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoCategoriaResponse>> asociar(@Valid @RequestBody ProductoCategoriaRequest request) {
        ProductoCategoria pc = service.asociar(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Asociación creada", mapToResponse(pc)));
    }

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