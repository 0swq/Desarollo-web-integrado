package com.autopartes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orden {
    private java.util.UUID id;
    private String numeroOrden;
    private java.util.UUID usuarioId;
    private EstadoOrden estado;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String moneda;
    private String direccionEntrega;
    private String telefonoContacto;
    private String notas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
