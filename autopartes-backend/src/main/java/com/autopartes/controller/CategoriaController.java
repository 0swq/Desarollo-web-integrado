package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.categoria.CategoriaRequest;
import com.autopartes.dto.categoria.CategoriaResponse;
import com.autopartes.model.Categoria;
import com.autopartes.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listar() {
        List<CategoriaResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(c -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(c))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Categoría no encontrada")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaResponse>> crear(@Valid @RequestBody CategoriaRequest request) {
        Categoria c = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Categoría creada", mapToResponse(c)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody CategoriaRequest request) {
        Categoria c = service.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Categoría actualizada", mapToResponse(c)));
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<CategoriaResponse>> toggleActivo(@PathVariable UUID id) {
        service.toggleActivo(id);
        return service.buscarPorId(id)
                .map(c -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(c))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Categoría no encontrada")));
    }

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