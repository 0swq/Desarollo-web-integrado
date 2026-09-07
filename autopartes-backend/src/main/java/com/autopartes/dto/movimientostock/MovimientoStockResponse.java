package com.autopartes.dto.movimientostock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.autopartes.model.TipoMovimiento;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStockResponse {

    private UUID id;
    private UUID productoId;
    private TipoMovimiento tipo;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String motivo;
    private String referencia;
    private UUID usuarioId;
    private LocalDateTime fecha;
}
