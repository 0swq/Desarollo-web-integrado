package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.categoria.CategoriaRequest;
import com.autopartes.dto.categoria.CategoriaResponse;
import com.autopartes.model.Categoria;
import com.autopartes.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Categorias", description = "Categorias de productos")
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar categorias", description = "Devuelve todas las categorias")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listar() {
        List<CategoriaResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Obtener categoria", description = "Devuelve una categoria por su identificador")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(c -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(c))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Categoría no encontrada")));
    }

    @Operation(summary = "Crear categoria", description = "Crea una categoria nueva")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recurso creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaResponse>> crear(@Valid @RequestBody CategoriaRequest request) {
        Categoria c = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Categoría creada", mapToResponse(c)));
    }

    @Operation(summary = "Actualizar categoria", description = "Actualiza una categoria existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody CategoriaRequest request) {
        Categoria c = service.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Categoría actualizada", mapToResponse(c)));
    }

    @Operation(summary = "Activar o desactivar categoria", description = "Invierte el estado activo de la categoria")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<CategoriaResponse>> toggleActivo(@PathVariable UUID id) {
        service.toggleActivo(id);
        return service.buscarPorId(id)
                .map(c -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(c))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Categoría no encontrada")));
    }

    @Operation(summary = "Eliminar categoria", description = "Elimina una categoria")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private CategoriaResponse mapToResponse(Categoria c) {
        CategoriaResponse r = new CategoriaResponse();
        r.setId(c.getId());
        r.setNombre(c.getNombre());
        r.setDescripcion(c.getDescripcion());
        r.setImagenUrl(c.getImagenUrl());
        r.setActivo(c.getActivo());
        r.setFechaCreacion(c.getFechaCreacion());
        r.setFechaActualizacion(c.getFechaActualizacion());
        return r;
    }
}