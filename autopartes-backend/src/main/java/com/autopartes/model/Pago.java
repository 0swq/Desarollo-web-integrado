package com.autopartes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {
    private java.util.UUID id;
    private java.util.UUID ordenId;
    private BigDecimal monto;
    private String moneda;
    private EstadoPago estado;
    private String metodoPago;
    private String mercadoPagoPagoId;
    private String mercadoPagoPreferenciaId;
    private LocalDateTime fechaCreacion;
}
