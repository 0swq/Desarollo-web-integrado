package com.autopartes.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    private Long id;
    private Long productId;
    private String productSku;
    private String productNombre;
    private Integer cantidad;
    private Integer stockMinimo;
    private Boolean bajoStock;
    private String ubicacionAlmacen;
    private LocalDateTime updatedAt;
}
