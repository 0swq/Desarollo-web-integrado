package com.autopartes.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockRequest {

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    private String ubicacionAlmacen;
}
