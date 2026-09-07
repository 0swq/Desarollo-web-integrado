package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.modelovehiculo.ModeloVehiculoRequest;
import com.autopartes.dto.modelovehiculo.ModeloVehiculoResponse;
import com.autopartes.model.ModeloVehiculo;
import com.autopartes.service.ModeloVehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/modelos-vehiculos")
public class ModeloVehiculoController {

    private final ModeloVehiculoService service;

    public ModeloVehiculoController(ModeloVehiculoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ModeloVehiculoResponse>>> listar() {
        List<ModeloVehiculoResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModeloVehiculoResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(m))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Modelo no encontrado")));
    }

    @GetMapping("/marca/{marcaId}")
    public ResponseEntity<ApiResponse<List<ModeloVehiculoResponse>>> porMarca(@PathVariable UUID marcaId) {
        List<ModeloVehiculoResponse> response = service.buscarPorMarca(marcaId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ModeloVehiculoResponse>> crear(@Valid @RequestBody ModeloVehiculoRequest request) {
        ModeloVehiculo m = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Modelo creado", mapToResponse(m)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ModeloVehiculoResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody ModeloVehiculoRequest request) {
        ModeloVehiculo m = service.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Modelo actualizado", mapToResponse(m)));
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<ModeloVehiculoResponse>> toggleActivo(@PathVariable UUID id) {
        service.toggleActivo(id);
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(m))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Modelo no encontrado")));
    }

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