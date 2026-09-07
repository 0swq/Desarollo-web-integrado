package com.autopartes.dto.movimientostock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.autopartes.model.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStockRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private UUID productoId;

    @NotNull(message = "El tipo de movimiento es obligatorio (ENTRADA, SALIDA, AJUSTE)")
    private TipoMovimiento tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "El stock anterior es obligatorio")
    private Integer stockAnterior;

    @NotNull(message = "El stock nuevo es obligatorio")
    private Integer stockNuevo;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private String referencia;
    private UUID usuarioId;
}
