package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.compatibilidadvehiculo.CompatibilidadVehiculoRequest;
import com.autopartes.dto.compatibilidadvehiculo.CompatibilidadVehiculoResponse;
import com.autopartes.model.CompatibilidadVehiculo;
import com.autopartes.service.CompatibilidadVehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/compatibilidades-vehiculos")
public class CompatibilidadVehiculoController {

    private final CompatibilidadVehiculoService service;

    public CompatibilidadVehiculoController(CompatibilidadVehiculoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompatibilidadVehiculoResponse>>> listar() {
        List<CompatibilidadVehiculoResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompatibilidadVehiculoResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(c -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(c))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Compatibilidad no encontrada")));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<List<CompatibilidadVehiculoResponse>>> porProducto(@PathVariable UUID productoId) {
        List<CompatibilidadVehiculoResponse> response = service.buscarPorProducto(productoId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/modelo/{modeloId}")
    public ResponseEntity<ApiResponse<List<CompatibilidadVehiculoResponse>>> porModelo(@PathVariable UUID modeloId) {
        List<CompatibilidadVehiculoResponse> response = service.buscarPorModeloVehiculo(modeloId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/compatibles")
    public ResponseEntity<ApiResponse<List<CompatibilidadVehiculoResponse>>> compatibles(
            @RequestParam UUID modeloId, @RequestParam Integer anio) {
        List<CompatibilidadVehiculoResponse> response = service.buscarCompatiblesPorModeloYAnio(modeloId, anio).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompatibilidadVehiculoResponse>> crear(@Valid @RequestBody CompatibilidadVehiculoRequest request) {
        CompatibilidadVehiculo c = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Compatibilidad creada", mapToResponse(c)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<Void>> eliminarPorProducto(@PathVariable UUID productoId) {
        service.eliminarPorProducto(productoId);
        return ResponseEntity.noContent().build();
    }

    private CompatibilidadVehiculoResponse mapToResponse(CompatibilidadVehiculo c) {
        CompatibilidadVehiculoResponse r = new CompatibilidadVehiculoResponse();
        r.setId(c.getId());
        r.setProductoId(c.getProductoId());
        r.setModeloVehiculoId(c.getModeloVehiculoId());
        r.setAnioInicio(c.getAnioInicio());
        r.setAnioFin(c.getAnioFin());
        r.setMotor(c.getMotor());
        r.setNotas(c.getNotas());
        return r;
    }
}