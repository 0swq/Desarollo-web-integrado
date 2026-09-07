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
public class MovimientoStock {
    private java.util.UUID id;
    private java.util.UUID productoId;
    private TipoMovimiento tipo;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String motivo;
    private String referencia;
    private java.util.UUID usuarioId;
    private LocalDateTime fecha;
}
