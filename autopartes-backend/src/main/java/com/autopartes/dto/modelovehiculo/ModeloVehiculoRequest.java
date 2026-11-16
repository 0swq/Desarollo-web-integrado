package com.autopartes.dto.modelovehiculo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeloVehiculoRequest {

    @NotBlank(message = "El nombre del modelo es obligatorio")
    private String nombre;

    @NotNull(message = "El ID de la marca es obligatorio")
    private UUID marcaId;

    private String tipoVehiculo;

    @Size(max = 2048, message = "La URL de imagen no puede superar 2048 caracteres")
    private String imagenUrl;

    @Builder.Default
    private Boolean activo = true;
}
