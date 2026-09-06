package com.autopartes.dto.vehicle;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleCompatibilityRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "El ID del modelo de vehículo es obligatorio")
    private Long vehicleModelId;

    @NotNull(message = "El año de inicio es obligatorio")
    @Min(value = 1960, message = "Año de inicio no válido")
    @Max(value = 2035, message = "Año de inicio no válido")
    private Integer anioInicio;

    @NotNull(message = "El año de fin es obligatorio")
    @Min(value = 1960, message = "Año de fin no válido")
    @Max(value = 2035, message = "Año de fin no válido")
    private Integer anioFin;

    private String motor;
    private String notas;
}
