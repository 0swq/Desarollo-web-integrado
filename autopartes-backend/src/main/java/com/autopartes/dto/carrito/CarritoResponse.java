package com.autopartes.dto.carrito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.autopartes.dto.itemcarrito.ItemCarritoResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoResponse {

    private UUID id;
    private UUID usuarioId;
    private List<ItemCarritoResponse> items;
    private Integer totalItems;
    private BigDecimal total;
    private String moneda;
}
