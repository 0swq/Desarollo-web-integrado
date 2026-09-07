package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.producto.ProductoRequest;
import com.autopartes.dto.producto.ProductoResponse;
import com.autopartes.model.Producto;
import com.autopartes.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listar() {
        List<ProductoResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Producto no encontrado")));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorSku(@PathVariable String sku) {
        return service.buscarPorSku(sku)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Producto no encontrado")));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> buscar(@RequestParam String q) {
        List<ProductoResponse> response = service.buscarPorNombreContiene(q).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> porProveedor(@PathVariable UUID proveedorId) {
        List<ProductoResponse> response = service.buscarPorProveedor(proveedorId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest request) {
        Producto p = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Producto creado", mapToResponse(p)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody ProductoRequest request) {
        Producto p = service.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado", mapToResponse(p)));
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<ProductoResponse>> toggleActivo(@PathVariable UUID id) {
        service.toggleActivo(id);
        return service.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Producto no encontrado")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private ProductoResponse mapToResponse(Producto p) {
        ProductoResponse r = new ProductoResponse();
        r.setId(p.getId());
        r.setSku(p.getSku());
        r.setNombre(p.getNombre());
        r.setDescripcion(p.getDescripcion());
        r.setPrecio(p.getPrecio());
        r.setPrecioPromocional(p.getPrecioPromocional());
        r.setMoneda(p.getMoneda());
        r.setImagenUrl(p.getImagenUrl());
        r.setProveedorId(p.getProveedorId());
        r.setActivo(p.getActivo());
        r.setFechaCreacion(p.getFechaCreacion());
        r.setFechaActualizacion(p.getFechaActualizacion());
        return r;
    }
}