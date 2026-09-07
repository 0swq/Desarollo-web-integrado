package com.autopartes.dto.itemorden;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemOrdenResponse {

    private UUID id;
    private UUID ordenId;
    private UUID productoId;
    private String nombreProducto;
    private String sku;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
