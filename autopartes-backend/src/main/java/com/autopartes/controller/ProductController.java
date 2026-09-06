package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.product.ProductRequest;
import com.autopartes.dto.product.ProductResponse;
import com.autopartes.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Endpoints de catálogo y administración de autopartes")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar productos activos paginados (Público)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllActiveProducts(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getAllActiveProducts(pageable)));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar autopartes por nombre, SKU o descripción (Público)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam String query,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(productService.searchProducts(query, pageable)));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar productos por categoría (Público)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductsByCategory(categoryId, pageable)));
    }

    @GetMapping("/featured")
    @Operation(summary = "Obtener autopartes destacadas para la pantalla de bienvenida (Público)")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getFeaturedProducts()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener autoparte por ID (Público)")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductById(id)));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Obtener autoparte por código SKU (Público)")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductBySku(sku)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear nueva autoparte en catálogo con stock inicial (Admin/Empleado)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Producto creado exitosamente", productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar autoparte (Admin/Empleado)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id,
                                                                       @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar (desactivar) autoparte (solo ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Producto desactivado exitosamente", null));
    }
}
