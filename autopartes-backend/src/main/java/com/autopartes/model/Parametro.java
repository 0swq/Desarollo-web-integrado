package com.autopartes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parametro {
    private java.util.UUID id;
    private String clave;
    private String valor;
    private String descripcion;
    private LocalDateTime fechaActualizacion;
}
