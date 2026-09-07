package com.autopartes.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockResponse {

    private UUID id;
    private UUID productoId;
    private Integer cantidad;
    private Integer stockMinimo;
    private String ubicacionAlmacen;
    private LocalDateTime fechaActualizacion;
}
