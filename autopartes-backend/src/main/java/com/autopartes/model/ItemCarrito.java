package com.autopartes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarrito {
    private java.util.UUID id;
    private java.util.UUID carritoId;
    private java.util.UUID productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
