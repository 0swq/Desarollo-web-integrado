package com.autopartes.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.autopartes.model.EstadoOrden;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenResponse {

    private UUID id;
    private String numeroOrden;
    private UUID usuarioId;
    private EstadoOrden estado;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String moneda;
    private String direccionEntrega;
    private String telefonoContacto;
    private String notas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
