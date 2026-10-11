package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.compatibilidadvehiculo.CompatibilidadVehiculoRequest;
import com.autopartes.dto.compatibilidadvehiculo.CompatibilidadVehiculoResponse;
import com.autopartes.model.CompatibilidadVehiculo;
import com.autopartes.service.CompatibilidadVehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Compatibilidad de vehiculos", description = "Productos compatibles con modelos de vehiculo")
@RequestMapping("/api/compatibilidades-vehiculos")
public class CompatibilidadVehiculoController {

    private final CompatibilidadVehiculoService service;

    public CompatibilidadVehiculoController(CompatibilidadVehiculoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar compatibilidades", description = "Devuelve todas las compatibilidades")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CompatibilidadVehiculoResponse>>> listar() {
        List<CompatibilidadVehiculoResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Obtener compatibilidad", description = "Devuelve una compatibilidad por su identificador")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompatibilidadVehiculoResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(c -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(c))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Compatibilidad no encontrada")));
    }

    @Operation(summary = "Compatibilidades por producto", description = "Devuelve los modelos compatibles con un producto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<List<CompatibilidadVehiculoResponse>>> porProducto(@PathVariable UUID productoId) {
        List<CompatibilidadVehiculoResponse> response = service.buscarPorProducto(productoId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Compatibilidades por modelo", description = "Devuelve los productos compatibles con un modelo")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping("/modelo/{modeloId}")
    public ResponseEntity<ApiResponse<List<CompatibilidadVehiculoResponse>>> porModelo(@PathVariable UUID modeloId) {
        List<CompatibilidadVehiculoResponse> response = service.buscarPorModeloVehiculo(modeloId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Productos compatibles por anio", description = "Filtra los productos compatibles con un modelo en un anio dado")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida")
    })
    @GetMapping("/compatibles")
    public ResponseEntity<ApiResponse<List<CompatibilidadVehiculoResponse>>> compatibles(
            @RequestParam UUID modeloId, @RequestParam Integer anio) {
        List<CompatibilidadVehiculoResponse> response = service.buscarCompatiblesPorModeloYAnio(modeloId, anio).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Crear compatibilidad", description = "Registra la compatibilidad de un producto con un modelo")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recurso creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CompatibilidadVehiculoResponse>> crear(@Valid @RequestBody CompatibilidadVehiculoRequest request) {
        CompatibilidadVehiculo c = service.crear(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Compatibilidad creada", mapToResponse(c)));
    }

    @Operation(summary = "Eliminar compatibilidad", description = "Elimina una compatibilidad")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar compatibilidades por producto", description = "Elimina todas las compatibilidades de un producto")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
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