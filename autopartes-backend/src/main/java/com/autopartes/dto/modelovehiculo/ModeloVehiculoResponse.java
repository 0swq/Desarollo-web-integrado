package com.autopartes.dto.modelovehiculo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeloVehiculoResponse {

    private UUID id;
    private String nombre;
    private UUID marcaId;
    private String tipoVehiculo;
    private Boolean activo;
}
