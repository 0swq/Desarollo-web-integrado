package com.autopartes.dto.parametro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroRequest {

    @NotBlank(message = "La clave es obligatoria")
    private String clave;

    @NotBlank(message = "El valor es obligatorio")
    private String valor;

    private String descripcion;
}
