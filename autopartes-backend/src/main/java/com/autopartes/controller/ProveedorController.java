package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.proveedor.ProveedorRequest;
import com.autopartes.dto.proveedor.ProveedorResponse;
import com.autopartes.model.Proveedor;
import com.autopartes.service.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService service;

    public ProveedorController(ProveedorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> listar() {
        List<ProveedorResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Proveedor no encontrado")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProveedorResponse>> crear(@Valid @RequestBody ProveedorRequest request) {
        Proveedor p = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Proveedor creado", mapToResponse(p)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody ProveedorRequest request) {
        Proveedor p = service.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Proveedor actualizado", mapToResponse(p)));
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<ProveedorResponse>> toggleActivo(@PathVariable UUID id) {
        service.toggleActivo(id);
        return service.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Proveedor no encontrado")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private ProveedorResponse mapToResponse(Proveedor p) {
        ProveedorResponse r = new ProveedorResponse();
        r.setId(p.getId());
        r.setRuc(p.getRuc());
        r.setRazonSocial(p.getRazonSocial());
        r.setContactoNombre(p.getContactoNombre());
        r.setTelefono(p.getTelefono());
        r.setCorreo(p.getCorreo());
        r.setDireccion(p.getDireccion());
        r.setActivo(p.getActivo());
        r.setFechaCreacion(p.getFechaCreacion());
        r.setFechaActualizacion(p.getFechaActualizacion());
        return r;
    }
}