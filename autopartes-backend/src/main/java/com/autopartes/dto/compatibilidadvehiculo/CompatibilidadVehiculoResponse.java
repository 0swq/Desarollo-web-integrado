package com.autopartes.dto.compatibilidadvehiculo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilidadVehiculoResponse {

    private UUID id;
    private UUID productoId;
    private UUID modeloVehiculoId;
    private Integer anioInicio;
    private Integer anioFin;
    private String motor;
    private String notas;
}
