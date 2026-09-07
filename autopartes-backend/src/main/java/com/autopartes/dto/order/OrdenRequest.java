package com.autopartes.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenRequest {

    @NotBlank(message = "La dirección de entrega es obligatoria")
    private String direccionEntrega;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String telefonoContacto;

    private String notas;
    private String metodoPago;
}
