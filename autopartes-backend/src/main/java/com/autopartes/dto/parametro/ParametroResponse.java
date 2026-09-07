package com.autopartes.dto.parametro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroResponse {

    private UUID id;
    private String clave;
    private String valor;
    private String descripcion;
    private LocalDateTime fechaActualizacion;
}
