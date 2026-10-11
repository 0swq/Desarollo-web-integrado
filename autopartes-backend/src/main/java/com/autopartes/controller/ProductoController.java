package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.producto.ProductoRequest;
import com.autopartes.dto.producto.ProductoResponse;
import com.autopartes.model.Producto;
import com.autopartes.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Productos", description = "Catalogo de productos")
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar productos", description = "Devuelve todos los productos")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listar() {
        List<ProductoResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Obtener producto", description = "Devuelve un producto por su identificador")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Producto no encontrado")));
    }

    @Operation(summary = "Obtener producto por SKU", description = "Busca un producto por su codigo SKU")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorSku(@PathVariable String sku) {
        return service.buscarPorSku(sku)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Producto no encontrado")));
    }

    @Operation(summary = "Buscar productos", description = "Busca productos por nombre, descripcion o SKU")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> buscar(@RequestParam String q) {
        List<ProductoResponse> response = service.buscarPorNombreContiene(q).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Productos por proveedor", description = "Devuelve los productos de un proveedor")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> porProveedor(@PathVariable UUID proveedorId) {
        List<ProductoResponse> response = service.buscarPorProveedor(proveedorId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Crear producto", description = "Crea un producto, lo asocia a una categoria e inicializa su stock")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recurso creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest request) {
        Producto p = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Producto creado", mapToResponse(p)));
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza un producto y sus categorias")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody ProductoRequest request) {
        Producto p = service.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado", mapToResponse(p)));
    }

    @Operation(summary = "Activar o desactivar producto", description = "Invierte el estado activo del producto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<ProductoResponse>> toggleActivo(@PathVariable UUID id) {
        service.toggleActivo(id);
        return service.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Producto no encontrado")));
    }

    @Operation(summary = "Eliminar producto", description = "Baja logica: marca el producto como inactivo")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
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