package com.autopartes.dto.pago;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.autopartes.model.EstadoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponse {

    private UUID id;
    private UUID ordenId;
    private BigDecimal monto;
    private String moneda;
    private EstadoPago estado;
    private String metodoPago;
    private String mercadoPagoPagoId;
    private String mercadoPagoPreferenciaId;
    private LocalDateTime fechaCreacion;
}
