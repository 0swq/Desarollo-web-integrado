package com.autopartes.dto.marcavehiculo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarcaVehiculoRequest {

    @NotBlank(message = "El nombre de la marca es obligatorio")
    private String nombre;

    private String paisOrigen;

    @Size(max = 2048, message = "La URL de imagen no puede superar 2048 caracteres")
    private String imagenUrl;

    @Builder.Default
    private Boolean activo = true;
}
