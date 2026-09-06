package com.autopartes.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private Boolean activo;
    private LocalDateTime createdAt;
}
