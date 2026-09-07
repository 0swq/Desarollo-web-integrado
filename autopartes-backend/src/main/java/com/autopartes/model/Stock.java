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
public class Stock {
    private java.util.UUID id;
    private java.util.UUID productoId;
    private Integer cantidad;
    private Integer stockMinimo;
    private String ubicacionAlmacen;
    private LocalDateTime fechaActualizacion;

}