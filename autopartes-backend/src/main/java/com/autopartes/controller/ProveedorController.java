package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.proveedor.ProveedorRequest;
import com.autopartes.dto.proveedor.ProveedorResponse;
import com.autopartes.model.Proveedor;
import com.autopartes.service.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Proveedores", description = "Proveedores de productos")
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService service;

    public ProveedorController(ProveedorService service) {
        this.service = service;
    }

    @Operation(summary = "Listar proveedores", description = "Devuelve todos los proveedores")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> listar() {
        List<ProveedorResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Obtener proveedor", description = "Devuelve un proveedor por su identificador")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Proveedor no encontrado")));
    }

    @Operation(summary = "Crear proveedor", description = "Crea un proveedor. El RUC debe tener 11 digitos y ser unico")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recurso creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProveedorResponse>> crear(@Valid @RequestBody ProveedorRequest request) {
        Proveedor p = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Proveedor creado", mapToResponse(p)));
    }

    @Operation(summary = "Actualizar proveedor", description = "Actualiza un proveedor existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody ProveedorRequest request) {
        Proveedor p = service.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Proveedor actualizado", mapToResponse(p)));
    }

    @Operation(summary = "Activar o desactivar proveedor", description = "Invierte el estado activo del proveedor")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<ProveedorResponse>> toggleActivo(@PathVariable UUID id) {
        service.toggleActivo(id);
        return service.buscarPorId(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(p))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Proveedor no encontrado")));
    }

    @Operation(summary = "Eliminar proveedor", description = "Baja logica: marca el proveedor como inactivo")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
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