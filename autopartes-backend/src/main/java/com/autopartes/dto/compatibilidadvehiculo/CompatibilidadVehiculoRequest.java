package com.autopartes.dto.compatibilidadvehiculo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilidadVehiculoRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private UUID productoId;

    @NotNull(message = "El ID del modelo de vehículo es obligatorio")
    private UUID modeloVehiculoId;

    @NotNull(message = "El año de inicio es obligatorio")
    @Min(value = 1900, message = "El año de inicio debe ser mayor a 1900")
    @Max(value = 2100, message = "El año de inicio no puede ser mayor a 2100")
    private Integer anioInicio;

    @NotNull(message = "El año de fin es obligatorio")
    @Min(value = 1900, message = "El año de fin debe ser mayor a 1900")
    @Max(value = 2100, message = "El año de fin no puede ser mayor a 2100")
    private Integer anioFin;

    private String motor;
    private String notas;
}
