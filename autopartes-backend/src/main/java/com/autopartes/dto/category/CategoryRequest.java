package com.autopartes.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private Boolean activo = true;
}
