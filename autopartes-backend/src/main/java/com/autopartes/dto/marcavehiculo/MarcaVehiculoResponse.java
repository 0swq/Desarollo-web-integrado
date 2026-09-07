package com.autopartes.dto.marcavehiculo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarcaVehiculoResponse {

    private UUID id;
    private String nombre;
    private String paisOrigen;
    private Boolean activo;
}
