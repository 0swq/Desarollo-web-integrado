package com.autopartes.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponse {

    private UUID id;
    private String sku;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal precioPromocional;
    private String moneda;
    private String imagenUrl;
    private UUID proveedorId;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
