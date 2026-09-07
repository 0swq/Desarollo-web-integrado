package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.marcavehiculo.MarcaVehiculoRequest;
import com.autopartes.dto.marcavehiculo.MarcaVehiculoResponse;
import com.autopartes.model.MarcaVehiculo;
import com.autopartes.service.MarcaVehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/marcas-vehiculos")
public class MarcaVehiculoController {

    private final MarcaVehiculoService service;

    public MarcaVehiculoController(MarcaVehiculoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MarcaVehiculoResponse>>> listar() {
        List<MarcaVehiculoResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MarcaVehiculoResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(m))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Marca no encontrada")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MarcaVehiculoResponse>> crear(@Valid @RequestBody MarcaVehiculoRequest request) {
        MarcaVehiculo m = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Marca creada", mapToResponse(m)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MarcaVehiculoResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody MarcaVehiculoRequest request) {
        MarcaVehiculo m = service.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Marca actualizada", mapToResponse(m)));
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<MarcaVehiculoResponse>> toggleActivo(@PathVariable UUID id) {
        service.toggleActivo(id);
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(m))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Marca no encontrada")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private MarcaVehiculoResponse mapToResponse(MarcaVehiculo m) {
        MarcaVehiculoResponse r = new MarcaVehiculoResponse();
        r.setId(m.getId());
        r.setNombre(m.getNombre());
        r.setPaisOrigen(m.getPaisOrigen());
        r.setActivo(m.getActivo());
        return r;
    }
}