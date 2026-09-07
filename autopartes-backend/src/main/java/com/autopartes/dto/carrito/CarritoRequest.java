package com.autopartes.dto.carrito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoRequest {

    @NotNull(message = "El ID del usuario es obligatorio")
    private UUID usuarioId;
}
