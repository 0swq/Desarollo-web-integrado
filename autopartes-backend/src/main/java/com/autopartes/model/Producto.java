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
public class Producto {
    private java.util.UUID id;
    private String sku;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal precioPromocional;
    private String moneda;
    private String imagenUrl;
    private java.util.UUID proveedorId;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
