package com.autopartes.dto.categoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;

    private String descripcion;
    private String imagenUrl;

    @Builder.Default
    private Boolean activo = true;
}
