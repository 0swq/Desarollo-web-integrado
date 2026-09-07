package com.autopartes.dto.marcavehiculo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarcaVehiculoRequest {

    @NotBlank(message = "El nombre de la marca es obligatorio")
    private String nombre;

    private String paisOrigen;

    @Builder.Default
    private Boolean activo = true;
}
