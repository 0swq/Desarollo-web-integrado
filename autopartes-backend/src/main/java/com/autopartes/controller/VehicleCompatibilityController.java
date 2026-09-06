package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.product.ProductResponse;
import com.autopartes.dto.vehicle.VehicleCompatibilityRequest;
import com.autopartes.dto.vehicle.VehicleCompatibilityResponse;
import com.autopartes.dto.vehicle.VehicleMakeDTO;
import com.autopartes.dto.vehicle.VehicleModelDTO;
import com.autopartes.service.VehicleCompatibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@Tag(name = "Compatibilidad Vehicular", description = "Endpoints para búsqueda de autopartes por marca, modelo y año del vehículo")
public class VehicleCompatibilityController {

    private final VehicleCompatibilityService compatibilityService;

    @GetMapping("/makes")
    @Operation(summary = "Listar marcas de vehículos activas (Toyota, Nissan, Ford, etc.) - Público")
    public ResponseEntity<ApiResponse<List<VehicleMakeDTO>>> getAllMakes() {
        return ResponseEntity.ok(ApiResponse.ok(compatibilityService.getAllActiveMakes()));
    }

    @PostMapping("/makes")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Registrar nueva marca de vehículo (solo ADMIN)")
    public ResponseEntity<ApiResponse<VehicleMakeDTO>> createMake(@Valid @RequestBody VehicleMakeDTO makeDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Marca creada exitosamente", compatibilityService.createMake(makeDTO)));
    }

    @GetMapping("/makes/{makeId}/models")
    @Operation(summary = "Listar modelos de una marca (Yaris, Hilux, Corolla, etc.) - Público")
    public ResponseEntity<ApiResponse<List<VehicleModelDTO>>> getModelsByMake(@PathVariable Long makeId) {
        return ResponseEntity.ok(ApiResponse.ok(compatibilityService.getModelsByMake(makeId)));
    }

    @PostMapping("/models")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Registrar nuevo modelo de vehículo (solo ADMIN)")
    public ResponseEntity<ApiResponse<VehicleModelDTO>> createModel(@Valid @RequestBody VehicleModelDTO modelDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Modelo creado exitosamente", compatibilityService.createModel(modelDTO)));
    }

    @PostMapping("/compatibility")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Asociar compatibilidad entre autoparte, modelo y rango de años (Admin/Empleado)")
    public ResponseEntity<ApiResponse<VehicleCompatibilityResponse>> addCompatibility(
            @Valid @RequestBody VehicleCompatibilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Compatibilidad asociada exitosamente", compatibilityService.addCompatibility(request)));
    }

    @GetMapping("/compatibility/product/{productId}")
    @Operation(summary = "Obtener vehículos compatibles con una autoparte específica (Público)")
    public ResponseEntity<ApiResponse<List<VehicleCompatibilityResponse>>> getCompatibilitiesByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok(compatibilityService.getCompatibilitiesByProduct(productId)));
    }

    @DeleteMapping("/compatibility/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar asociación de compatibilidad (solo ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteCompatibility(@PathVariable Long id) {
        compatibilityService.deleteCompatibility(id);
        return ResponseEntity.ok(ApiResponse.ok("Compatibilidad eliminada", null));
    }

    @GetMapping("/compatible-products")
    @Operation(summary = "Buscar autopartes compatibles por ID de modelo y año (Público)",
               description = "Filtro principal para el cliente: selecciona el modelo y el año del auto y devuelve las autopartes que le calzan.")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> findCompatibleProducts(
            @RequestParam Long modelId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.ok(compatibilityService.findCompatibleProducts(modelId, year)));
    }

    @GetMapping("/compatible-products-by-name")
    @Operation(summary = "Buscar autopartes compatibles por nombre de marca, modelo y año (Público)",
               description = "Permite buscar ingresando texto: make='Toyota', model='Yaris', year=2018")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> findCompatibleProductsByName(
            @RequestParam String make,
            @RequestParam String model,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.ok(compatibilityService.findCompatibleProductsByName(make, model, year)));
    }
}
