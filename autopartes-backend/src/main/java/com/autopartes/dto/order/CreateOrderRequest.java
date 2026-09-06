package com.autopartes.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "La dirección de entrega es obligatoria")
    private String direccionEntrega;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String telefonoContacto;

    private String notas;
    private String metodoPago; // ej: "MERCADO_PAGO", "EFECTIVO"
}
