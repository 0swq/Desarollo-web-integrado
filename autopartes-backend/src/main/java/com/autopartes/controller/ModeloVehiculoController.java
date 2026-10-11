package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.modelovehiculo.ModeloVehiculoRequest;
import com.autopartes.dto.modelovehiculo.ModeloVehiculoResponse;
import com.autopartes.model.ModeloVehiculo;
import com.autopartes.service.ModeloVehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Modelos de vehiculo", description = "Modelos asociados a una marca")
@RequestMapping("/api/modelos-vehiculos")
public class ModeloVehiculoController {

    private final ModeloVehiculoService service;

    public ModeloVehiculoController(ModeloVehiculoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar modelos de vehiculo", description = "Devuelve todos los modelos")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ModeloVehiculoResponse>>> listar() {
        List<ModeloVehiculoResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Obtener modelo", description = "Devuelve un modelo por su identificador")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModeloVehiculoResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(m))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Modelo no encontrado")));
    }

    @Operation(summary = "Modelos por marca", description = "Devuelve los modelos de una marca")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping("/marca/{marcaId}")
    public ResponseEntity<ApiResponse<List<ModeloVehiculoResponse>>> porMarca(@PathVariable UUID marcaId) {
        List<ModeloVehiculoResponse> response = service.buscarPorMarca(marcaId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Crear modelo", description = "Crea un modelo asociado a una marca")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recurso creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ModeloVehiculoResponse>> crear(@Valid @RequestBody ModeloVehiculoRequest request) {
        ModeloVehiculo m = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Modelo creado", mapToResponse(m)));
    }

    @Operation(summary = "Actualizar modelo", description = "Actualiza un modelo existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ModeloVehiculoResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody ModeloVehiculoRequest request) {
        ModeloVehiculo m = service.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Modelo actualizado", mapToResponse(m)));
    }

    @Operation(summary = "Activar o desactivar modelo", description = "Invierte el estado activo del modelo")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<ModeloVehiculoResponse>> toggleActivo(@PathVariable UUID id) {
        service.toggleActivo(id);
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(m))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Modelo no encontrado")));
    }

    @Operation(summary = "Eliminar modelo", description = "Elimina un modelo")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private ModeloVehiculoResponse mapToResponse(ModeloVehiculo m) {
        ModeloVehiculoResponse r = new ModeloVehiculoResponse();
        r.setId(m.getId());
        r.setNombre(m.getNombre());
        r.setMarcaId(m.getMarcaId());
        r.setTipoVehiculo(m.getTipoVehiculo());
        r.setActivo(m.getActivo());
        return r;
    }
}