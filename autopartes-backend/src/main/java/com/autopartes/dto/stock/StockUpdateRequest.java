package com.autopartes.dto.stock;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class StockUpdateRequest {
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;
    private String ubicacionAlmacen;
}
